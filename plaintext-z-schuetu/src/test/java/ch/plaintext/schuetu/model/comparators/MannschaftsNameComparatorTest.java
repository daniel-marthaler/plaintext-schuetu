package ch.plaintext.schuetu.model.comparators;

import ch.plaintext.schuetu.entity.Mannschaft;
import ch.plaintext.schuetu.model.enums.GeschlechtEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MannschaftsNameComparatorTest {

    private MannschaftsNameComparator comparator;

    @BeforeEach
    void setUp() {
        comparator = new MannschaftsNameComparator();
    }

    private Mannschaft createMannschaft(GeschlechtEnum geschlecht, int klasse, int teamNummer) {
        Mannschaft m = new Mannschaft();
        m.setGeschlecht(geschlecht);
        m.setKlasse(klasse);
        m.setTeamNummer(teamNummer);
        return m;
    }

    @Test
    void testCompare_SameName() {
        Mannschaft m1 = createMannschaft(GeschlechtEnum.M, 5, 1);
        Mannschaft m2 = createMannschaft(GeschlechtEnum.M, 5, 1);
        assertEquals(0, comparator.compare(m1, m2));
    }

    @Test
    void testCompare_DifferentGeschlecht() {
        Mannschaft m1 = createMannschaft(GeschlechtEnum.K, 5, 1);
        Mannschaft m2 = createMannschaft(GeschlechtEnum.M, 5, 1);
        // K < M alphabetically
        assertTrue(comparator.compare(m1, m2) < 0);
    }

    @Test
    void testCompare_DifferentKlasse() {
        Mannschaft m1 = createMannschaft(GeschlechtEnum.M, 3, 1);
        Mannschaft m2 = createMannschaft(GeschlechtEnum.M, 5, 1);
        assertTrue(comparator.compare(m1, m2) < 0);
    }

    @Test
    void testCompare_DifferentTeamNummer() {
        Mannschaft m1 = createMannschaft(GeschlechtEnum.M, 5, 1);
        Mannschaft m2 = createMannschaft(GeschlechtEnum.M, 5, 3);
        assertTrue(comparator.compare(m1, m2) < 0);
    }

    @Test
    void testSort_Multiple() {
        Mannschaft m1 = createMannschaft(GeschlechtEnum.M, 5, 3);
        Mannschaft m2 = createMannschaft(GeschlechtEnum.K, 3, 1);
        Mannschaft m3 = createMannschaft(GeschlechtEnum.M, 5, 1);

        List<Mannschaft> list = Arrays.asList(m1, m2, m3);
        list.sort(comparator);

        // K3 should be first (K < M), then M5 sorted by team number
        assertTrue(list.get(0).getName().startsWith("K"));
    }

    @Test
    void testSerializable() {
        // KategorieNameComparator implements Serializable
        assertTrue(comparator instanceof java.io.Serializable);
    }
}
