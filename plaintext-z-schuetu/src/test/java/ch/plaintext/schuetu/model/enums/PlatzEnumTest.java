package ch.plaintext.schuetu.model.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlatzEnumTest {

    @Test
    void testGetText() {
        assertEquals("A", PlatzEnum.A.getText());
        assertEquals("B", PlatzEnum.B.getText());
        assertEquals("C", PlatzEnum.C.getText());
        assertEquals("D", PlatzEnum.D.getText());
    }

    @Test
    void testFromString_Valid() {
        assertEquals(PlatzEnum.A, PlatzEnum.fromString("A"));
        assertEquals(PlatzEnum.B, PlatzEnum.fromString("B"));
        assertEquals(PlatzEnum.C, PlatzEnum.fromString("C"));
        assertEquals(PlatzEnum.D, PlatzEnum.fromString("D"));
    }

    @Test
    void testFromString_CaseInsensitive() {
        assertEquals(PlatzEnum.A, PlatzEnum.fromString("a"));
        assertEquals(PlatzEnum.B, PlatzEnum.fromString("b"));
    }

    @Test
    void testFromString_Null() {
        assertNull(PlatzEnum.fromString(null));
    }

    @Test
    void testFromString_Unknown() {
        assertNull(PlatzEnum.fromString("X"));
    }

    @Test
    void testEnumValues() {
        assertEquals(4, PlatzEnum.values().length);
    }
}
