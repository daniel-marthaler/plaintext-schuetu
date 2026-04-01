package ch.plaintext.schuetu.model.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SpielTageszeitTest {

    @Test
    void testEnumValues() {
        assertEquals(4, SpielTageszeit.values().length);
    }

    @Test
    void testValueOf_Samstagmorgen() {
        assertEquals(SpielTageszeit.SAMSTAGMORGEN, SpielTageszeit.valueOf("SAMSTAGMORGEN"));
    }

    @Test
    void testValueOf_Samstagnachmittag() {
        assertEquals(SpielTageszeit.SAMSTAGNACHMITTAG, SpielTageszeit.valueOf("SAMSTAGNACHMITTAG"));
    }

    @Test
    void testValueOf_Sonntagmorgen() {
        assertEquals(SpielTageszeit.SONNTAGMORGEN, SpielTageszeit.valueOf("SONNTAGMORGEN"));
    }

    @Test
    void testValueOf_Egal() {
        assertEquals(SpielTageszeit.EGAL, SpielTageszeit.valueOf("EGAL"));
    }

    @Test
    void testValueOf_Invalid() {
        assertThrows(IllegalArgumentException.class, () -> SpielTageszeit.valueOf("ABEND"));
    }
}
