package ch.plaintext.schuetu.model.comparators;

import ch.plaintext.schuetu.entity.Mannschaft;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class MannschaftsCreationDateComparatorTest {

    @Test
    void testCompare_Earlier() {
        Mannschaft m1 = new Mannschaft();
        m1.setCreationdate(new Date(1000));
        Mannschaft m2 = new Mannschaft();
        m2.setCreationdate(new Date(2000));

        MannschaftsCreationDateComparator comp = new MannschaftsCreationDateComparator();
        assertTrue(comp.compare(m1, m2) < 0);
    }

    @Test
    void testCompare_Later() {
        Mannschaft m1 = new Mannschaft();
        m1.setCreationdate(new Date(2000));
        Mannschaft m2 = new Mannschaft();
        m2.setCreationdate(new Date(1000));

        MannschaftsCreationDateComparator comp = new MannschaftsCreationDateComparator();
        assertTrue(comp.compare(m1, m2) > 0);
    }

    @Test
    void testCompare_Same() {
        Date date = new Date(1000);
        Mannschaft m1 = new Mannschaft();
        m1.setCreationdate(date);
        Mannschaft m2 = new Mannschaft();
        m2.setCreationdate(date);

        MannschaftsCreationDateComparator comp = new MannschaftsCreationDateComparator();
        assertEquals(0, comp.compare(m1, m2));
    }
}
