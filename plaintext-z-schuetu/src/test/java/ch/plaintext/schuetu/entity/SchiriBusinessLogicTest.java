package ch.plaintext.schuetu.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SchiriBusinessLogicTest {

    @Test
    void testGetShName() {
        Schiri s = new Schiri();
        s.setVorname("Max");
        s.setNachname("Muster");

        assertEquals("Max Muster", s.getShName());
    }

    @Test
    void testDefaultValues() {
        Schiri s = new Schiri();
        assertFalse(s.isAktiviert());
        assertEquals(0, s.getMatchcount());
        assertEquals("", s.getSpielIDs());
        assertNotNull(s.getCreationdate());
    }

    @Test
    void testSetAndGetFields() {
        Schiri s = new Schiri();
        s.setGame("TestTurnier");
        s.setAktiviert(true);
        s.setMatchcount(5);
        s.setTelefon("031 123 45 67");
        s.setEinteilung("A");
        s.setPasswordHash("abc123");
        s.setLoginName("mmuster");

        assertEquals("TestTurnier", s.getGame());
        assertTrue(s.isAktiviert());
        assertEquals(5, s.getMatchcount());
        assertEquals("031 123 45 67", s.getTelefon());
        assertEquals("A", s.getEinteilung());
        assertEquals("abc123", s.getPasswordHash());
        assertEquals("mmuster", s.getLoginName());
    }
}
