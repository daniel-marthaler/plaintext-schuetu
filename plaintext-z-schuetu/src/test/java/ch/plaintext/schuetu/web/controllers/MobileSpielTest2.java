package ch.plaintext.schuetu.web.controllers;

import ch.plaintext.schuetu.entity.Spiel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MobileSpielTest2 {

    @Test
    void testDefaultValues() {
        MobileSpiel ms = new MobileSpiel();
        assertTrue(ms.stehtBevor);
        assertEquals("", ms.getZeile());
        assertFalse(ms.isVerloren());
        assertFalse(ms.isAmSpielen());
        assertNull(ms.getSpiel());
        assertNull(ms.getColor());
        assertNull(ms.getStart());
        assertNull(ms.getPlatz());
        assertNull(ms.getGegner());
        assertNull(ms.getResultat());
    }

    @Test
    void testSetAndGetFields() {
        MobileSpiel ms = new MobileSpiel();
        Spiel spiel = new Spiel();

        ms.setZeile("08:30");
        ms.setColor("green");
        ms.setStart("Sa 08:30");
        ms.setPlatz("A");
        ms.setGegner("M502");
        ms.setVerloren(true);
        ms.setResultat("3:1");
        ms.setAmSpielen(true);
        ms.setSpiel(spiel);
        ms.stehtBevor = false;

        assertEquals("08:30", ms.getZeile());
        assertEquals("green", ms.getColor());
        assertEquals("Sa 08:30", ms.getStart());
        assertEquals("A", ms.getPlatz());
        assertEquals("M502", ms.getGegner());
        assertTrue(ms.isVerloren());
        assertEquals("3:1", ms.getResultat());
        assertTrue(ms.isAmSpielen());
        assertSame(spiel, ms.getSpiel());
        assertFalse(ms.stehtBevor);
    }
}
