package ch.plaintext.schuetu.model.comparators;

import ch.plaintext.schuetu.entity.Mannschaft;
import ch.plaintext.schuetu.entity.Spiel;
import ch.plaintext.schuetu.model.enums.GeschlechtEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SpielMannschaftsnamenComparatorTest {

    private Mannschaft createMannschaft(int teamNr, int klasse, GeschlechtEnum geschlecht) {
        Mannschaft m = new Mannschaft();
        m.setTeamNummer(teamNr);
        m.setKlasse(klasse);
        m.setGeschlecht(geschlecht);
        m.setFarbe("");
        m.setSchulhaus("");
        return m;
    }

    @Test
    void testCompare_SameNames() {
        Mannschaft mA = createMannschaft(1, 5, GeschlechtEnum.M);
        Mannschaft mB = createMannschaft(2, 5, GeschlechtEnum.M);

        Spiel s1 = new Spiel();
        s1.setMannschaftA(mA);
        s1.setMannschaftB(mB);

        Spiel s2 = new Spiel();
        s2.setMannschaftA(mA);
        s2.setMannschaftB(mB);

        SpielMannschaftsnamenComparator comp = new SpielMannschaftsnamenComparator();
        assertEquals(0, comp.compare(s1, s2));
    }

    @Test
    void testCompare_DifferentNames() {
        Mannschaft mA1 = createMannschaft(1, 5, GeschlechtEnum.M);
        Mannschaft mB1 = createMannschaft(2, 5, GeschlechtEnum.M);

        Mannschaft mA2 = createMannschaft(3, 6, GeschlechtEnum.K);
        Mannschaft mB2 = createMannschaft(4, 6, GeschlechtEnum.K);

        Spiel s1 = new Spiel();
        s1.setMannschaftA(mA1);
        s1.setMannschaftB(mB1);

        Spiel s2 = new Spiel();
        s2.setMannschaftA(mA2);
        s2.setMannschaftB(mB2);

        SpielMannschaftsnamenComparator comp = new SpielMannschaftsnamenComparator();
        int result = comp.compare(s1, s2);
        assertNotEquals(0, result);
    }
}
