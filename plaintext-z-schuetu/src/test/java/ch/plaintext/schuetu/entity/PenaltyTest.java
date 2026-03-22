package ch.plaintext.schuetu.entity;

import ch.plaintext.schuetu.model.TurnierException;
import ch.plaintext.schuetu.model.enums.GeschlechtEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PenaltyTest {

    private Penalty penalty;

    @BeforeEach
    void setUp() {
        penalty = new Penalty();
    }

    @Test
    void testDefaultReihenfolge() {
        assertEquals(Penalty.LEER, penalty.getReihenfolge());
        assertEquals(Penalty.LEER, penalty.getReihenfolgeOrig());
    }

    @Test
    void testAddMannschaftInitial() {
        Mannschaft m1 = createMannschaft(1, "M5");
        Mannschaft m2 = createMannschaft(2, "M5");

        penalty.addMannschaftInitial(m1);
        penalty.addMannschaftInitial(m2);

        assertEquals(2, penalty.getRealFinalList().size());
        assertTrue(penalty.getReihenfolgeOrig().contains(m1.getNameNoNickname()));
    }

    @Test
    void testAddMannschaft() {
        Mannschaft m1 = createMannschaft(1, "M5");

        penalty.addMannschaft(m1);

        assertEquals(1, penalty.getRealFinalList().size());
        assertFalse(penalty.getReihenfolge().equals(Penalty.LEER));
    }

    @Test
    void testContains_True() {
        Mannschaft m = createMannschaft(1, "M5");
        penalty.addMannschaftInitial(m);

        assertTrue(penalty.contains(m));
    }

    @Test
    void testContains_False() {
        Mannschaft m1 = createMannschaft(1, "M5");
        Mannschaft m2 = createMannschaft(2, "M5");
        penalty.addMannschaftInitial(m1);

        assertFalse(penalty.contains(m2));
    }

    @Test
    void testGetRang_NotGespielt() {
        Mannschaft m = createMannschaft(1, "M5");
        penalty.addMannschaftInitial(m);
        penalty.setGespielt(false);

        assertThrows(TurnierException.class, () -> penalty.getRang(m));
    }

    @Test
    void testGetRang_Gespielt() throws TurnierException {
        Mannschaft m1 = createMannschaft(1, "M5");
        Mannschaft m2 = createMannschaft(2, "M5");

        penalty.addMannschaftInitial(m1);
        penalty.addMannschaftInitial(m2);
        penalty.setGespielt(true);

        int rang = penalty.getRang(m1);
        assertTrue(rang >= 1);
    }

    @Test
    void testDefaultFlags() {
        assertFalse(penalty.isGespielt());
        assertFalse(penalty.isBestaetigt());
    }

    @Test
    void testSetGr() {
        Gruppe g = new Gruppe();
        penalty.setGr(g);
        assertEquals(g, penalty.getKategorie());
    }

    private Mannschaft createMannschaft(int nummer, String prefix) {
        Mannschaft m = new Mannschaft();
        m.setTeamNummer(nummer);
        m.setKlasse(5);
        m.setGeschlecht(GeschlechtEnum.M);
        return m;
    }
}
