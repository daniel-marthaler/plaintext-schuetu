package ch.plaintext.schuetu.service.websiteinfo;

import ch.plaintext.schuetu.service.DevStrategy;
import ch.plaintext.schuetu.service.Game;
import ch.plaintext.schuetu.service.GameRoot;
import ch.plaintext.schuetu.service.ResultateVerarbeiter;
import ch.plaintext.schuetu.service.websiteinfo.model.*;
import ch.plaintext.schuetu.model.Einstellungen;
import ch.plaintext.schuetu.entity.Kategorie;
import ch.plaintext.schuetu.entity.Spiel;
import ch.plaintext.schuetu.model.comparators.KategorieKlasseUndGeschlechtComparator;
import ch.plaintext.schuetu.model.enums.GeschlechtEnum;
import ch.plaintext.schuetu.repository.KategorieRepository;
import ch.plaintext.schuetu.repository.MannschaftRepository;
import ch.plaintext.schuetu.repository.SpielRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.collections4.comparators.ComparatorChain;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.StringWriter;
import java.util.*;

/**
 * Zugriff auf die Mannschaften, Kategorien und Resultate fuer die Darstellung
 * auf der Webseite
 */
@Component
@Slf4j
public class WebsiteInfoService {

    private WebsiteInfoJahresDump jetzt = new WebsiteInfoJahresDump();

    private Map<String, WebsiteInfoJahresDump> alt = new HashMap<>();

    @Autowired(required = false)
    private DevStrategy dev;

    @Autowired
    private MannschaftRepository mannschaftRepo;

    @Autowired
    private KategorieRepository kategorieRepository;

    @Autowired
    private SpielRepository spielRepository;

    @Autowired
    private GameRoot games;

    // TODO: ContextUtil from old framework removed
    // @Autowired
    // private ContextUtil ctx;

    @PostConstruct
    private void init() {
        this.initOldJahresdump();
    }

    public void dumpJetzt(String jahr) {
        this.getFinalspiele(jahr);
        this.getGruppenspiele(jahr);
        this.getKnabenMannschaften(jahr, false);
        this.getMaedchenMannschaften(jahr, false);
        this.getRangliste(jahr);
        this.getEinstellungen(jahr);

    }

    public List<Spiel> getGruppenspiele(String jahr) {

        if (spielRepository.findGruppenSpielAsc(jahr).size() > 0) {

            List<Spiel> spiele = spielRepository.findGruppenSpielAsc(jahr);

            for (Spiel s : spiele) {
                if (BooleanUtils.isTrue(s.getMannschaftA().getDisqualifiziert())) {
                    s.setMannschaftA(null);
                }
                if (BooleanUtils.isTrue(s.getMannschaftB().getDisqualifiziert())) {
                    s.setMannschaftB(null);
                }
            }

            jetzt.setGruppenspiele(spiele);
            return jetzt.getGruppenspiele();
        }

        WebsiteInfoJahresDump dump = getOldJahredump(jahr);

        if (dump != null) {
            return getOldJahredump(jahr).getGruppenspiele();
        }

        return new ArrayList<>();
    }

    public Einstellungen getEinstellungen(String jahr) {

        Game game = games.selectGame(jahr);

        if (game != null) {
            return game.getModel();
        } else {
            WebsiteInfoJahresDump dump = getOldJahredump(jahr);
            if (dump != null) {
                return dump.getEinstellung();
            }
            return null;
        }
    }

    public WebsiteInfoJahresDump getOldJahredump(String jahr) {
        return alt.get(jahr);
    }

    public void initOldJahresdump() {

        if (dev != null && !dev.getLoadOldGames()) {
            log.info("Dev Strategy: Lade alte Spiele nicht");
            return;
        }
    }

    public Collection<String> getOldJahre() {
        return this.alt.keySet();
    }

    public List<Spiel> getFinalspiele(String jahr) {

        if (spielRepository.findFinalSpielAsc(jahr).size() > 0) {
            jetzt.setFinalspiele(spielRepository.findFinalSpielAsc(jahr));
            return jetzt.getFinalspiele();
        }

        WebsiteInfoJahresDump dump = getOldJahredump(jahr);

        if (dump != null) {
            return getOldJahredump(jahr).getFinalspiele();
        }

        return new ArrayList<>();
    }

