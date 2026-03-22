package ch.plaintext.schuetu.entity;

import ch.plaintext.schuetu.model.enums.SpielPhasenEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameModelTest {

    @Test
    void testGetPhase_Anmeldung() {
        GameModel gm = new GameModel();
        gm.setSpielPhase("anmeldung");
        assertEquals(SpielPhasenEnum.A_ANMELDEPHASE, gm.getPhase());
    }

    @Test
    void testGetPhase_Kategorie() {
        GameModel gm = new GameModel();
        gm.setSpielPhase("kategorie");
        assertEquals(SpielPhasenEnum.B_KATEGORIE_ZUORDNUNG, gm.getPhase());
    }

    @Test
    void testGetPhase_Spieltage() {
        GameModel gm = new GameModel();
        gm.setSpielPhase("spieltage");
        assertEquals(SpielPhasenEnum.C_SPIELTAGE_DEFINIEREN, gm.getPhase());
    }

    @Test
    void testGetPhase_Spielen() {
        GameModel gm = new GameModel();
        gm.setSpielPhase("spielen");
        assertEquals(SpielPhasenEnum.F_SPIELEN, gm.getPhase());
    }

    @Test
    void testGetPhase_Unknown() {
        GameModel gm = new GameModel();
        gm.setSpielPhase("abgeschlossen");
        assertEquals(SpielPhasenEnum.G_ABGESCHLOSSEN, gm.getPhase());
    }

    @Test
    void testIsAnmeldephase_True() {
        GameModel gm = new GameModel();
        gm.setSpielPhase("anmeldung");
        assertTrue(gm.isAnmeldephase());
    }

    @Test
    void testIsAnmeldephase_False() {
        GameModel gm = new GameModel();
        gm.setSpielPhase("spielen");
        assertFalse(gm.isAnmeldephase());
    }

    @Test
    void testDefaultValues() {
        GameModel gm = new GameModel();
        assertEquals("anmeldung", gm.getSpielPhase());
        assertEquals(2, gm.getPause());
        assertEquals(10, gm.getSpiellaenge());
        assertEquals(13, gm.getSpiellaengefinale());
        assertEquals(60, gm.getAufholzeitInSekunden());
        assertEquals(1, gm.getVerschnellerungsFaktor());
        assertFalse(gm.isAutomatischesAufholen());
        assertFalse(gm.isAutomatischesVorbereiten());
        assertFalse(gm.isAutomatischesAnsagen());
        assertFalse(gm.isAbbrechenZulassen());
        assertFalse(gm.isGongEinschalten());
        assertTrue(gm.isBehandleFinaleProKlassebeiZusammengefuehrten());
        assertFalse(gm.isWebsiteInMannschaftslistenmode());
        assertEquals("https://www.schuelerturnierworb.ch/", gm.getWebsiteUrl());
        assertEquals("nichts", gm.getWebsiteTurnierMeldung());
        assertEquals(3, gm.getZweiPausenBisKlasse());
        assertTrue(gm.isGroesser6AufC());
    }
}
