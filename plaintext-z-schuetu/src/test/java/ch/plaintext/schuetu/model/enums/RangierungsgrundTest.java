package ch.plaintext.schuetu.model.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RangierungsgrundTest {

    @Test
    void testAllValues() {
        Rangierungsgrund[] values = Rangierungsgrund.values();
        assertEquals(2, values.length);
    }

    @Test
    void testValueOf() {
        assertEquals(Rangierungsgrund.WEITERSUCHEN, Rangierungsgrund.valueOf("WEITERSUCHEN"));
        assertEquals(Rangierungsgrund.PENALTY, Rangierungsgrund.valueOf("PENALTY"));
    }
}
