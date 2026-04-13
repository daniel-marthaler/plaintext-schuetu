package ch.plaintext.schuetu.model.comparators;

import ch.plaintext.schuetu.entity.Mannschaft;
import ch.plaintext.schuetu.entity.Penalty;
import ch.plaintext.schuetu.model.enums.GeschlechtEnum;
import ch.plaintext.schuetu.model.ranglistensortierung.RanglisteneintragZeile;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PenaltyComparatorTest {

    private Mannschaft createMannschaft(int teamNr) {
        Mannschaft m = new Mannschaft();
        m.setTeamNummer(teamNr);
        m.setKlasse(5);
        m.setGeschlecht(GeschlechtEnum.M);
        m.setFarbe("");
        m.setSchulhaus("");
        return m;
    }

    @Test
    void testCompare_FirstBetter() {
        Mannschaft m1 = createMannschaft(1);
        Mannschaft m2 = createMannschaft(2);

        Penalty p = new Penalty();
        p.addMannschaftInitial(m1);
        p.addMannschaftInitial(m2);
        p.setGespielt(true);

        RanglisteneintragZeile z1 = new RanglisteneintragZeile();
        z1.setMannschaft(m1);
        RanglisteneintragZeile z2 = new RanglisteneintragZeile();
        z2.setMannschaft(m2);

        PenaltyComparator comp = new PenaltyComparator(p);
        assertTrue(comp.compare(z1, z2) < 0);
    }

    @Test
    void testCompare_SecondBetter() {
        Mannschaft m1 = createMannschaft(1);
        Mannschaft m2 = createMannschaft(2);

        Penalty p = new Penalty();
        p.addMannschaftInitial(m1);
        p.addMannschaftInitial(m2);
        p.setGespielt(true);

        RanglisteneintragZeile z1 = new RanglisteneintragZeile();
        z1.setMannschaft(m2);
        RanglisteneintragZeile z2 = new RanglisteneintragZeile();
        z2.setMannschaft(m1);

        PenaltyComparator comp = new PenaltyComparator(p);
        assertTrue(comp.compare(z1, z2) > 0);
    }

    @Test
    void testCompare_Same() {
        Mannschaft m1 = createMannschaft(1);

        Penalty p = new Penalty();
        p.addMannschaftInitial(m1);
        p.setGespielt(true);

        RanglisteneintragZeile z1 = new RanglisteneintragZeile();
        z1.setMannschaft(m1);
        RanglisteneintragZeile z2 = new RanglisteneintragZeile();
        z2.setMannschaft(m1);

        PenaltyComparator comp = new PenaltyComparator(p);
        assertEquals(0, comp.compare(z1, z2));
    }
}
