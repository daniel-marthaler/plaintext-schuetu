package ch.plaintext.schuetu.model.comparators;

import ch.plaintext.schuetu.entity.Spiel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SpielZeitComparatorTest {

    private SpielZeitComparator comparator;

    @BeforeEach
    void setUp() {
        comparator = new SpielZeitComparator();
    }

    @Test
    void testCompare_FirstBeforeSecond() {
        Spiel s1 = new Spiel();
        s1.setStart(new Date(1000));
        Spiel s2 = new Spiel();
        s2.setStart(new Date(2000));

        assertEquals(-1, comparator.compare(s1, s2));
    }

    @Test
    void testCompare_FirstAfterSecond() {
        Spiel s1 = new Spiel();
        s1.setStart(new Date(2000));
        Spiel s2 = new Spiel();
        s2.setStart(new Date(1000));

        assertEquals(1, comparator.compare(s1, s2));
    }

    @Test
    void testCompare_SameTime() {
        Spiel s1 = new Spiel();
        s1.setStart(new Date(1000));
        Spiel s2 = new Spiel();
        s2.setStart(new Date(1000));

        assertEquals(0, comparator.compare(s1, s2));
    }

    @Test
    void testCompare_NullStart() {
        Spiel s1 = new Spiel();
        s1.setStart(null);
        Spiel s2 = new Spiel();
        s2.setStart(new Date(1000));

        assertEquals(0, comparator.compare(s1, s2));
    }

    @Test
    void testCompare_NullSpiel() {
        Spiel s1 = new Spiel();
        s1.setStart(new Date(1000));

        assertEquals(0, comparator.compare(null, s1));
    }

    @Test
    void testSort() {
        Spiel s1 = new Spiel();
        s1.setStart(new Date(3000));
        Spiel s2 = new Spiel();
        s2.setStart(new Date(1000));
        Spiel s3 = new Spiel();
        s3.setStart(new Date(2000));

        List<Spiel> list = Arrays.asList(s1, s2, s3);
        list.sort(comparator);

        assertEquals(1000, list.get(0).getStart().getTime());
        assertEquals(2000, list.get(1).getStart().getTime());
        assertEquals(3000, list.get(2).getStart().getTime());
    }
}
