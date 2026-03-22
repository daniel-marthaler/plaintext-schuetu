package ch.plaintext.schuetu.web.controllers;

import ch.plaintext.schuetu.entity.Spiel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MobileSpielTest {

    @Test
    void testDefaultValues() {
        MobileSpiel ms = new MobileSpiel();
        assertTrue(ms.isStehtBevor());
        assertEquals("", ms.getZeile());
        assertFalse(ms.isVerloren());
        assertFalse(ms.isAmSpielen());
    }

    @Test
    void testSettersGetters() {
        MobileSpiel ms = new MobileSpiel();
        ms.setStehtBevor(false);
        ms.setZeile("10:30");
        ms.setColor("green");
        ms.setStart("Sa 10:30");
        ms.setPlatz("A");
        ms.setGegner("M502");
        ms.setVerloren(true);
        ms.setResultat("3:1");
        ms.setAmSpielen(true);

        Spiel spiel = new Spiel();
        ms.setSpiel(spiel);

        assertFalse(ms.isStehtBevor());
        assertEquals("10:30", ms.getZeile());
        assertEquals("green", ms.getColor());
        assertEquals("Sa 10:30", ms.getStart());
        assertEquals("A", ms.getPlatz());
        assertEquals("M502", ms.getGegner());
        assertTrue(ms.isVerloren());
        assertEquals("3:1", ms.getResultat());
        assertTrue(ms.isAmSpielen());
        assertSame(spiel, ms.getSpiel());
    }
}
