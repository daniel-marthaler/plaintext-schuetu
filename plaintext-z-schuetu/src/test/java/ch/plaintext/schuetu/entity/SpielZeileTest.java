package ch.plaintext.schuetu.entity;

import ch.plaintext.schuetu.model.enums.SpielZeilenPhaseEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SpielZeileTest {

    private SpielZeile zeile;

    @BeforeEach
    void setUp() throws ParseException {
        zeile = new SpielZeile();
        SimpleDateFormat fmt = new SimpleDateFormat("HH:mm");
        zeile.setStart(fmt.parse("10:30"));
    }

    // === checkEmty ===

    @Test
    void testCheckEmty_True() {
        SpielZeile empty = new SpielZeile();
        // a, b, c are null by default in the underlying field
        assertTrue(empty.checkEmty());
    }

    // === getA/getB/getC/getD return placeholders when null ===

    @Test
    void testGetA_NullReturnsPlatzhalter() {
        SpielZeile z = new SpielZeile();
        Spiel result = z.getA();
        assertTrue(result.isPlatzhalter());
    }

    @Test
    void testGetB_NullReturnsPlatzhalter() {
        SpielZeile z = new SpielZeile();
        Spiel result = z.getB();
        assertTrue(result.isPlatzhalter());
    }

    @Test
    void testGetC_NullReturnsPlatzhalter() {
        SpielZeile z = new SpielZeile();
        Spiel result = z.getC();
        assertTrue(result.isPlatzhalter());
    }

    @Test
    void testGetD_NullReturnsPlatzhalter() {
        SpielZeile z = new SpielZeile();
        Spiel result = z.getD();
        assertTrue(result.isPlatzhalter());
    }

    // === togglePause ===

    @Test
    void testTogglePause() {
        assertFalse(zeile.isPause());
        zeile.togglePause();
        assertTrue(zeile.isPause());
        zeile.togglePause();
        assertFalse(zeile.isPause());
    }

    // === isKonflikt ===

    @Test
    void testIsKonflikt_False_Null() {
        zeile.setKonfliktText(null);
        assertFalse(zeile.isKonflikt());
    }

    @Test
    void testIsKonflikt_False_Empty() {
        zeile.setKonfliktText("");
        assertFalse(zeile.isKonflikt());
    }

    @Test
    void testIsKonflikt_True() {
        zeile.setKonfliktText("Team spielt doppelt");
        assertTrue(zeile.isKonflikt());
    }

    // === isEmty ===

    @Test
    void testIsEmty_True() {
        SpielZeile z = new SpielZeile();
        assertTrue(z.isEmty());
    }

    // === getZeitAsString ===

    @Test
    void testGetZeitAsString() {
        assertEquals("10:30", zeile.getZeitAsString());
    }

    // === getTauschId ===

    @Test
    void testGetTauschId_Samstag() {
        zeile.setSonntag(false);
        assertEquals("sa,10:30,", zeile.getTauschId());
    }

    @Test
    void testGetTauschId_Sonntag() {
        zeile.setSonntag(true);
        assertEquals("so,10:30,", zeile.getTauschId());
    }

    // === setSpiel / getSpiel ===

    @Test
    void testSetSpiel_A() {
        Spiel s = new Spiel();
        zeile.setSpiel(s, "a");
        // getA() now returns the set spiel
        assertSame(s, zeile.getA());
    }

    @Test
    void testSetSpiel_B() {
        Spiel s = new Spiel();
        zeile.setSpiel(s, "b");
        assertSame(s, zeile.getB());
    }

    @Test
    void testGetSpiel_D() {
        SpielZeile z = new SpielZeile();
        Spiel s = z.getSpiel("d");
        assertNotNull(s);
        assertTrue(s.isPlatzhalter());
    }

    @Test
    void testGetSpiel_Invalid() {
        assertNull(zeile.getSpiel("x"));
    }

    // === getAllSpiele ===

    @Test
    void testGetAllSpiele_Empty() {
        SpielZeile z = new SpielZeile();
        assertTrue(z.getAllSpiele().isEmpty());
    }

    @Test
    void testGetAllSpiele_WithSpiele() {
        Spiel a = new Spiel();
        Spiel b = new Spiel();
        zeile.setSpiel(a, "a");
        zeile.setSpiel(b, "b");

        List<Spiel> spiele = zeile.getAllSpiele();
        assertEquals(2, spiele.size());
    }

    // === Default values ===

    @Test
    void testDefaultValues() {
        SpielZeile z = new SpielZeile();
        assertFalse(z.isPause());
        assertFalse(z.isFinale());
        assertFalse(z.isSonntag());
        assertEquals(SpielZeilenPhaseEnum.A_ANSTEHEND, z.getPhase());
        assertNotNull(z.getGuid());
        assertNotNull(z.getGId());
    }
}
