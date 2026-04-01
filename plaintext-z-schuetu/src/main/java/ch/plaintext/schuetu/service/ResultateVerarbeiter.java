package ch.plaintext.schuetu.service;

import ch.plaintext.schuetu.service.backupsync.BackupSyncProvider;
import ch.plaintext.schuetu.service.html.HTMLOutConverter;
import ch.plaintext.schuetu.service.html.HTMLSpielMatrixConverter;
import ch.plaintext.schuetu.service.html.ModelConverterRangliste;
import ch.plaintext.schuetu.service.websiteinfo.VelocityReplacer;
import ch.plaintext.schuetu.service.websiteinfo.model.KlassenrangZeile;
import ch.plaintext.schuetu.entity.Kategorie;
import ch.plaintext.schuetu.entity.Mannschaft;
import ch.plaintext.schuetu.entity.Penalty;
import ch.plaintext.schuetu.entity.Spiel;
import ch.plaintext.schuetu.model.comparators.MannschaftsNameComparator;
import ch.plaintext.schuetu.model.enums.SpielEnum;
import ch.plaintext.schuetu.model.ranglistensortierung.RanglisteneintragHistorie;
import ch.plaintext.schuetu.model.ranglistensortierung.RanglisteneintragZeile;
import ch.plaintext.schuetu.repository.KategorieRepository;
import ch.plaintext.schuetu.repository.SpielRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Verarbeitet Resultate und berechnet Ranglisten
 */
@Component
@Scope("prototype")
@Slf4j
public class ResultateVerarbeiter implements GameConnectable {

    private static final int WAITTIME = 1000 * 15;

    @Autowired
    private HTMLSpielMatrixConverter matrix;

    @Autowired
    private VelocityReplacer web;

    @Autowired
    private HTMLOutConverter historieGenerator;

    @Autowired
    private SpielRepository spielRepo;
    @Autowired
    private KategorieRepository katRepo;

    @Autowired
    private ModelConverterRangliste ranglisteConverter;

    @Autowired
    private BackupSyncProvider syncProvider;

    @Autowired
    private PenaltyLoaderFactory penaltyLoaderFactory;

    private Game game;

    private Map<String, Boolean> beendet = new HashMap<>();
    private Queue<Long> spielQueue = new ConcurrentLinkedQueue<>();
    private Queue<Penalty> penaltyQueue = new ConcurrentLinkedQueue<>();
    private Map<String, RanglisteneintragHistorie> map = new HashMap<>();

    public void signalPenalty(Penalty p) {
        penaltyQueue.offer(p);
    }

    public RanglisteneintragHistorie getHistorie(String kategorie) {
        return map.get(kategorie);
    }

