package ch.plaintext.schuetu.model.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SpielEnumTest {

    @Test
    void testGetText() {
        assertEquals("GRUPPE", SpielEnum.GRUPPE.getText());
        assertEquals("GFINAL", SpielEnum.GFINAL.getText());
        assertEquals("KFINAL", SpielEnum.KFINAL.getText());
    }

    @Test
    void testFromString_Valid() {
        assertEquals(SpielEnum.GRUPPE, SpielEnum.fromString("GRUPPE"));
        assertEquals(SpielEnum.GFINAL, SpielEnum.fromString("GFINAL"));
        assertEquals(SpielEnum.KFINAL, SpielEnum.fromString("KFINAL"));
    }

    @Test
    void testFromString_CaseInsensitive() {
        assertEquals(SpielEnum.GRUPPE, SpielEnum.fromString("gruppe"));
        assertEquals(SpielEnum.GFINAL, SpielEnum.fromString("gfinal"));
    }

    @Test
    void testFromString_Null() {
        assertNull(SpielEnum.fromString(null));
    }

    @Test
    void testFromString_Unknown() {
        assertNull(SpielEnum.fromString("UNKNOWN"));
    }

    @Test
    void testEnumValues() {
        assertEquals(3, SpielEnum.values().length);
    }
}
