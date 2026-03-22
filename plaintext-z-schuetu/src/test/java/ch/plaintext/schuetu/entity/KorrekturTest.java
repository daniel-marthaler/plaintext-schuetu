package ch.plaintext.schuetu.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KorrekturTest {

    @Test
    void testDefaultValues() {
        Korrektur k = new Korrektur();
        assertNull(k.getTyp());
        assertNull(k.getWert());
        assertNotNull(k.getCreationdate());
    }

    @Test
    void testSettersGetters() {
        Korrektur k = new Korrektur();
        k.setTyp("KATEGORIE");
        k.setWert("M5 -> M6");
        k.setGame("2024");
        k.setReihenfolge(5);

        assertEquals("KATEGORIE", k.getTyp());
        assertEquals("M5 -> M6", k.getWert());
        assertEquals("2024", k.getGame());
        assertEquals(5, k.getReihenfolge());
    }
}
