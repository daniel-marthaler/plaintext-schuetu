package ch.plaintext.schuetu.model.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AnredeEnumTest {

    @Test
    void testGetText() {
        assertEquals("Frau", AnredeEnum.FRAU.getText());
        assertEquals("Herr", AnredeEnum.HERR.getText());
        assertEquals("An", AnredeEnum.AN.getText());
    }

    @Test
    void testFromString_Valid() {
        assertEquals(AnredeEnum.FRAU, AnredeEnum.fromString("Frau"));
        assertEquals(AnredeEnum.HERR, AnredeEnum.fromString("Herr"));
        assertEquals(AnredeEnum.AN, AnredeEnum.fromString("An"));
    }

    @Test
    void testFromString_CaseInsensitive() {
        assertEquals(AnredeEnum.FRAU, AnredeEnum.fromString("frau"));
        assertEquals(AnredeEnum.HERR, AnredeEnum.fromString("herr"));
        assertEquals(AnredeEnum.AN, AnredeEnum.fromString("an"));
    }

    @Test
    void testFromString_Null() {
        assertEquals(AnredeEnum.AN, AnredeEnum.fromString(null));
    }

    @Test
    void testFromString_Unknown() {
        assertEquals(AnredeEnum.AN, AnredeEnum.fromString("Unknown"));
    }

    @Test
    void testEnumValues() {
        assertEquals(3, AnredeEnum.values().length);
    }
}
