package ch.plaintext.schuetu.service.websiteinfo.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MannschaftsComperatorTest {

    @Test
    void testCompare_Same() {
        Mannschaft m1 = new Mannschaft();
        m1.setKlasse("5");
        Mannschaft m2 = new Mannschaft();
        m2.setKlasse("5");

        MannschaftsComperator comp = new MannschaftsComperator();
        assertEquals(0, comp.compare(m1, m2));
    }

    @Test
    void testCompare_Lower() {
        Mannschaft m1 = new Mannschaft();
        m1.setKlasse("4");
        Mannschaft m2 = new Mannschaft();
        m2.setKlasse("5");

        MannschaftsComperator comp = new MannschaftsComperator();
        assertTrue(comp.compare(m1, m2) < 0);
    }

    @Test
    void testCompare_Higher() {
        Mannschaft m1 = new Mannschaft();
        m1.setKlasse("6");
        Mannschaft m2 = new Mannschaft();
        m2.setKlasse("5");

        MannschaftsComperator comp = new MannschaftsComperator();
        assertTrue(comp.compare(m1, m2) > 0);
    }
}
