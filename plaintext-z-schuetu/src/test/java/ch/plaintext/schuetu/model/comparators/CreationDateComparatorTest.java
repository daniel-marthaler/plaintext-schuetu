package ch.plaintext.schuetu.model.comparators;

import ch.plaintext.schuetu.model.CreationDateProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CreationDateComparatorTest {

    private CreationDateComparator comparator;

    @BeforeEach
    void setUp() {
        comparator = new CreationDateComparator();
    }

    private CreationDateProvider createProvider(long timeMillis) {
        return () -> new Date(timeMillis);
    }

    @Test
    void testCompare_FirstBefore() {
        CreationDateProvider p1 = createProvider(1000);
        CreationDateProvider p2 = createProvider(2000);
        assertEquals(-1, comparator.compare(p1, p2));
    }

    @Test
    void testCompare_FirstAfter() {
        CreationDateProvider p1 = createProvider(2000);
        CreationDateProvider p2 = createProvider(1000);
        assertEquals(1, comparator.compare(p1, p2));
    }

    @Test
    void testCompare_SameDate() {
        CreationDateProvider p1 = createProvider(1000);
        CreationDateProvider p2 = createProvider(1000);
        assertEquals(0, comparator.compare(p1, p2));
    }

    @Test
    void testSort() {
        CreationDateProvider p1 = createProvider(3000);
        CreationDateProvider p2 = createProvider(1000);
        CreationDateProvider p3 = createProvider(2000);

        List<CreationDateProvider> list = Arrays.asList(p1, p2, p3);
        list.sort(comparator);

        assertEquals(1000, list.get(0).getCreationdate().getTime());
        assertEquals(2000, list.get(1).getCreationdate().getTime());
        assertEquals(3000, list.get(2).getCreationdate().getTime());
    }

    @Test
    void testCompare_DistantDates() {
        // Jan 1, 2000 vs Jan 1, 2026
        CreationDateProvider p1 = createProvider(946684800000L);
        CreationDateProvider p2 = createProvider(1767225600000L);
        assertTrue(comparator.compare(p1, p2) < 0);
        assertTrue(comparator.compare(p2, p1) > 0);
    }
}
