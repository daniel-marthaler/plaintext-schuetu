package ch.plaintext.schuetu.model.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GeschlechtEnumTest {

    @Test
    void testEnumValues() {
        assertEquals(2, GeschlechtEnum.values().length);
    }

    @Test
    void testValueOf_M() {
        assertEquals(GeschlechtEnum.M, GeschlechtEnum.valueOf("M"));
    }

    @Test
    void testValueOf_K() {
        assertEquals(GeschlechtEnum.K, GeschlechtEnum.valueOf("K"));
    }

    @Test
    void testToString_M() {
        assertEquals("M", GeschlechtEnum.M.toString());
    }

    @Test
    void testToString_K() {
        assertEquals("K", GeschlechtEnum.K.toString());
    }

    @Test
    void testValueOf_Invalid() {
        assertThrows(IllegalArgumentException.class, () -> GeschlechtEnum.valueOf("X"));
    }
}
