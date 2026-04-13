package ch.plaintext.schuetu.entity;

import ch.plaintext.schuetu.model.TurnierException;
import ch.plaintext.schuetu.model.enums.GeschlechtEnum;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PenaltyBusinessLogicTest {

    private Mannschaft createMannschaft(String nickname, int teamNr, int klasse) {
        Mannschaft m = new Mannschaft();
        m.setNickname(nickname);
        m.setTeamNummer(teamNr);
        m.setKlasse(klasse);
        m.setGeschlecht(GeschlechtEnum.M);
        m.setFarbe("");
        m.setSchulhaus("");
        return m;
    }

    @Test
    void testDefaultValues() {
        Penalty p = new Penalty();
        assertEquals(Penalty.LEER, p.getReihenfolge());
        assertEquals(Penalty.LEER, p.getReihenfolgeOrig());
        assertFalse(p.isGespielt());
        assertFalse(p.isBestaetigt());
    }

    @Test
    void testAddMannschaftInitial() {
        Penalty p = new Penalty();
        Mannschaft m = createMannschaft("", 1, 5);

        p.addMannschaftInitial(m);

        assertEquals(1, p.getRealFinalList().size());
        assertNotEquals(Penalty.LEER, p.getReihenfolgeOrig());
    }

    @Test
    void testAddMannschaft() {
        Penalty p = new Penalty();
        Mannschaft m = createMannschaft("", 1, 5);

        p.addMannschaft(m);

        assertEquals(1, p.getRealFinalList().size());
        assertNotEquals(Penalty.LEER, p.getReihenfolge());
    }

    @Test
    void testContains() {
        Penalty p = new Penalty();
        Mannschaft m = createMannschaft("", 1, 5);
        p.addMannschaftInitial(m);

        assertTrue(p.contains(m));
    }

    @Test
    void testContains_NotFound() {
        Penalty p = new Penalty();
        Mannschaft m1 = createMannschaft("", 1, 5);
        Mannschaft m2 = createMannschaft("", 2, 5);
        p.addMannschaftInitial(m1);

        assertFalse(p.contains(m2));
    }

    @Test
    void testGetRang_Gespielt() throws TurnierException {
        Penalty p = new Penalty();
        Mannschaft m1 = createMannschaft("", 1, 5);
        Mannschaft m2 = createMannschaft("", 2, 5);
        p.addMannschaftInitial(m1);
        p.addMannschaftInitial(m2);
        p.setGespielt(true);

        List<Mannschaft> finalList = p.getFinalList();
        int rang = p.getRang(m1);
        assertTrue(rang >= 1);
    }

    @Test
    void testGetRang_NotGespielt_Throws() {
        Penalty p = new Penalty();
        Mannschaft m = createMannschaft("", 1, 5);
        p.addMannschaftInitial(m);

        assertThrows(TurnierException.class, () -> p.getRang(m));
    }

    @Test
    void testSetFinalList() {
        Penalty p = new Penalty();
        Mannschaft m1 = createMannschaft("", 1, 5);
        Mannschaft m2 = createMannschaft("", 2, 5);

        p.setFinalList(List.of(m1, m2));

        assertEquals(2, p.getRealFinalList().size());
    }

    @Test
    void testToMannschaftsString_Empty() {
        Penalty p = new Penalty();
        String result = p.toMannschaftsString();
        assertTrue(result.contains("penalty ohne mannschaften"));
    }

    @Test
    void testToMannschaftsString_WithMannschaften() {
        Penalty p = new Penalty();
        Mannschaft m1 = createMannschaft("", 1, 5);
        Mannschaft m2 = createMannschaft("", 2, 5);
        p.addMannschaftInitial(m1);
        p.addMannschaftInitial(m2);

        String result = p.toMannschaftsString();
        assertFalse(result.isEmpty());
        assertFalse(result.contains("penalty ohne mannschaften"));
    }

    @Test
    void testSetAndGetKategorie() {
        Penalty p = new Penalty();
        Gruppe g = new Gruppe();
        p.setKategorie(g);
        assertSame(g, p.getKategorie());
    }

    @Test
    void testSetAndGetGr() {
        Penalty p = new Penalty();
        Gruppe g = new Gruppe();
        p.setGr(g);
        assertSame(g, p.getKategorie());
    }
}
