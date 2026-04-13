package ch.plaintext.schuetu.model;

import ch.plaintext.schuetu.model.enums.SpielPhasenEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SpielEinstellungenTest {

    @Test
    void testSetSpielPhaseString_A() {
        SpielEinstellungen e = new SpielEinstellungen();
        e.setSpielPhaseString("a_anmeldung");
        assertEquals(SpielPhasenEnum.A_ANMELDEPHASE, e.getPhase());
    }

    @Test
    void testSetSpielPhaseString_B() {
        SpielEinstellungen e = new SpielEinstellungen();
        e.setSpielPhaseString("b_kategorie");
        assertEquals(SpielPhasenEnum.B_KATEGORIE_ZUORDNUNG, e.getPhase());
    }

    @Test
    void testSetSpielPhaseString_C() {
        SpielEinstellungen e = new SpielEinstellungen();
        e.setSpielPhaseString("c_spieltage");
        assertEquals(SpielPhasenEnum.C_SPIELTAGE_DEFINIEREN, e.getPhase());
    }

    @Test
    void testSetSpielPhaseString_D() {
        SpielEinstellungen e = new SpielEinstellungen();
        e.setSpielPhaseString("d_spiele");
        assertEquals(SpielPhasenEnum.D_SPIELE_ZUORDNUNG, e.getPhase());
    }

    @Test
    void testSetSpielPhaseString_E() {
        SpielEinstellungen e = new SpielEinstellungen();
        e.setSpielPhaseString("e_spielbereit");
        assertEquals(SpielPhasenEnum.E_SPIELBEREIT, e.getPhase());
    }

    @Test
    void testSetSpielPhaseString_F() {
        SpielEinstellungen e = new SpielEinstellungen();
        e.setSpielPhaseString("f_spielen");
        assertEquals(SpielPhasenEnum.F_SPIELEN, e.getPhase());
    }

    @Test
    void testSetSpielPhaseString_G() {
        SpielEinstellungen e = new SpielEinstellungen();
        e.setSpielPhaseString("g_abgeschlossen");
        assertEquals(SpielPhasenEnum.G_ABGESCHLOSSEN, e.getPhase());
    }

    @Test
    void testDefaultValues() {
        SpielEinstellungen e = new SpielEinstellungen();
        assertEquals(SpielPhasenEnum.A_ANMELDEPHASE, e.getPhase());
        assertEquals(10, e.getSpiellaenge());
        assertEquals(13, e.getSpiellaengefinale());
        assertEquals(2, e.getPause());
        assertEquals(1, e.getVerschnellerungsFaktor());
        assertEquals(60, e.getAufholzeitInSekunden());
        assertFalse(e.isAutomatischesAufholen());
        assertFalse(e.isAutomatischesVorbereiten());
        assertFalse(e.isAbbrechenZulassen());
        assertFalse(e.isGongEinschalten());
        assertTrue(e.isBehandleFinaleProKlassebeiZusammengefuehrten());
        assertFalse(e.isMobileLinkOn());
        assertFalse(e.isMaster());
        assertFalse(e.isAnmeldephase());
    }

    @Test
    void testMasterField() {
        SpielEinstellungen e = new SpielEinstellungen();
        e.setMaster(true);
        assertTrue(e.isMaster());
    }

    @Test
    void testWebsiteSettings() {
        SpielEinstellungen e = new SpielEinstellungen();
        e.setWebsiteInMannschaftslistenmode(true);
        e.setWebsiteEnableDownloadLink(true);
        e.setWebsiteDownloadLink("http://download.test");
        e.setWebsiteTurnierTitel("Turnier 2026");
        e.setWebsiteEnableProgrammDownloadLink(true);
        e.setWebsiteProgrammDownloadLink("http://programm.test");

        assertTrue(e.isWebsiteInMannschaftslistenmode());
        assertTrue(e.getWebsiteInMannschaftslistenmode());
        assertTrue(e.isWebsiteEnableDownloadLink());
        assertEquals("http://download.test", e.getWebsiteDownloadLink());
        assertEquals("Turnier 2026", e.getWebsiteTurnierTitel());
        assertTrue(e.isWebsiteEnableProgrammDownloadLink());
        assertEquals("http://programm.test", e.getWebsiteProgrammDownloadLink());
    }

    @Test
    void testGroesser6Settings() {
        SpielEinstellungen e = new SpielEinstellungen();
        assertTrue(e.isGroesser6AufC());
        assertEquals(7, e.getGroesser6AufCMin());

        e.setGroesser6AufC(false);
        e.setGroesser6AufCMin(8);

        assertFalse(e.isGroesser6AufC());
        assertEquals(8, e.getGroesser6AufCMin());
    }

    @Test
    void testZweiPausenBisKlasse() {
        SpielEinstellungen e = new SpielEinstellungen();
        assertEquals(3, e.getZweiPausenBisKlasse());

        e.setZweiPausenBisKlasse(5);
        assertEquals(5, e.getZweiPausenBisKlasse());
    }
}
