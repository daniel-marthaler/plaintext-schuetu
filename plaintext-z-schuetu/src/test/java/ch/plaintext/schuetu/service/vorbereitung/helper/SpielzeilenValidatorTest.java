package ch.plaintext.schuetu.service.vorbereitung.helper;

import ch.plaintext.schuetu.entity.GameModel;
import ch.plaintext.schuetu.entity.Mannschaft;
import ch.plaintext.schuetu.entity.Spiel;
import ch.plaintext.schuetu.entity.SpielZeile;
import ch.plaintext.schuetu.model.enums.GeschlechtEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SpielzeilenValidatorTest {

    private final SpielzeilenValidator validator = new SpielzeilenValidator();

    private Mannschaft createMannschaft(int teamNr, int klasse) {
        Mannschaft m = new Mannschaft();
        m.setTeamNummer(teamNr);
        m.setKlasse(klasse);
        m.setGeschlecht(GeschlechtEnum.M);
        m.setFarbe("");
        m.setSchulhaus("");
        return m;
    }

    private SpielZeile createZeile(Mannschaft mA, Mannschaft mB) {
        SpielZeile zeile = new SpielZeile();
        Spiel spiel = new Spiel();
        spiel.setMannschaftA(mA);
        spiel.setMannschaftB(mB);
        zeile.setA(spiel);
        return zeile;
    }

    private GameModel createModel() {
        GameModel model = new GameModel();
        model.setZweiPausenBisKlasse(3);
        return model;
    }

    @Test
    void testValidateSpielZeilen_NoConflict() {
        Mannschaft m1 = createMannschaft(1, 5);
        Mannschaft m2 = createMannschaft(2, 5);
        Mannschaft m3 = createMannschaft(3, 5);
        Mannschaft m4 = createMannschaft(4, 5);

        SpielZeile vorher = createZeile(m1, m2);
        SpielZeile jetzt = createZeile(m3, m4);

        String result = validator.validateSpielZeilen(vorher, null, jetzt, createModel());
        assertEquals("", result);
    }

    @Test
    void testValidateSpielZeilen_ConflictWithPreviousLine() {
        Mannschaft m1 = createMannschaft(1, 5);
        Mannschaft m2 = createMannschaft(2, 5);

        SpielZeile vorher = createZeile(m1, m2);
        SpielZeile jetzt = createZeile(m1, m2); // same mannschaften

        String result = validator.validateSpielZeilen(vorher, null, jetzt, createModel());
        assertFalse(result.isEmpty());
        assertTrue(result.contains("voherigen zeile"));
    }

    @Test
    void testValidateSpielZeilen_DuplicateInSameLine() {
        Mannschaft m1 = createMannschaft(1, 5);

        SpielZeile jetzt = createZeile(m1, m1); // same mannschaft twice

        String result = validator.validateSpielZeilen(null, null, jetzt, createModel());
        assertFalse(result.isEmpty());
        assertTrue(result.contains("doppelte mannschaften"));
    }

    @Test
    void testValidateSpielZeilen_ConflictWithPreviousPreviousLine() {
        Mannschaft m1 = createMannschaft(1, 2); // klasse 2, below zweipausenBisKlasse (3)
        Mannschaft m2 = createMannschaft(2, 2);
        Mannschaft m3 = createMannschaft(3, 5);
        Mannschaft m4 = createMannschaft(4, 5);

        SpielZeile vorVorher = createZeile(m1, m2);
        SpielZeile vorher = createZeile(m3, m4);
        SpielZeile jetzt = createZeile(m1, m2); // same as vorVorher

        String result = validator.validateSpielZeilen(vorher, vorVorher, jetzt, createModel());
        assertFalse(result.isEmpty());
        assertTrue(result.contains("vor-voherigen zeile"));
    }

    @Test
    void testValidateSpielZeilen_VorVorherIgnoredForHigherKlasse() {
        Mannschaft m1 = createMannschaft(1, 5); // klasse 5, above zweipausenBisKlasse (3)
        Mannschaft m2 = createMannschaft(2, 5);
        Mannschaft m3 = createMannschaft(3, 5);
        Mannschaft m4 = createMannschaft(4, 5);

        SpielZeile vorVorher = createZeile(m1, m2);
        SpielZeile vorher = createZeile(m3, m4);
        SpielZeile jetzt = createZeile(m1, m2); // same as vorVorher but klasse > zweipausen

        String result = validator.validateSpielZeilen(vorher, vorVorher, jetzt, createModel());
        // Should not have vorvorher conflict because klasse (5) > zweipausenBisKlasse (3)
        assertFalse(result.contains("vor-voherigen"));
    }

    @Test
    void testValidateSpielZeilen_AllNull() {
        Mannschaft m1 = createMannschaft(1, 5);
        Mannschaft m2 = createMannschaft(2, 5);
        SpielZeile jetzt = createZeile(m1, m2);

        String result = validator.validateSpielZeilen(null, null, jetzt, createModel());
        assertEquals("", result);
    }
}
