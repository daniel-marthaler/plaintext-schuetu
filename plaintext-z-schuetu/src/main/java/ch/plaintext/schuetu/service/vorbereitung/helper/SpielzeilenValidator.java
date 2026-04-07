package ch.plaintext.schuetu.service.vorbereitung.helper;

import ch.plaintext.schuetu.entity.GameModel;
import ch.plaintext.schuetu.entity.Mannschaft;
import ch.plaintext.schuetu.entity.SpielZeile;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class SpielzeilenValidator {

    public String validateSpielZeilen(SpielZeile zeileVorher, SpielZeile zeileVorVorher, SpielZeile zeileJetzt, GameModel model) {
        int zweipausenBisKlasse = model.getZweiPausenBisKlasse();
        for (Mannschaft m : zeileJetzt.getAllMannschaften()) { m.setKonflikt(false); }
        String ret = "";
        Set<Mannschaft> konflikteVorVorher = new HashSet<>();
        ret = pruefeObInVorherigerZeileVorhanden(zeileVorVorher, zeileJetzt, ret, konflikteVorVorher, true, zweipausenBisKlasse);
        Set<Mannschaft> konflikteVorher = new HashSet<>();
        ret = pruefeObInVorherigerZeileVorhanden(zeileVorher, zeileJetzt, ret, konflikteVorher, false, zweipausenBisKlasse);
        ret = pruefeDoppelteIngleicherZeile(zeileJetzt, ret);
        zeileJetzt.setKonfliktText(ret);
        return ret;
    }

    private String pruefeDoppelteIngleicherZeile(SpielZeile zeileJetzt, String ret) {
        Set<Mannschaft> doppelte = new HashSet<>();
        for (Mannschaft jetzt : zeileJetzt.getAllMannschaften()) {
            int i = 0;
            for (Mannschaft vergleich : zeileJetzt.getAllMannschaften()) { if (jetzt.equals(vergleich)) { i++; } }
            if (i > 1) { jetzt.setKonflikt(true); doppelte.add(jetzt); }
        }
        if (doppelte.size() > 0) {
            StringBuilder retBuilder = new StringBuilder(ret + " in dieser zeile hat es doppelte mannschaften:");
            for (Mannschaft mannschaft : doppelte) { retBuilder.append(" ").append(mannschaft.getName()); }
            ret = retBuilder.toString() + "!";
        }
        return ret;
    }

    private String pruefeObInVorherigerZeileVorhanden(SpielZeile zeileVorher, SpielZeile zeileJetzt, String retIn, Set<Mannschaft> konflikte, boolean vorvorher, int zweipausenBisKlasse) {
        String ret = retIn;
        if (zeileVorher != null) {
            List<Mannschaft> vorherList = zeileVorher.getAllMannschaften();
            for (Mannschaft jetzt : zeileJetzt.getAllMannschaften()) {
                if (vorvorher && zweipausenBisKlasse < jetzt.getKlasse()) { continue; }
                if (vorherList.contains(jetzt)) { jetzt.setKonflikt(true); konflikte.add(jetzt); }
            }
            if (konflikte.size() > 0) {
                if (vorvorher) { ret = ret + " Bereits in der vor-voherigen zeile vorhanden:"; } else { ret = ret + " Bereits in der voherigen zeile vorhanden:"; }
                StringBuilder retBuilder = new StringBuilder(ret);
                for (Mannschaft mannschaft : konflikte) { retBuilder.append(" ").append(mannschaft.getName()); }
                ret = retBuilder.toString() + "!";
            }
        }
        return ret;
    }
}