    public void verarbeitePenalty() {
        Penalty p = null;
        try {

            if (penaltyQueue.size() > 0) {
                p = penaltyQueue.remove();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        if (p == null) {
            return;
        }

        log.info("verarbeite penalty: " + p);

        this.neuberechnenDerKategorie(p.getKategorie().getKategorie(), game.getModel().getGameName());
    }

    public boolean isFertig() {

        if (this.spielQueue.size() > 0) {
            return false;
        }

        if (this.penaltyQueue.size() > 0) {
            return false;
        }

        beendet.remove("invalide");
        if (beendet.size() < 1) {
            return false;
        }

        for (boolean ok : beendet.values()) {
            if (!ok) {
                return false;
            }
        }

        return true;
    }

    public void initFertigMap() {
        if (beendet.size() > 0) {
            return;
        }
        List<Kategorie> katList = this.katRepo.findByGame(game.getModel().getGameName());

        for (Kategorie kat : katList) {
            this.beendet.put(kat.getName(), false);
        }
    }

    public void signalFertigesSpiel(Long id) {

        // to backup
        syncProvider.signalSpiel(id, game.getModel().getGameName());

        spielQueue.offer(id);
        log.info("spiel signalisiert: " + id + " queuesize: " + spielQueue.size());
    }

    public int getQueueSize() {
        int count = spielQueue.size();
        count = count + penaltyQueue.size();
        return count;
    }

    @Scheduled(fixedRate = WAITTIME)
    @Transactional
    public void verarbeiten() {
        boolean done = false;
        try {

            Long id = null;
            try {
                id = spielQueue.remove();
            } catch (NoSuchElementException e) {
                id = null;
            }

            while (id != null) {

                // map mit den fertig flags initialisieren
                initFertigMap();

                verarbeitePenalty();

                verarbeiteSpiel(id);
                done = true;
                try {
                    id = spielQueue.remove();
                } catch (NoSuchElementException e) {
                    id = null;
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        // upload
        if (done) {
            web.dump(game.getModel().getGameName());
        }

    }

    private void verarbeiteSpiel(Long id) {

        log.info("verarbeite fertiges spiel: " + id);
        Spiel spiel = spielRepo.findById(id).orElse(null);
        if (spiel == null) {
            log.warn("Spiel {} not found", id);
            return;
        }

        Kategorie kat;
        String katName = "";

        try {
            if (spiel.getMannschaftA() == null || spiel.getMannschaftA().getKategorie() == null) {
                log.warn("Spiel {} has no mannschaftA or kategorie", id);
                return;
            }
            kat = spiel.getMannschaftA().getKategorie();
            katName = kat.getName();
        } catch (Exception e) {
            log.error(spiel.getTyp() + ":" + spiel.getIdString());
            log.error(e.getMessage(), e);
            return;
        }

        RanglisteneintragHistorie rangListe = null;

        // hat a und b = mehr als 7 mannschaften
        try {
            if (spiel.getTyp() == SpielEnum.GRUPPE && !spiel.getGruppe().getKategorie().hasVorUndRueckrunde() && spiel.getGruppe().getKategorie().getGruppeB().getMannschaften().size() > 0) {

                aIstInGruppeA(spiel, katName);

                aIstInGruppeB(spiel, katName);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        rangListe = normalerEintrag(spiel, katName);

        pruefePenalty(spiel, rangListe);

        spiel = spielRepo.findById(id).get();

        kat = spiel.getGruppe().getKategorie();

        pruefeUndSetzeFinale(spiel, kat, katName, rangListe);

        pruefeEnde(kat, rangListe);

        toFinish();

    }

    private void pruefePenalty(Spiel spiel, RanglisteneintragHistorie rangListe) {

        Penalty penA = rangListe.getPenaltyA();
        Penalty penB = rangListe.getPenaltyB();

        // in kategorie setzen, falls neue penalty
        if (spiel.getGruppe().getKategorie().getPenaltyA() == null && penA != null) {
            spiel.getGruppe().getKategorie().setPenaltyA(penA);
            katRepo.save(spiel.getGruppe().getKategorie());
        }

        if (spiel.getGruppe().getKategorie().getPenaltyB() == null && penB != null) {
            spiel.getGruppe().getKategorie().setPenaltyA(penB);
            katRepo.save(spiel.getGruppe().getKategorie());
        }

    }

    private void pruefeEnde(Kategorie kat, RanglisteneintragHistorie rangListe) {
        // pruefe ob rangliste kategorie fertig
        if (rangListe.isFertigGespielt()) {
            boolean fertig = false;
            if (kat.getGrosserFinal() != null && kat.getGrosserFinal().isFertigBestaetigt()) {
                fertig = true;
            }

            if (kat.getKleineFinal() != null && !kat.getKleineFinal().isFertigBestaetigt()) {
                fertig = false;
            }

            if (kat.getGrosserfinal2() != null && !kat.getGrosserfinal2().isFertigBestaetigt()) {
                fertig = false;
            }

            this.beendet.put(kat.getName(), fertig);
        }
    }

    private void toFinish() {
        // pruefe ob rangliste kategorie fertig
        for (Boolean temp : this.beendet.values()) {
            if (temp == Boolean.FALSE) {
                return;
            }
        }
        this.game.setSpielPhase("fertig");
    }

    private RanglisteneintragHistorie normalerEintrag(Spiel spiel, String katName) {
        // normalen eintrag
        RanglisteneintragHistorie rangListe;

        rangListe = map.get(katName);

        if (rangListe == null) {
            rangListe = new RanglisteneintragHistorie(spiel, null, null, game.getModel().getGameName(), penaltyLoaderFactory::getPenalty);
        } else {
            rangListe = new RanglisteneintragHistorie(spiel, rangListe, null, game.getModel().getGameName(), penaltyLoaderFactory::getPenalty);
        }

        if (rangListe.isPenaltyAuswertungNoetig()) {
            rangListe = new RanglisteneintragHistorie(null, rangListe, null, game.getModel().getGameName(), penaltyLoaderFactory::getPenalty);
        }

        map.put(katName, rangListe);
        return rangListe;
    }

    private void aIstInGruppeA(Spiel spiel, String katName) {
        RanglisteneintragHistorie rangListe;
        if (spiel.getGruppe().getKategorie().getGruppeA().getMannschaften().contains(spiel.getMannschaftA()) && spiel.getTyp() == SpielEnum.GRUPPE) {
            rangListe = map.get(katName + "_A");

            if (rangListe == null) {
                rangListe = new RanglisteneintragHistorie(spiel, null, Boolean.TRUE, game.getModel().getGameName(), penaltyLoaderFactory::getPenalty);
            } else {
                rangListe = new RanglisteneintragHistorie(spiel, rangListe, Boolean.TRUE, game.getModel().getGameName(), penaltyLoaderFactory::getPenalty);
            }

            if (rangListe.isPenaltyAuswertungNoetig()) {
                rangListe = new RanglisteneintragHistorie(null, rangListe, Boolean.TRUE, game.getModel().getGameName(), penaltyLoaderFactory::getPenalty);
            }

            map.put(katName + "_A", rangListe);
        }
    }

    private void aIstInGruppeB(Spiel spiel, String katName) {
        RanglisteneintragHistorie rangListe;
        if (spiel.getGruppe().getKategorie().getGruppeB().getMannschaften().contains(spiel.getMannschaftA()) && spiel.getTyp() == SpielEnum.GRUPPE) {

            rangListe = map.get(katName + "_B");

            if (rangListe == null) {
                rangListe = new RanglisteneintragHistorie(spiel, null, Boolean.FALSE, game.getModel().getGameName(), penaltyLoaderFactory::getPenalty);
            } else {
                rangListe = new RanglisteneintragHistorie(spiel, rangListe, Boolean.FALSE, game.getModel().getGameName(), penaltyLoaderFactory::getPenalty);
            }
            if (rangListe.isPenaltyAuswertungNoetig()) {
                rangListe = new RanglisteneintragHistorie(null, rangListe, Boolean.FALSE, game.getModel().getGameName(), penaltyLoaderFactory::getPenalty);
            }

            map.put(katName + "_B", rangListe);
        }
    }

    private void pruefeUndSetzeFinale(Spiel spiel, Kategorie kat, String katName, RanglisteneintragHistorie rangListe) {

        // im falle einer korrektur soll nichts mehr neu gesetzt werden
        if (kat.getNosave() != null && kat.getNosave()) {
            log.info("Finale werden nicht neu berechnet, da wir hier eine manuelle Korrektur hatten");
            return;
        }
        if (rangListe.isFertigGespielt() && spiel.getTyp() == SpielEnum.GRUPPE) {

            String katS = kat.getName();

            List<Mannschaft> gross = new ArrayList<>();
            List<Mannschaft> klein = new ArrayList<>();

            List<Mannschaft> zweiteGross = new ArrayList<>();

            // nur 3
            if (kat.hasVorUndRueckrunde()) {
                RanglisteneintragHistorie rl = map.get(katName);
                gross.add(rl.getZeilen().get(0).getMannschaft());
                gross.add(rl.getZeilen().get(1).getMannschaft());
            } else if (kat.isMixedKlassen() && game.getModel().isBehandleFinaleProKlassebeiZusammengefuehrten()) {
                finaleSuchenNachKlasse(kat, gross, klein, zweiteGross);
            } else {
                finaleSuchenNormal(kat, gross, klein);
            }

            // die richtige reihenfolge
            klein.sort(new MannschaftsNameComparator());
            gross.sort(new MannschaftsNameComparator());
            zweiteGross.sort(new MannschaftsNameComparator());

            // gross setzen immer
            kat.getGrosserFinal().setMannschaftA(gross.get(0));
            kat.getGrosserFinal().setMannschaftB(gross.get(1));

            if (kat.getKleineFinal() != null) {
                kat.getKleineFinal().setMannschaftA(klein.get(0));
                kat.getKleineFinal().setMannschaftB(klein.get(1));
            }

            if (kat.computeAnzahlFinale() > 2) {
                kat.getGrosserfinal2().setMannschaftA(zweiteGross.get(0));
                kat.getGrosserfinal2().setMannschaftB(zweiteGross.get(1));
            }

            this.katRepo.save(kat);

        }

    }

    private void finaleSuchenNormal(Kategorie kat, List<Mannschaft> gross, List<Mannschaft> klein) {

        log.info("final-spiel-suche-start");

        if (!kat.has2Groups()) {

            RanglisteneintragHistorie rl = map.get(kat.getName());
            gross.add(rl.getZeilen().get(0).getMannschaft());
            gross.add(rl.getZeilen().get(1).getMannschaft());

            klein.add(rl.getZeilen().get(2).getMannschaft());
            klein.add(rl.getZeilen().get(3).getMannschaft());

        } else {
            RanglisteneintragHistorie rla = map.get(kat.getName() + "_A");
            RanglisteneintragHistorie rlb = map.get(kat.getName() + "_B");

            gross.add(rla.getZeilen().get(0).getMannschaft());
            gross.add(rlb.getZeilen().get(0).getMannschaft());

            klein.add(rla.getZeilen().get(1).getMannschaft());
            klein.add(rlb.getZeilen().get(1).getMannschaft());

        }
    }

    private void finaleSuchenNachKlasse(Kategorie kat, List<Mannschaft> gross, List<Mannschaft> klein, List<Mannschaft> zweiteGross) {

        int klasseTief = 0;
        int klasseHoch = 0;

        List<Mannschaft> listeTief = new ArrayList<>();
        List<Mannschaft> listeHoch = new ArrayList<>();

        RanglisteneintragHistorie rl = map.get(kat.getName());
        klasseTief = rl.getZeilen().get(0).getMannschaft().getKlasse();
        for (RanglisteneintragZeile temp : rl.getZeilen()) {
            if (klasseTief != temp.getMannschaft().getKlasse()) {
                klasseHoch = temp.getMannschaft().getKlasse();
                listeHoch.add(temp.getMannschaft());
            } else {
                listeTief.add(temp.getMannschaft());
            }
        }
        if (klasseTief > klasseHoch) {
            int temp = klasseTief;
            List<Mannschaft> listeTemp = listeTief;
            klasseTief = klasseHoch;
            listeTief = listeHoch;
            klasseHoch = temp;
            listeHoch = listeTemp;
        }

        log.info("*** klassen gefunden: tief " + klasseTief + " und hoch " + klasseHoch);

        if (kat.computeAnzahlFinale() > 2) {
            gross.add(listeHoch.get(0));
            gross.add(listeHoch.get(1));

            zweiteGross.add(listeTief.get(0));
            zweiteGross.add(listeTief.get(1));

            if (listeHoch.size() >= 4) {
                klein.add(listeHoch.get(2));
                klein.add(listeHoch.get(3));
            } else if (listeTief.size() >= 4) {
                klein.add(listeTief.get(2));
                klein.add(listeTief.get(3));
            }

            if (listeTief.size() >= 4 && listeHoch.size() >= 4) {
                log.error("ACHTUNG: 2. Grosser Final, haette aber auch 2 kleine Finale !!!" + listeHoch.toString() + "/" + listeTief.toString());
            }

        } else if (!kat.isMixedAndWithEinzelklasse()) {
            gross.add(listeTief.get(0));
            gross.add(listeTief.get(1));

            klein.add(listeHoch.get(0));
            klein.add(listeHoch.get(1));
        } else {
            List<Mannschaft> listeSortGo = new ArrayList<>();
            if (listeTief.size() == 1) {
                listeSortGo = listeHoch;
            } else if (listeHoch.size() == 1) {
                listeSortGo = listeTief;
            } else {
                log.error("achtung!!! isMixedAndWithEinzelklasse aber keine klasse mit nur einer anzahl in kategorie gefunden");
            }

            if (listeSortGo.size() < 2) {
                log.error("achtung!!! isMixedAndWithEinzelklasse groessere liste hat nicht mehr 2 mannschaften");
            } else {
                gross.add(listeSortGo.get(0));
                gross.add(listeSortGo.get(1));
            }

            if (listeSortGo.size() > 3) {
                log.info("kleiner finale gefunden, mehr als 3 mannschaften in gruppe");
                klein.add(listeSortGo.get(2));
                klein.add(listeSortGo.get(3));
            } else {
                log.info("kein kleiner finale, weniger als 4 mannschaften vorhanden");
            }

        }

    }

    public void neuberechnenAlleKategorien() {
        for (Kategorie kat : katRepo.findByGame(game.getModel().getGameName())) {
            neuberechnenDerKategorie(kat, game.getModel().getGameName());
        }
    }

    public void neuberechnenDerKategorie(Kategorie kat, String game) {
        if (kat == null) return;

        try {
            String katName = kat.getName();

            map.remove(katName);
            map.remove(katName + "_A");
            map.remove(katName + "_B");

            List<Spiel> spiele = this.spielRepo.findGruppenSpielAsc(game);

            for (Spiel spiel : spiele) {
                try {
                    if (spiel.isFertigBestaetigt() && spiel.getMannschaftA() != null
                            && spiel.getMannschaftA().getKategorie() != null
                            && spiel.getMannschaftA().getKategorie().getName().equals(katName)) {
                        this.signalFertigesSpiel(spiel.getId());
                    }
                } catch (Exception e) {
                    log.debug("Skipping spiel {} in neuberechnen: {}", spiel.getId(), e.getMessage());
                }
            }

            if (kat.getKleineFinal() != null && kat.getKleineFinal().isFertigBestaetigt()) {
                this.signalFertigesSpiel(kat.getKleineFinal().getId());
            }

            if (kat.getGrosserFinal() != null && kat.getGrosserFinal().isFertigBestaetigt()) {
                this.signalFertigesSpiel(kat.getGrosserFinal().getId());
            }

            if (kat.getGrosserfinal2() != null && kat.getGrosserfinal2().isFertigBestaetigt()) {
                this.signalFertigesSpiel(kat.getGrosserfinal2().getId());
            }
        } catch (Exception e) {
            log.warn("neuberechnenDerKategorie failed for {}: {}", kat, e.getMessage());
        }
    }

    public String generateSpieleMatrix() {
        List<Kategorie> kategorien = this.katRepo.findByGame(game.getModel().getGameName());
        return matrix.generateSpieleTable(kategorien);
    }

    public String generateRanglistenHistorieForKategorieName(String kategorieName) {
        RanglisteneintragHistorie h = this.map.get(kategorieName);
        return historieGenerator.getRangliste(h);
    }

    public List<KlassenrangZeile> getRanglisteModel() {

        List<RanglisteneintragHistorie> ranglisten = new ArrayList<>();

        Set<String> set = map.keySet();

        for (String key : set) {
            if (!key.contains("_A") && !key.contains("_B")) {
                ranglisten.add(map.get(key));
            }
        }

        return this.ranglisteConverter.convertKlassenrangZeile(ranglisten);
    }

    public List<KlassenrangZeile> getRanglisteModelVerkuendigung() {

        List<RanglisteneintragHistorie> ranglisten = new ArrayList<>();

        Set<String> set = map.keySet();

        for (String key : set) {
            if (!key.contains("_A") && !key.contains("_B")) {
                ranglisten.add(map.get(key));
            }
        }

        return this.ranglisteConverter.convertKlassenrangZeileVerkuendigung(ranglisten);
    }

    @Deprecated
    public String generateRanglistenHistorie(final RanglisteneintragHistorie ranglistenHistorie) {
        return historieGenerator.getRangliste(ranglistenHistorie);
    }

    @Deprecated
    public Collection<RanglisteneintragHistorie> getAllHystorien() {
        return this.map.values();
    }

    public Set<String> getKeys() {
        return this.map.keySet();
    }

    @Override
    public Game getGame() {
        return this.game;
    }

    @Override
    public void setGame(Game game) {
        this.game = game;
    }

}