    public List<KlassenrangZeile> getRangliste(String jahr) {

        Game game = games.selectGame(jahr);

        if (game == null) {
            WebsiteInfoJahresDump dump = getOldJahredump(jahr);
            if (dump != null) {
                return dump.getRangliste();
            }
            return new ArrayList<>();
        }

        ResultateVerarbeiter verarbeiter = game.getResultate();

        if (verarbeiter == null) {
            WebsiteInfoJahresDump dump = getOldJahredump(jahr);
            if (dump != null) {
                return dump.getRangliste();
            }
            return new ArrayList<>();
        }

        this.jetzt.setRangliste(verarbeiter.getRanglisteModel());
        return jetzt.getRangliste();
    }

    public List<KlassenrangZeile> getRanglisteReal(String gameS) {

        Game game = games.selectGame(gameS);

        ResultateVerarbeiter verarbeiter = game.getResultate();

        List<KlassenrangZeile> ret = verarbeiter.getRanglisteModelVerkuendigung();

        BeanComparator<KlassenrangZeile> comp = new BeanComparator<>();
        comp.setProperty("klasse");

        ret.sort(comp);

        return ret;
    }

    public List<TeamGruppen> getKnabenMannschaften(String jahr, boolean ganzeliste) {

        if (mannschaftRepo.findByGame(jahr).size() > 0) {
            if (ganzeliste) {
                return convertToGanzeGruppe(mannschaftRepo.findByGame(jahr), true);
            }
            return convertToKategorienGruppen(kategorieRepository.findByGame(jahr), true);
        }

        WebsiteInfoJahresDump dump = getOldJahredump(jahr);

        if (dump != null) {
            return getOldJahredump(jahr).getKnabenMannschaften();
        }

        return new ArrayList<>();
    }

    public List<TeamGruppen> getMaedchenMannschaften(String jahr, boolean ganzeliste) {
        List<ch.plaintext.schuetu.entity.Mannschaft> mannschaften = mannschaftRepo.findByGame(jahr);
        if (mannschaften != null && mannschaften.size() > 0) {
            if (ganzeliste) {
                return convertToGanzeGruppe(mannschaften, false);
            }
            return convertToKategorienGruppen(kategorieRepository.findByGame(jahr), false);
        }

        WebsiteInfoJahresDump dump = getOldJahredump(jahr);

        if (dump != null) {
            return getOldJahredump(jahr).getMaedchenMannschaften();
        }

        return new ArrayList<>();
    }

    public List<Mannschaft> getMaedchenMannschaften2(String jahr) {

        Game game = games.selectGame(jahr);
        boolean alle = false;
        if (game != null) {
            if (game.getModel().getWebsiteInMannschaftslistenmode()) {
                alle = true;
            }
        }

        List<TeamGruppen> gr = getMaedchenMannschaften(jahr, alle);
        List<Mannschaft> mannschaften = new ArrayList<>();
        for (TeamGruppen g : gr) {
            mannschaften.addAll(g.getMannschaften());
        }

        BeanComparator<Mannschaft> mannschaftsKategorieComperator = new BeanComparator<>("gruppe");
        BeanComparator<Mannschaft> mannschaftsNameKomperator = new BeanComparator<>("nummer");

        ComparatorChain<Mannschaft> chain = new ComparatorChain<>();
        chain.addComparator(mannschaftsKategorieComperator);
        chain.addComparator(mannschaftsNameKomperator);

        mannschaften.sort(chain);

        return mannschaften;
    }

    public List<Mannschaft> getKnabenMannschaften2(String jahr) {

        Game game = games.selectGame(jahr);
        boolean alle = false;
        if (game != null) {
            if (game.getModel().getWebsiteInMannschaftslistenmode()) {
                alle = true;
            }
        }

        List<TeamGruppen> gr = getKnabenMannschaften(jahr, alle);
        List<Mannschaft> mannschaften = new ArrayList<>();
        for (TeamGruppen g : gr) {
            mannschaften.addAll(g.getMannschaften());
        }

        BeanComparator<Mannschaft> mannschaftsKategorieComperator = new BeanComparator<>("gruppe");
        BeanComparator<Mannschaft> mannschaftsNameKomperator = new BeanComparator<>("nummer");

        ComparatorChain<Mannschaft> chain = new ComparatorChain<>();
        chain.addComparator(mannschaftsKategorieComperator);
        chain.addComparator(mannschaftsNameKomperator);

        mannschaften.sort(chain);

        return mannschaften;
    }

