package ch.plaintext.schuetu.model.comparators;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GenericSorterTest {

    // Helper class with a Comparable return type for testing
    public static class SortableItem {
        private final String name;
        private final Integer value;

        public SortableItem(String name, Integer value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public Integer getValue() {
            return value;
        }
    }

    @Test
    void testSortAsc_ByName() {
        List<SortableItem> list = new ArrayList<>(Arrays.asList(
                new SortableItem("Charlie", 3),
                new SortableItem("Alice", 1),
                new SortableItem("Bob", 2)
        ));

        GenericSorter.sortAsc(list, "getName");

        assertEquals("Alice", list.get(0).getName());
        assertEquals("Bob", list.get(1).getName());
        assertEquals("Charlie", list.get(2).getName());
    }

    @Test
    void testSortAsc_ByValue() {
        List<SortableItem> list = new ArrayList<>(Arrays.asList(
                new SortableItem("C", 30),
                new SortableItem("A", 10),
                new SortableItem("B", 20)
        ));

        GenericSorter.sortAsc(list, "getValue");

        assertEquals(10, list.get(0).getValue());
        assertEquals(20, list.get(1).getValue());
        assertEquals(30, list.get(2).getValue());
    }

    @Test
    void testSortDesc_ByName() {
        List<SortableItem> list = new ArrayList<>(Arrays.asList(
                new SortableItem("Alice", 1),
                new SortableItem("Charlie", 3),
                new SortableItem("Bob", 2)
        ));

        GenericSorter.sortDesc(list, "getName");

        assertEquals("Charlie", list.get(0).getName());
        assertEquals("Bob", list.get(1).getName());
        assertEquals("Alice", list.get(2).getName());
    }

    @Test
    void testSortDesc_ByValue() {
        List<SortableItem> list = new ArrayList<>(Arrays.asList(
                new SortableItem("A", 10),
                new SortableItem("C", 30),
                new SortableItem("B", 20)
        ));

        GenericSorter.sortDesc(list, "getValue");

        assertEquals(30, list.get(0).getValue());
        assertEquals(20, list.get(1).getValue());
        assertEquals(10, list.get(2).getValue());
    }

    @Test
    void testSortAsc_SingleElement() {
        List<SortableItem> list = new ArrayList<>(Arrays.asList(
                new SortableItem("Only", 1)
        ));

        GenericSorter.sortAsc(list, "getName");
        assertEquals(1, list.size());
        assertEquals("Only", list.get(0).getName());
    }

    @Test
    void testSortAsc_AlreadySorted() {
        List<SortableItem> list = new ArrayList<>(Arrays.asList(
                new SortableItem("Alice", 1),
                new SortableItem("Bob", 2),
                new SortableItem("Charlie", 3)
        ));

        GenericSorter.sortAsc(list, "getName");

        assertEquals("Alice", list.get(0).getName());
        assertEquals("Bob", list.get(1).getName());
        assertEquals("Charlie", list.get(2).getName());
    }
}
