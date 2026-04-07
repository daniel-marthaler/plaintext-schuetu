package ch.plaintext.schuetu.service.vorbereitung;

import ch.plaintext.schuetu.service.vorbereitung.helper.SpielverteilerHelper;
import ch.plaintext.schuetu.service.vorbereitung.helper.SpielzeilenValidator;
import ch.plaintext.schuetu.entity.GameModel;
import ch.plaintext.schuetu.entity.Kategorie;
import ch.plaintext.schuetu.entity.Spiel;
import ch.plaintext.schuetu.entity.SpielZeile;
import ch.plaintext.schuetu.model.comparators.KategorieKlasseUndGeschlechtComparator;
import ch.plaintext.schuetu.model.enums.PlatzEnum;
import ch.plaintext.schuetu.model.enums.SpielTageszeit;
import ch.plaintext.schuetu.repository.KategorieRepository;
import ch.plaintext.schuetu.repository.SpielRepository;
import ch.plaintext.schuetu.repository.SpielZeilenRepository;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class E5Spielverteiler {

    @Autowired private SpielZeilenRepository spielzeilenRepo;
    @Autowired private SpielRepository spielRepo;
    @Autowired private KategorieRepository katRepo;
    @Autowired private SpielzeilenValidator val;
    @Autowired private SpielverteilerHelper helper;

    private boolean groesser6AufCRegel;
    private int groesser6AufCMin;

    public String spieleAutomatischVerteilen(GameModel game) {
        StringBuilder result = new StringBuilder();
        behandleFinalspielzeilen(game.getGameName());
        SpielZeile vorherVorher = null; SpielZeile vorher = null;
        List<SpielZeile> gruppenSpieleZeilen = spielzeilenRepo.findGruppenSpielZeilen(game.getGameName());
        List<Spiel> gruppenSpiele = spielRepo.findGruppenSpiel(game.getGameName());
        helper.init(gruppenSpiele);
        groesser6AufCRegel = game.isGroesser6AufC(); groesser6AufCMin = game.getGroesser6AufCMin();
        for (SpielZeile zeileJetzt : gruppenSpieleZeilen) {
            if (zeileJetzt.isPause() && !isSamstagNachNeuekategoriesperre(zeileJetzt)) { vorherVorher = vorher; vorher = zeileJetzt; continue; }
            stopA(vorher, vorherVorher, gruppenSpiele, zeileJetzt, game);
            stopB(vorher, vorherVorher, gruppenSpiele, zeileJetzt, game);
            stopC(vorher, vorherVorher, gruppenSpiele, zeileJetzt, game);
            vorherVorher = vorher; vorher = spielzeilenRepo.save(zeileJetzt);
        }
        if (gruppenSpiele.size() > 0) { result.append("NICHT ZUGEORDNETE SPIELE !!! ").append(gruppenSpiele.size()); log.error(result.toString()); }
        List<SpielZeile> zeilenSo = getSpielzeilen(true, game); List<SpielZeile> zeilenSa = getSpielzeilen(false, game);
        removeUeberschuss(zeilenSo); removeUeberschuss(zeilenSa);
        return result.toString();
    }

    public List<SpielZeile> getSpielzeilen(final boolean sonntag, GameModel game) {
        List<SpielZeile> ret;
        if (!sonntag) { ret = spielzeilenRepo.findSpieleSamstag(game.getGameName()); } else { ret = spielzeilenRepo.findSpieleSonntag(game.getGameName()); }
        SpielZeile vorher = null; SpielZeile vorVorher = null;
        for (final SpielZeile spielZeile : ret) { this.val.validateSpielZeilen(vorher, vorVorher, spielZeile, game); vorVorher = vorher; vorher = spielZeile; }
        return ret;
    }

    private void stopA(SpielZeile vorher, SpielZeile vorherVorher, List<Spiel> gruppenSpiele, SpielZeile zeilen, GameModel model) {
        boolean stopA = false; int iA = 0;
        while (!stopA) {
            Spiel tempSpiel = this.getNextSpiel(zeilen, gruppenSpiele, iA, false);
            if (tempSpiel == null) { break; }
            tempSpiel.setPlatz(PlatzEnum.A); zeilen.setA(tempSpiel); tempSpiel.setStart(zeilen.getStart()); zeilen.setKonfliktText(null); zeilen.setPause(false);
            String ret = val.validateSpielZeilen(vorher, vorherVorher, zeilen, model);
            if (ret == null || ret.equals("")) { gruppenSpiele.remove(tempSpiel); helper.consumeSpiel(tempSpiel); spielRepo.save(tempSpiel); stopA = true; } else { zeilen.setA(null); }
            iA++;
        }
    }

    private void stopB(SpielZeile vorher, SpielZeile vorherVorher, List<Spiel> gruppenSpiele, SpielZeile zeilen, GameModel model) {
        boolean stopB = false; int iB = 0;
        while (!stopB) {
            Spiel tempSpiel = this.getNextSpiel(zeilen, gruppenSpiele, iB, false);
            if (tempSpiel == null) { break; }
            tempSpiel.setPlatz(PlatzEnum.B); zeilen.setB(tempSpiel); tempSpiel.setStart(zeilen.getStart()); zeilen.setKonfliktText(null); zeilen.setPause(false);
            String ret = val.validateSpielZeilen(vorher, vorherVorher, zeilen, model);
            if (ret == null || ret.equals("")) { helper.consumeSpiel(tempSpiel); gruppenSpiele.remove(tempSpiel); spielRepo.save(tempSpiel); stopB = true; } else { zeilen.setB(null); }
            iB++;
        }
    }

    private void stopC(SpielZeile vorher, SpielZeile vorherVorher, List<Spiel> gruppenSpiele, SpielZeile zeilen, GameModel game) {
        boolean stopC = false; int iC = 0;
        while (!stopC) {
            Spiel tempSpiel = this.getNextSpiel(zeilen, gruppenSpiele, iC, true);
            if (tempSpiel == null) { break; }
            tempSpiel.setPlatz(PlatzEnum.C); zeilen.setC(tempSpiel); tempSpiel.setStart(zeilen.getStart()); zeilen.setKonfliktText(null); zeilen.setPause(false);
            String ret = val.validateSpielZeilen(vorher, vorherVorher, zeilen, game);
            if (ret == null || ret.equals("")) { helper.consumeSpiel(tempSpiel); gruppenSpiele.remove(tempSpiel); spielRepo.save(tempSpiel); stopC = true; } else { zeilen.setC(null); }
            iC++;
        }
    }

    private void behandleFinalspielzeilen(String game) {
        List<Kategorie> kategorien = katRepo.findByGame(game);
        kategorien.sort(new KategorieKlasseUndGeschlechtComparator());
        List<SpielZeile> finalspieleZeilen = spielzeilenRepo.findFinalSpielZeilen(game);
        List<SpielZeile> finalspieleZeilenGeaendert = new ArrayList<>();
        List<Spiel> spieleGeaendert = new ArrayList<>();
        boolean first = true; Spiel letztesFinale = null;
        for (Kategorie k : kategorien) {
            if (k.getGrosserFinal() == null && k.getKleineFinal() == null) {
                log.warn("Kategorie '{}' hat keine Finalspiele, ueberspringe", k.getName());
                continue;
            }
            if (finalspieleZeilen.isEmpty()) {
                log.warn("Keine Finalspielzeilen mehr verfuegbar fuer Kategorie '{}'", k.getName());
                break;
            }
            SpielZeile zeile = finalspieleZeilen.remove(0);
            if (k.getGrosserfinal2() != null) { mergeGrosserFinal2ToSpielzeile(spieleGeaendert, zeile, k.getGrosserfinal2()); }
            if (first) { mergeKleinerFinalToSpielzeile(spieleGeaendert, k, zeile); first = false; }
            else { mergeKleinerFinalToSpielzeile(spieleGeaendert, k, zeile); if (letztesFinale != null) { mergeGrosserFinalToSpielzeile(spieleGeaendert, zeile, letztesFinale); } }
            letztesFinale = k.getGrosserFinal(); finalspieleZeilenGeaendert.add(zeile);
        }
        if (!finalspieleZeilen.isEmpty() && letztesFinale != null) {
            SpielZeile zeile = finalspieleZeilen.remove(0);
            mergeGrosserFinalToSpielzeile(spieleGeaendert, zeile, letztesFinale); finalspieleZeilenGeaendert.add(zeile);
        }
        spielRepo.saveAll(spieleGeaendert); spielzeilenRepo.saveAll(finalspieleZeilenGeaendert);
    }

    private void mergeKleinerFinalToSpielzeile(List<Spiel> spieleGeaendert, Kategorie k, SpielZeile zeile) {
        if (k.getKleineFinal() != null) { zeile.setB(k.getKleineFinal()); k.getKleineFinal().setPlatz(PlatzEnum.B); k.getKleineFinal().setStart(zeile.getStart()); spieleGeaendert.add(k.getKleineFinal()); }
    }

    private void mergeGrosserFinalToSpielzeile(List<Spiel> spieleGeaendert, SpielZeile zeile, Spiel grosserFinal) {
        zeile.setA(grosserFinal); grosserFinal.setPlatz(PlatzEnum.A); grosserFinal.setStart(zeile.getStart()); spieleGeaendert.add(grosserFinal);
    }

    private void mergeGrosserFinal2ToSpielzeile(List<Spiel> spieleGeaendert, SpielZeile zeile, Spiel grosserFinal) {
        zeile.setC(grosserFinal); grosserFinal.setPlatz(PlatzEnum.C); grosserFinal.setStart(zeile.getStart()); spieleGeaendert.add(grosserFinal);
    }

    private Spiel getNextSpiel(SpielZeile zeile, List<Spiel> gruppenSpiele, int iB, boolean groesserAls6Kl) {
        Spiel firstEgal = null; int ieg = 0; int ineg = 0;
        for (Spiel spiel : gruppenSpiele) {
            if (groesserAls6Kl && groesser6AufCRegel) { if (spiel.getMannschaftA().getKlasse() < groesser6AufCMin) { continue; } }
            if (!groesserAls6Kl && groesser6AufCRegel) { if (spiel.getMannschaftA().getKlasse() > groesser6AufCMin - 1) { continue; } }
            if (firstEgal == null && spiel.getMannschaftA().getKategorie().getSpielwunsch() == SpielTageszeit.EGAL) {
                if (ieg == iB) { if (isSamstagNachNeuekategoriesperre(zeile)) { if (helper.isFirstSpielNotInGruppe(spiel)) { firstEgal = spiel; } } else { firstEgal = spiel; } } ieg++;
            }
            if (zeile.getSpieltageszeit() == spiel.getMannschaftA().getKategorie().getSpielwunsch()) {
                if (ineg == iB) { if (isSamstagNachNeuekategoriesperre(zeile)) { if (helper.isFirstSpielNotInGruppe(spiel)) { return spiel; } } else { return spiel; } } ineg++;
            }
        }
        return firstEgal;
    }

    private boolean isSamstagNachNeuekategoriesperre(SpielZeile zeile) {
        DateTime start = new DateTime(zeile.getStart());
        return zeile.getSpieltageszeit() == SpielTageszeit.SAMSTAGNACHMITTAG && start.getHourOfDay() > 17;
    }

    private void removeUeberschuss(List<SpielZeile> zeilen) {
        List<SpielZeile> remove = new ArrayList<>();
        for (SpielZeile spielZeile : zeilen) { if (spielZeile.isPause()) { remove.add(spielZeile); } else { break; } }
        for (int i = zeilen.size() - 1; i > 0; i--) { if (zeilen.get(i).isPause()) { remove.add(zeilen.get(i)); } else { break; } }
        spielzeilenRepo.deleteAll(remove);
    }
}
