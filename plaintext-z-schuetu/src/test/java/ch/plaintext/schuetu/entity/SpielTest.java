package ch.plaintext.schuetu.entity;

import ch.plaintext.schuetu.model.enums.GeschlechtEnum;
import ch.plaintext.schuetu.model.enums.SpielEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SpielTest {

    private Spiel spiel;
    private Mannschaft mannschaftA;
    private Mannschaft mannschaftB;

    @BeforeEach
    void setUp() {
        spiel = new Spiel();

        mannschaftA = new Mannschaft();
        mannschaftA.setTeamNummer(1);
        mannschaftA.setKlasse(5);
        mannschaftA.setGeschlecht(GeschlechtEnum.M);

        mannschaftB = new Mannschaft();
        mannschaftB.setTeamNummer(2);
        mannschaftB.setKlasse(5);
        mannschaftB.setGeschlecht(GeschlechtEnum.M);

        spiel.setMannschaftA(mannschaftA);
        spiel.setMannschaftB(mannschaftB);
    }

    // === getPunkteA / getPunkteB ===

    @Test
    void testGetPunkteA_SiegA() {
        spiel.setToreABestaetigt(3);
        spiel.setToreBBestaetigt(1);

        assertEquals(3, spiel.getPunkteA());
        assertEquals(0, spiel.getPunkteB());
    }

    @Test
    void testGetPunkteA_SiegB() {
        spiel.setToreABestaetigt(1);
        spiel.setToreBBestaetigt(3);

        assertEquals(0, spiel.getPunkteA());
        assertEquals(3, spiel.getPunkteB());
    }

    @Test
    void testGetPunkteA_Unentschieden() {
        spiel.setToreABestaetigt(2);
        spiel.setToreBBestaetigt(2);

        assertEquals(1, spiel.getPunkteA());
        assertEquals(1, spiel.getPunkteB());
    }

    @Test
    void testGetPunkteA_NotInitialized() {
        // Default is -1
        assertEquals(-1, spiel.getPunkteA());
        assertEquals(-1, spiel.getPunkteB());
    }

    // === getPunkteVonMannschaft ===

    @Test
    void testGetPunkteVonMannschaft_A() {
        spiel.setToreABestaetigt(3);
        spiel.setToreBBestaetigt(1);

        assertEquals(3, spiel.getPunkteVonMannschaft(mannschaftA));
        assertEquals(0, spiel.getPunkteVonMannschaft(mannschaftB));
    }

    @Test
    void testGetPunkteVonMannschaft_NotInitialized() {
        assertEquals(-1, spiel.getPunkteVonMannschaft(mannschaftA));
    }

    // === getToreErziehlt / getToreKassiert ===

    @Test
    void testGetToreErziehlt_MannschaftA() {
        spiel.setToreABestaetigt(3);
        spiel.setToreBBestaetigt(1);

        assertEquals(3, spiel.getToreErziehlt(mannschaftA));
        assertEquals(1, spiel.getToreErziehlt(mannschaftB));
    }

    @Test
    void testGetToreKassiert_MannschaftA() {
        spiel.setToreABestaetigt(3);
        spiel.setToreBBestaetigt(1);

        assertEquals(1, spiel.getToreKassiert(mannschaftA));
        assertEquals(3, spiel.getToreKassiert(mannschaftB));
    }

    @Test
    void testGetToreErziehlt_NotInitialized() {
        assertEquals(0, spiel.getToreErziehlt(mannschaftA));
    }

    // === getFertiggespielt ===

    @Test
    void testGetFertiggespielt_True() {
        spiel.setToreABestaetigt(2);
        spiel.setToreBBestaetigt(1);

        assertTrue(spiel.getFertiggespielt());
    }

    @Test
    void testGetFertiggespielt_False() {
        spiel.setToreABestaetigt(2);
        // toreBBestaetigt still -1
        assertFalse(spiel.getFertiggespielt());
    }

    // === evaluateToreBestateigtString ===

    @Test
    void testEvaluateToreABestateigtString_Initialized() {
        spiel.setToreABestaetigt(5);
        assertEquals("05", spiel.evaluateToreABestateigtString());
    }

    @Test
    void testEvaluateToreABestateigtString_NotInitialized() {
        assertEquals("--", spiel.evaluateToreABestateigtString());
    }

    @Test
    void testEvaluateToreBBestateigtString_Initialized() {
        spiel.setToreBBestaetigt(12);
        assertEquals("12", spiel.evaluateToreBBestateigtString());
    }

    // === getMannschaftAName / getMannschaftBName ===

    @Test
    void testGetMannschaftAName_WithMannschaft() {
        assertEquals(mannschaftA.getName(), spiel.getMannschaftAName());
    }

    @Test
    void testGetMannschaftAName_NullMannschaft_GFinal() {
        spiel.setMannschaftA(null);
        spiel.setTyp(SpielEnum.GFINAL);

        assertEquals("A, GF", spiel.getMannschaftAName());
    }

    @Test
    void testGetMannschaftAName_NullMannschaft_KFinal() {
        spiel.setMannschaftA(null);
        spiel.setTyp(SpielEnum.KFINAL);

        assertEquals("A, KF ", spiel.getMannschaftAName());
    }

    @Test
    void testGetMannschaftBName_NullMannschaft_GFinal() {
        spiel.setMannschaftB(null);
        spiel.setTyp(SpielEnum.GFINAL);

        assertEquals("B, GF", spiel.getMannschaftBName());
    }

    // === getFarbe ===

    @Test
    void testGetFarbe_SameColor() {
        mannschaftA.setFarbe("Blau");
        mannschaftB.setFarbe("Blau");

        assertEquals("red", spiel.getFarbe());
    }

    @Test
    void testGetFarbe_DifferentColor() {
        mannschaftA.setFarbe("Blau");
        mannschaftB.setFarbe("Rot");

        assertEquals("white", spiel.getFarbe());
    }

    // === resetTausch ===

    @Test
    void testResetTausch() {
        spiel.setHintauschOk(false);
        spiel.setHertauschOk(false);

        spiel.resetTausch();

        assertTrue(spiel.isHintauschOk());
        assertTrue(spiel.isHertauschOk());
    }

    // === isFinaleBekannt ===

    @Test
    void testIsFinaleBekannt_True() {
        assertTrue(spiel.isFinaleBekannt());
    }

    @Test
    void testIsFinaleBekannt_False() {
        spiel.setMannschaftA(null);
        assertFalse(spiel.isFinaleBekannt());
    }

    // === Default values ===

    @Test
    void testDefaultValues() {
        Spiel s = new Spiel();
        assertEquals(SpielEnum.GRUPPE, s.getTyp());
        assertEquals(-1, s.getToreA());
        assertEquals(-1, s.getToreB());
        assertEquals(-1, s.getToreABestaetigt());
        assertEquals(-1, s.getToreBBestaetigt());
        assertFalse(s.getFertiggespielt());
        assertFalse(s.isAmSpielen());
        assertFalse(s.isFertigEingetragen());
        assertFalse(s.isFertigBestaetigt());
        assertFalse(s.isZurueckgewiesen());
    }

    // === getWebsiteName ===

    @Test
    void testGetWebsiteName_WithRealName() {
        spiel.setRealName("Finale 5. Klasse");
        assertEquals("Finale 5. Klasse", spiel.getWebsiteName());
    }

    @Test
    void testGetWebsiteName_GFinal() {
        spiel.setRealName("");
        spiel.setTyp(SpielEnum.GFINAL);
        spiel.setKategorieName("MKl5");

        assertEquals("GrFin-MKl5", spiel.getWebsiteName());
    }

    @Test
    void testGetWebsiteName_KFinal() {
        spiel.setRealName("");
        spiel.setTyp(SpielEnum.KFINAL);
        spiel.setKategorieName("MKl5");

        assertEquals("KlFin-MKl5", spiel.getWebsiteName());
    }
}
