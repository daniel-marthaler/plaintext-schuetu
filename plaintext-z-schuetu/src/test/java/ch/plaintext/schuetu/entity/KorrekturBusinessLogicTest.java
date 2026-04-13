package ch.plaintext.schuetu.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KorrekturBusinessLogicTest {

    @Test
    void testDefaultValues() {
        Korrektur k = new Korrektur();
        assertNull(k.getTyp());
        assertNull(k.getWert());
        assertEquals(0, k.getReihenfolge());
        assertNotNull(k.getCreationdate());
    }

    @Test
    void testSetAndGet() {
        Korrektur k = new Korrektur();
        k.setGame("TestTurnier");
        k.setTyp("spielzeile");
        k.setWert("42");
        k.setReihenfolge(3);

        assertEquals("TestTurnier", k.getGame());
        assertEquals("spielzeile", k.getTyp());
        assertEquals("42", k.getWert());
        assertEquals(3, k.getReihenfolge());
    }
}
