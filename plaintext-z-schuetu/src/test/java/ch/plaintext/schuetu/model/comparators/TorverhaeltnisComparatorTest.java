package ch.plaintext.schuetu.model.comparators;

import ch.plaintext.schuetu.entity.Mannschaft;
import ch.plaintext.schuetu.model.enums.GeschlechtEnum;
import ch.plaintext.schuetu.model.ranglistensortierung.RanglisteneintragZeile;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TorverhaeltnisComparatorTest {

    @Test
    void testCompare_HigherDifferenceFirst() {
        RanglisteneintragZeile a = createZeile(10, 3); // diff = 7
        RanglisteneintragZeile b = createZeile(5, 4);  // diff = 1

        TorverhaeltnisComparator comp = new TorverhaeltnisComparator();
        assertTrue(comp.compare(a, b) < 0);
    }

    @Test
    void testCompare_LowerDifferenceSecond() {
        RanglisteneintragZeile a = createZeile(3, 5); // diff = -2
        RanglisteneintragZeile b = createZeile(8, 2); // diff = 6

        TorverhaeltnisComparator comp = new TorverhaeltnisComparator();
        assertTrue(comp.compare(a, b) > 0);
    }

    @Test
    void testCompare_Equal() {
        RanglisteneintragZeile a = createZeile(5, 2); // diff = 3
        RanglisteneintragZeile b = createZeile(6, 3); // diff = 3

        TorverhaeltnisComparator comp = new TorverhaeltnisComparator();
        assertEquals(0, comp.compare(a, b));
    }

    @Test
    void testSorting() {
        RanglisteneintragZeile a = createZeile(2, 5);  // diff = -3
        RanglisteneintragZeile b = createZeile(8, 1);  // diff = 7
        RanglisteneintragZeile c = createZeile(4, 4);  // diff = 0

        List<RanglisteneintragZeile> list = new ArrayList<>(List.of(a, b, c));
        list.sort(new TorverhaeltnisComparator());

        assertEquals(7, list.get(0).getTordifferenz());
        assertEquals(0, list.get(1).getTordifferenz());
        assertEquals(-3, list.get(2).getTordifferenz());
    }

    private RanglisteneintragZeile createZeile(int erzielt, int kassiert) {
        RanglisteneintragZeile z = new RanglisteneintragZeile();
        z.setToreErziehlt(erzielt);
        z.setToreKassiert(kassiert);
        Mannschaft m = new Mannschaft();
        m.setGeschlecht(GeschlechtEnum.M);
        m.setKlasse(5);
        m.setTeamNummer(1);
        z.setMannschaft(m);
        return z;
    }
}
