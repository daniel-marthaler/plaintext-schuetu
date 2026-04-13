package ch.plaintext.schuetu.entity;

import ch.plaintext.schuetu.model.enums.SpielPhasenEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameModelTest2 {

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
    void testGetPhase_Abgeschlossen() {
        GameModel gm = new GameModel();
        gm.setSpielPhase("fertig");
        assertEquals(SpielPhasenEnum.G_ABGESCHLOSSEN, gm.getPhase());
    }

    @Test
    void testIsAnmeldephase() {
        GameModel gm = new GameModel();
        gm.setSpielPhase("anmeldung");
        assertTrue(gm.isAnmeldephase());
    }

    @Test
    void testIsNotAnmeldephase() {
        GameModel gm = new GameModel();
        gm.setSpielPhase("spielen");
        assertFalse(gm.isAnmeldephase());
    }

    @Test
    void testDefaultValues() {
        GameModel gm = new GameModel();
        assertEquals("anmeldung", gm.getSpielPhase());
        assertEquals(10, gm.getSpiellaenge());
        assertEquals(13, gm.getSpiellaengefinale());
        assertEquals(2, gm.getPause());
        assertEquals(1, gm.getVerschnellerungsFaktor());
        assertEquals(60, gm.getAufholzeitInSekunden());
        assertFalse(gm.isAutomatischesAufholen());
        assertFalse(gm.isAutomatischesVorbereiten());
        assertFalse(gm.isAutomatischesAnsagen());
        assertFalse(gm.isAbbrechenZulassen());
        assertFalse(gm.isGongEinschalten());
        assertTrue(gm.isBehandleFinaleProKlassebeiZusammengefuehrten());
        assertFalse(gm.isWebsiteInMannschaftslistenmode());
        assertFalse(gm.isWebsiteEnableDownloadLink());
        assertFalse(gm.isMobileLinkOn());
        assertFalse(gm.isBackportSyncOn());
        assertFalse(gm.isUploadOn());
        assertEquals(3, gm.getZweiPausenBisKlasse());
        assertTrue(gm.isGroesser6AufC());
        assertEquals(6, gm.getGroesser6AufCMin());
    }

    @Test
    void testWebsiteSettings() {
        GameModel gm = new GameModel();
        gm.setWebsiteTurnierTitel("Schuelerturnierworb 2026");
        gm.setWebsiteFixString("fix");
        gm.setWebsiteInMannschaftslistenmode(true);
        gm.setWebsiteEnableDownloadLink(true);

        assertEquals("Schuelerturnierworb 2026", gm.getWebsiteTurnierTitel());
        assertEquals("fix", gm.getWebsiteFixString());
        assertTrue(gm.isWebsiteInMannschaftslistenmode());
        assertTrue(gm.getWebsiteInMannschaftslistenmode());
    }

    @Test
    void testMobileSettings() {
        GameModel gm = new GameModel();
        gm.setMobileLinkOn(true);
        gm.setMobileLink("http://mobile.test");

        assertTrue(gm.isMobileLinkOn());
        assertEquals("http://mobile.test", gm.getMobileLink());
    }

    @Test
    void testBackportSettings() {
        GameModel gm = new GameModel();
        gm.setBackportSync("sync-url");
        gm.setBackportSyncOn(true);

        assertEquals("sync-url", gm.getBackportSync());
        assertTrue(gm.isBackportSyncOn());
    }
}