    private List<TeamGruppen> convertToKategorienGruppen(List<Kategorie> kategorien, boolean knaben) {
        kategorien.sort(new KategorieKlasseUndGeschlechtComparator());
        List<Kategorie> maedchen = new ArrayList<>();
        List<Kategorie> kanben = new ArrayList<>();
        List<Kategorie> list;

        List<TeamGruppen> result = new ArrayList<>();

        for (Kategorie k : kategorien) {

            if (k.getMannschaften() == null || k.getMannschaften().isEmpty()) {
                continue;
            }

            if (k.getMannschaften().get(0).getGeschlecht() == GeschlechtEnum.K) {
                kanben.add(k);
            } else {
                maedchen.add(k);
            }
        }
        if (knaben) {
            list = kanben;
        } else {
            list = maedchen;
        }

        for (Kategorie m : list) {

            TeamGruppen gr = new TeamGruppen();
            gr.setName("Gruppe: " + m.getName());
            this.mannschaftenKonvertierenUndEinfuellen(m.getMannschaften(), gr);
            result.add(gr);
        }

        if (knaben) {
            jetzt.setKnabenMannschaften(result);
        } else {
            jetzt.setMaedchenMannschaften(result);
        }

        return result;
    }

    private List<TeamGruppen> convertToGanzeGruppe(List<ch.plaintext.schuetu.entity.Mannschaft> mannschaften, boolean knaben) {

        List<ch.plaintext.schuetu.entity.Mannschaft> maedchen = new ArrayList<>();
        List<ch.plaintext.schuetu.entity.Mannschaft> kanben = new ArrayList<>();

        for (ch.plaintext.schuetu.entity.Mannschaft m : mannschaften) {
            if (m.getGeschlecht() == GeschlechtEnum.K) {
                kanben.add(m);
            } else {
                maedchen.add(m);
            }
        }

        TeamGruppen result = new TeamGruppen();
        if (!knaben) {
            mannschaftenKonvertierenUndEinfuellen(maedchen, result);
            result.setName("Die Maedchenteams");
        } else {
            mannschaftenKonvertierenUndEinfuellen(kanben, result);
            result.setName("Die Knabenteams");
        }
        List<TeamGruppen> reslist = new ArrayList<>();
        reslist.add(result);
        return reslist;
    }

    private void mannschaftenKonvertierenUndEinfuellen(List<ch.plaintext.schuetu.entity.Mannschaft> mannschaften, TeamGruppen result) {
        for (ch.plaintext.schuetu.entity.Mannschaft m : mannschaften) {
            Mannschaft ma = new Mannschaft();
            if (BooleanUtils.isTrue(m.getDisqualifiziert())) {
                ma.setNummer("-");
                ma.setBegleitperson("-");
                ma.setCaptain("-");
                ma.setKlassenname("-");
                ma.setKlasse("-");
                ma.setSpieler(0);
                ma.setSchulhaus("-");
            } else {
                ma.setNummer(m.getName());
                ma.setBegleitperson(m.getBegleitperson2Vorname() + " " + m.getBegleitperson2Name());
                ma.setCaptain(m.getCaptain2Vorname() + " " + m.getCaptain2Name());
                ma.setKlassenname(m.getKlassenBezeichnung());
                ma.setKlasse("" + m.getKlasse());
                ma.setSpieler(m.getAnzahlSpieler());
                ma.setSchulhaus(m.getSchulhaus());
            }

            try {
                if (m.getKategorie() != null) {
                    ma.setGruppe(m.getKategorie().getName());
                } else {
                    ma.setGruppe("-");
                }
            } catch (Exception e) {
                ma.setGruppe("--");
            }

            result.addMannschaft(ma);
            result.setTotal(result.getTotal() + ma.getSpieler());
        }

        // sortieren nach klasse
        result.getMannschaften().sort(new MannschaftsComperator());
    }
}
