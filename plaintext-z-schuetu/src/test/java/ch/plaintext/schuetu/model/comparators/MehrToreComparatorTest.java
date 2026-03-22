package ch.plaintext.schuetu.model.comparators;

import ch.plaintext.schuetu.entity.Mannschaft;
import ch.plaintext.schuetu.model.enums.GeschlechtEnum;
import ch.plaintext.schuetu.model.ranglistensortierung.RanglisteneintragZeile;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MehrToreComparatorTest {

    @Test
    void testCompare_MoreGoalsFirst() {
        RanglisteneintragZeile a = createZeile(10);
        RanglisteneintragZeile b = createZeile(5);

        MehrToreComparator comp = new MehrToreComparator();
        assertTrue(comp.compare(a, b) < 0);
    }

    @Test
    void testCompare_FewerGoalsSecond() {
        RanglisteneintragZeile a = createZeile(3);
        RanglisteneintragZeile b = createZeile(8);

        MehrToreComparator comp = new MehrToreComparator();
        assertTrue(comp.compare(a, b) > 0);
    }

    @Test
    void testCompare_Equal() {
        RanglisteneintragZeile a = createZeile(5);
        RanglisteneintragZeile b = createZeile(5);

        MehrToreComparator comp = new MehrToreComparator();
        assertEquals(0, comp.compare(a, b));
    }

    @Test
    void testSorting() {
        RanglisteneintragZeile a = createZeile(2);
        RanglisteneintragZeile b = createZeile(8);
        RanglisteneintragZeile c = createZeile(5);

        List<RanglisteneintragZeile> list = new ArrayList<>(List.of(a, b, c));
        list.sort(new MehrToreComparator());

        assertEquals(8, list.get(0).getToreErziehlt());
        assertEquals(5, list.get(1).getToreErziehlt());
        assertEquals(2, list.get(2).getToreErziehlt());
    }

    private RanglisteneintragZeile createZeile(int toreErziehlt) {
        RanglisteneintragZeile z = new RanglisteneintragZeile();
        z.setToreErziehlt(toreErziehlt);
        Mannschaft m = new Mannschaft();
        m.setGeschlecht(GeschlechtEnum.M);
        m.setKlasse(5);
        m.setTeamNummer(1);
        z.setMannschaft(m);
        return z;
    }
}
