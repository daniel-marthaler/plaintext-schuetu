package ch.plaintext.schuetu.model;

import ch.plaintext.schuetu.model.enums.SpielPhasenEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for SpielEinstellungen (the primary implementation of Einstellungen interface)
 */
class EinstellungenTest {

    private SpielEinstellungen einstellungen;

    @BeforeEach
    void setUp() {
        einstellungen = new SpielEinstellungen();
    }

    @Test
    void testDefaultValues() {
        assertEquals(SpielPhasenEnum.A_ANMELDEPHASE, einstellungen.getPhase());
        assertEquals(2, einstellungen.getPause());
        assertEquals(10, einstellungen.getSpiellaenge());
        assertEquals(13, einstellungen.getSpiellaengefinale());
        assertEquals(1, einstellungen.getVerschnellerungsFaktor());
        assertEquals(60, einstellungen.getAufholzeitInSekunden());
        assertFalse(einstellungen.isAutomatischesAufholen());
        assertFalse(einstellungen.isAutomatischesAnsagen());
        assertFalse(einstellungen.isAutomatischesVorbereiten());
        assertFalse(einstellungen.isAbbrechenZulassen());
        assertFalse(einstellungen.isGongEinschalten());
        assertTrue(einstellungen.isBehandleFinaleProKlassebeiZusammengefuehrten());
    }

    @Test
    void testSetSpielPhaseString_A() {
        einstellungen.setSpielPhaseString("A_ANMELDEPHASE");
        assertEquals(SpielPhasenEnum.A_ANMELDEPHASE, einstellungen.getPhase());
    }

    @Test
    void testSetSpielPhaseString_F() {
        einstellungen.setSpielPhaseString("F_SPIELEN");
        assertEquals(SpielPhasenEnum.F_SPIELEN, einstellungen.getPhase());
    }

    @Test
    void testSetSpielPhaseString_G() {
        einstellungen.setSpielPhaseString("G_ABGESCHLOSSEN");
        assertEquals(SpielPhasenEnum.G_ABGESCHLOSSEN, einstellungen.getPhase());
    }

    @Test
    void testPauseGetterSetter() {
        einstellungen.setPause(5);
        assertEquals(5, einstellungen.getPause());
    }

    @Test
    void testSpiellaengeGetterSetter() {
        einstellungen.setSpiellaenge(15);
        assertEquals(15, einstellungen.getSpiellaenge());
    }

    @Test
    void testStarttag() {
        Date now = new Date();
        einstellungen.setStarttag(now);
        assertEquals(now, einstellungen.getStarttag());
    }

    @Test
    void testWebsiteSettings() {
        einstellungen.setWebsiteInMannschaftslistenmode(true);
        assertTrue(einstellungen.isWebsiteInMannschaftslistenmode());
        assertTrue(einstellungen.getWebsiteInMannschaftslistenmode());

        einstellungen.setWebsiteEnableDownloadLink(true);
        assertTrue(einstellungen.isWebsiteEnableDownloadLink());

        einstellungen.setWebsiteDownloadLink("http://example.com");
        assertEquals("http://example.com", einstellungen.getWebsiteDownloadLink());

        einstellungen.setWebsiteTurnierTitel("Turnier 2026");
        assertEquals("Turnier 2026", einstellungen.getWebsiteTurnierTitel());
    }
}
