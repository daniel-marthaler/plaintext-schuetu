package ch.plaintext.schuetu.entity;

import ch.plaintext.schuetu.model.enums.GeschlechtEnum;
import ch.plaintext.schuetu.model.enums.SpielZeilenPhaseEnum;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SpielZeileBusinessLogicTest {

    @Test
    void testCheckEmpty_AllNull() {
        SpielZeile sz = new SpielZeile();
        sz.setA(null);
        sz.setB(null);
        sz.setC(null);
        assertTrue(sz.checkEmty());
    }

    @Test
    void testCheckEmpty_ASet() {
        SpielZeile sz = new SpielZeile();
        sz.setA(new Spiel());
        assertFalse(sz.checkEmty());
    }

    @Test
    void testCheckEmpty_BSet() {
        SpielZeile sz = new SpielZeile();
        sz.setB(new Spiel());
        assertFalse(sz.checkEmty());
    }

    @Test
    void testCheckEmpty_CSet() {
        SpielZeile sz = new SpielZeile();
        sz.setC(new Spiel());
        assertFalse(sz.checkEmty());
    }

    @Test
    void testIsEmty() {
        SpielZeile sz = new SpielZeile();
        sz.setA(null);
        sz.setB(null);
        sz.setC(null);
        assertTrue(sz.isEmty());
    }

    @Test
    void testIsEmty_WithSpiel() {
        SpielZeile sz = new SpielZeile();
        sz.setA(new Spiel());
        assertFalse(sz.isEmty());
    }

    @Test
    void testGetA_WhenNull_ReturnsPlatzhalter() {
        SpielZeile sz = new SpielZeile();
        sz.setA(null);
        Spiel result = sz.getA();
        assertNotNull(result);
        assertTrue(result.isPlatzhalter());
    }

    @Test
    void testGetA_WhenSet_ReturnsSpiel() {
        SpielZeile sz = new SpielZeile();
        Spiel spiel = new Spiel();
        sz.setA(spiel);
        assertSame(spiel, sz.getA());
        assertFalse(spiel.isPlatzhalter());
    }

    @Test
    void testGetB_WhenNull_ReturnsPlatzhalter() {
        SpielZeile sz = new SpielZeile();
        sz.setB(null);
        assertTrue(sz.getB().isPlatzhalter());
    }

    @Test
    void testGetC_WhenNull_ReturnsPlatzhalter() {
        SpielZeile sz = new SpielZeile();
        sz.setC(null);
        assertTrue(sz.getC().isPlatzhalter());
    }

    @Test
    void testGetD_WhenNull_ReturnsPlatzhalter() {
        SpielZeile sz = new SpielZeile();
        sz.setD(null);
        assertTrue(sz.getD().isPlatzhalter());
    }

    @Test
    void testIsKonflikt_Null() {
        SpielZeile sz = new SpielZeile();
        sz.setKonfliktText(null);
        assertFalse(sz.isKonflikt());
    }

    @Test
    void testIsKonflikt_Empty() {
        SpielZeile sz = new SpielZeile();
        sz.setKonfliktText("");
        assertFalse(sz.isKonflikt());
    }

    @Test
    void testIsKonflikt_WithText() {
        SpielZeile sz = new SpielZeile();
        sz.setKonfliktText("Doppelte Mannschaft!");
        assertTrue(sz.isKonflikt());
    }

    @Test
    void testTogglePause() {
        SpielZeile sz = new SpielZeile();
        assertFalse(sz.isPause());

        sz.togglePause();
        assertTrue(sz.isPause());

        sz.togglePause();
        assertFalse(sz.isPause());
    }

    @Test
    void testGetAllMannschaften() {
        SpielZeile sz = new SpielZeile();
        Mannschaft mA = new Mannschaft();
        mA.setGeschlecht(GeschlechtEnum.M);
        Mannschaft mB = new Mannschaft();
        mB.setGeschlecht(GeschlechtEnum.M);

        Spiel spiel = new Spiel();
        spiel.setMannschaftA(mA);
        spiel.setMannschaftB(mB);
        sz.setA(spiel);

        List<Mannschaft> mannschaften = sz.getAllMannschaften();
        assertEquals(2, mannschaften.size());
    }

    @Test
    void testGetAllMannschaften_AllNull() {
        SpielZeile sz = new SpielZeile();
        sz.setA(null);
        sz.setB(null);
        sz.setC(null);
        sz.setD(null);
        List<Mannschaft> mannschaften = sz.getAllMannschaften();
        assertTrue(mannschaften.isEmpty());
    }

    @Test
    void testGetAllSpiele() {
        SpielZeile sz = new SpielZeile();
        Spiel s1 = new Spiel();
        Spiel s2 = new Spiel();
        sz.setA(s1);
        sz.setB(s2);

        List<Spiel> spiele = sz.getAllSpiele();
        assertEquals(2, spiele.size());
    }

    @Test
    void testSetSpiel() {
        SpielZeile sz = new SpielZeile();
        Spiel s = new Spiel();

        sz.setSpiel(s, "a");
        assertSame(s, sz.getA());

        Spiel s2 = new Spiel();
        sz.setSpiel(s2, "b");
        assertSame(s2, sz.getB());
    }

    @Test
    void testGetSpiel() {
        SpielZeile sz = new SpielZeile();
        Spiel s = new Spiel();
        sz.setA(s);

        assertSame(s, sz.getSpiel("a"));
    }

    @Test
    void testGetSpiel_InvalidPlatz() {
        SpielZeile sz = new SpielZeile();
        assertNull(sz.getSpiel("x"));
    }

    @Test
    void testGetTauschId_Samstag() {
        SpielZeile sz = new SpielZeile();
        sz.setSonntag(false);
        sz.setStart(new Date());

        String id = sz.getTauschId();
        assertTrue(id.startsWith("sa,"));
    }

    @Test
    void testGetTauschId_Sonntag() {
        SpielZeile sz = new SpielZeile();
        sz.setSonntag(true);
        sz.setStart(new Date());

        String id = sz.getTauschId();
        assertTrue(id.startsWith("so,"));
    }

    @Test
    void testDefaultValues() {
        SpielZeile sz = new SpielZeile();
        assertNotNull(sz.getGuid());
        assertNotNull(sz.getGId());
        assertNotNull(sz.getCreationdate());
        assertFalse(sz.isPause());
        assertFalse(sz.isFinale());
        assertFalse(sz.isSonntag());
        assertEquals(SpielZeilenPhaseEnum.A_ANSTEHEND, sz.getPhase());
    }
}
