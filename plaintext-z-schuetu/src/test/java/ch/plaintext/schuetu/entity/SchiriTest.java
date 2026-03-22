package ch.plaintext.schuetu.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SchiriTest {

    @Test
    void testGetShName() {
        Schiri schiri = new Schiri();
        schiri.setVorname("Hans");
        schiri.setNachname("Müller");

        assertEquals("Hans Müller", schiri.getShName());
    }

    @Test
    void testDefaultValues() {
        Schiri schiri = new Schiri();
        assertFalse(schiri.isAktiviert());
        assertEquals(0, schiri.getMatchcount());
        assertEquals("", schiri.getSpielIDs());
        assertNotNull(schiri.getCreationdate());
    }

    @Test
    void testSettersGetters() {
        Schiri schiri = new Schiri();
        schiri.setName("MuellerH");
        schiri.setVorname("Hans");
        schiri.setNachname("Müller");
        schiri.setEinteilung("Feld A");
        schiri.setAktiviert(true);
        schiri.setMatchcount(5);
        schiri.setSpielIDs("1,2,3");
        schiri.setGame("2024");

        assertEquals("MuellerH", schiri.getName());
        assertEquals("Hans", schiri.getVorname());
        assertEquals("Müller", schiri.getNachname());
        assertEquals("Feld A", schiri.getEinteilung());
        assertTrue(schiri.isAktiviert());
        assertEquals(5, schiri.getMatchcount());
        assertEquals("1,2,3", schiri.getSpielIDs());
        assertEquals("2024", schiri.getGame());
    }
}
