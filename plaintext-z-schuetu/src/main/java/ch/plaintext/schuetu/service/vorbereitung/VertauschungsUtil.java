package ch.plaintext.schuetu.service.vorbereitung;

import ch.plaintext.schuetu.entity.Spiel;
import ch.plaintext.schuetu.entity.SpielZeile;
import ch.plaintext.schuetu.model.enums.PlatzEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@Deprecated
public class VertauschungsUtil {

    public void korrekturenVornehmen(Iterable<SpielZeile> zeilen, List<String> korrekturen) {
        if (korrekturen == null || korrekturen.isEmpty()) { return; }
        for (String ko : korrekturen) { replace(zeilen, ko); }
    }

    private void replace(Iterable<SpielZeile> zeilen, String ko) {
        ko = ko.replace(";", "");
        String[] sp = ko.split("-");
        String key = sp[0]; String value = sp[1];
        SpielZeile quelle = findZeile(zeilen, convertSonntag(key), convertTime(key));
        SpielZeile ziel = findZeile(zeilen, convertSonntag(value), convertTime(value));
        Spiel quelleSpiel = getSpielFromZeile(zeilen, quelle, key);
        Spiel zielSpiel = getSpielFromZeile(zeilen, ziel, value);
        setSpielToZeile(zeilen, quelle, key, zielSpiel);
        setSpielToZeile(zeilen, ziel, value, quelleSpiel);
    }

    private SpielZeile findZeile(Iterable<SpielZeile> zeilen, boolean sonntag, String zeit) {
        for (SpielZeile spielZeile : zeilen) { if (spielZeile.isSonntag() == sonntag && spielZeile.getZeitAsString().equals(zeit)) { return spielZeile; } }
        return null;
    }

    private boolean convertSonntag(String in) { return in.contains("so"); }
    private String convertTime(String in) { return in.split(",")[1]; }
    private String convertPlatz(String in) { return in.split(",")[2]; }

    private Spiel getSpielFromZeile(Iterable<SpielZeile> zeilen, SpielZeile zeileIn, String key) {
        SpielZeile zeile = this.findOne(zeileIn, zeilen);
        String platz = convertPlatz(key); Spiel ret = null;
        if (platz.equals("a")) { ret = zeile.getA(); zeile.setA(null); }
        if (platz.equals("b")) { ret = zeile.getB(); zeile.setB(null); }
        if (platz.equals("c")) { ret = zeile.getC(); zeile.setC(null); }
        return ret;
    }

    private void setSpielToZeile(Iterable<SpielZeile> zeilen, SpielZeile zeileIn, String key, Spiel spiel) {
        SpielZeile zeile = this.findOne(zeileIn, zeilen);
        String platz = convertPlatz(key);
        if (platz.equals("a")) { zeile.setA(spiel); if (spiel != null) { spiel.setPlatz(PlatzEnum.A); } }
        if (platz.equals("b")) { zeile.setB(spiel); if (spiel != null) { spiel.setPlatz(PlatzEnum.B); } }
        if (platz.equals("c")) { zeile.setC(spiel); if (spiel != null) { spiel.setPlatz(PlatzEnum.C); } }
        if (spiel != null) { spiel.setStart(zeile.getStart()); }
    }

    private SpielZeile findOne(SpielZeile zeileIn, Iterable<SpielZeile> zeilen) {
        for (SpielZeile zeile : zeilen) { if (zeileIn.getId().equals(zeile.getId())) { return zeileIn; } }
        return null;
    }
}
