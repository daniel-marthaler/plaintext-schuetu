package ch.plaintext.schuetu.entity;

import ch.plaintext.schuetu.model.enums.GeschlechtEnum;
import ch.plaintext.schuetu.model.enums.SpielEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SpielBusinessLogicTest {

    private Mannschaft createMannschaft(String nickname, int teamNr, int klasse, GeschlechtEnum geschlecht) {
        Mannschaft m = new Mannschaft();
        m.setNickname(nickname);
        m.setTeamNummer(teamNr);
        m.setKlasse(klasse);
        m.setGeschlecht(geschlecht);
        m.setFarbe("blau");
        m.setSchulhaus("Schule");
        return m;
    }

    @Test
    void testGetPunkteA_Sieg() {
        Spiel spiel = new Spiel();
        spiel.setToreABestaetigt(3);
        spiel.setToreBBestaetigt(1);

        assertEquals(3, spiel.getPunkteA());
    }

    @Test
    void testGetPunkteA_Unentschieden() {
        Spiel spiel = new Spiel();
        spiel.setToreABestaetigt(2);
        spiel.setToreBBestaetigt(2);

        assertEquals(1, spiel.getPunkteA());
    }

    @Test
    void testGetPunkteA_Niederlage() {
        Spiel spiel = new Spiel();
        spiel.setToreABestaetigt(1);
        spiel.setToreBBestaetigt(3);

        assertEquals(0, spiel.getPunkteA());
    }

    @Test
    void testGetPunkteB_Sieg() {
        Spiel spiel = new Spiel();
        spiel.setToreABestaetigt(1);
        spiel.setToreBBestaetigt(3);

        assertEquals(3, spiel.getPunkteB());
    }

    @Test
    void testGetPunkteB_Unentschieden() {
        Spiel spiel = new Spiel();
        spiel.setToreABestaetigt(2);
        spiel.setToreBBestaetigt(2);

        assertEquals(1, spiel.getPunkteB());
    }

    @Test
    void testGetPunkteB_Niederlage() {
        Spiel spiel = new Spiel();
        spiel.setToreABestaetigt(3);
        spiel.setToreBBestaetigt(1);

        assertEquals(0, spiel.getPunkteB());
    }

    @Test
    void testGetPunkteA_NotInitialized() {
        Spiel spiel = new Spiel();
        assertEquals(-1, spiel.getPunkteA());
    }

    @Test
    void testGetPunkteVonMannschaft_A() {
        Mannschaft mA = createMannschaft("A", 1, 5, GeschlechtEnum.M);
        Mannschaft mB = createMannschaft("B", 2, 5, GeschlechtEnum.M);

        Spiel spiel = new Spiel();
        spiel.setMannschaftA(mA);
        spiel.setMannschaftB(mB);
        spiel.setToreABestaetigt(3);
        spiel.setToreBBestaetigt(1);

        assertEquals(3, spiel.getPunkteVonMannschaft(mA));
        assertEquals(0, spiel.getPunkteVonMannschaft(mB));
    }

    @Test
    void testGetPunkteVonMannschaft_Unknown() {
        Mannschaft mA = createMannschaft("A", 1, 5, GeschlechtEnum.M);
        Mannschaft mB = createMannschaft("B", 2, 5, GeschlechtEnum.M);
        Mannschaft mC = createMannschaft("C", 3, 5, GeschlechtEnum.M);

        Spiel spiel = new Spiel();
        spiel.setMannschaftA(mA);
        spiel.setMannschaftB(mB);
        spiel.setToreABestaetigt(3);
        spiel.setToreBBestaetigt(1);

        assertEquals(-1, spiel.getPunkteVonMannschaft(mC));
    }

    @Test
    void testGetToreErziehlt_A() {
        Mannschaft mA = createMannschaft("A", 1, 5, GeschlechtEnum.M);
        Mannschaft mB = createMannschaft("B", 2, 5, GeschlechtEnum.M);

        Spiel spiel = new Spiel();
        spiel.setMannschaftA(mA);
        spiel.setMannschaftB(mB);
        spiel.setToreABestaetigt(3);
        spiel.setToreBBestaetigt(1);

        assertEquals(3, spiel.getToreErziehlt(mA));
        assertEquals(1, spiel.getToreErziehlt(mB));
    }

    @Test
    void testGetToreKassiert_A() {
        Mannschaft mA = createMannschaft("A", 1, 5, GeschlechtEnum.M);
        Mannschaft mB = createMannschaft("B", 2, 5, GeschlechtEnum.M);

        Spiel spiel = new Spiel();
        spiel.setMannschaftA(mA);
        spiel.setMannschaftB(mB);
        spiel.setToreABestaetigt(3);
        spiel.setToreBBestaetigt(1);

        assertEquals(1, spiel.getToreKassiert(mA));
        assertEquals(3, spiel.getToreKassiert(mB));
    }

    @Test
    void testGetFertiggespielt_True() {
        Spiel spiel = new Spiel();
        spiel.setToreABestaetigt(3);
        spiel.setToreBBestaetigt(1);

        assertTrue(spiel.getFertiggespielt());
    }

    @Test
    void testGetFertiggespielt_False() {
        Spiel spiel = new Spiel();
        assertFalse(spiel.getFertiggespielt());
    }

    @Test
    void testGetMannschaftAName_WithMannschaft() {
        Mannschaft m = createMannschaft("Tigers", 1, 5, GeschlechtEnum.M);
        Spiel spiel = new Spiel();
        spiel.setMannschaftA(m);
        assertEquals(m.getName(), spiel.getMannschaftAName());
    }

    @Test
    void testGetMannschaftAName_GrosserFinal() {
        Spiel spiel = new Spiel();
        spiel.setTyp(SpielEnum.GFINAL);
        assertEquals("A, GF", spiel.getMannschaftAName());
    }

    @Test
    void testGetMannschaftAName_KleinerFinal() {
        Spiel spiel = new Spiel();
        spiel.setTyp(SpielEnum.KFINAL);
        assertEquals("A, KF ", spiel.getMannschaftAName());
    }

    @Test
    void testGetMannschaftBName_GrosserFinal() {
        Spiel spiel = new Spiel();
        spiel.setTyp(SpielEnum.GFINAL);
        assertEquals("B, GF", spiel.getMannschaftBName());
    }

    @Test
    void testGetFarbe_SameFarbe() {
        Mannschaft mA = createMannschaft("A", 1, 5, GeschlechtEnum.M);
        mA.setFarbe("rot");
        Mannschaft mB = createMannschaft("B", 2, 5, GeschlechtEnum.M);
        mB.setFarbe("rot");

        Spiel spiel = new Spiel();
        spiel.setMannschaftA(mA);
        spiel.setMannschaftB(mB);

        assertEquals("red", spiel.getFarbe());
    }

    @Test
    void testGetFarbe_DifferentFarbe() {
        Mannschaft mA = createMannschaft("A", 1, 5, GeschlechtEnum.M);
        mA.setFarbe("rot");
        Mannschaft mB = createMannschaft("B", 2, 5, GeschlechtEnum.M);
        mB.setFarbe("blau");

        Spiel spiel = new Spiel();
        spiel.setMannschaftA(mA);
        spiel.setMannschaftB(mB);

        assertEquals("white", spiel.getFarbe());
    }

    @Test
    void testGetFarbe_NoMannschaften() {
        Spiel spiel = new Spiel();
        assertEquals("white", spiel.getFarbe());
    }

    @Test
    void testEvaluateToreABestateigtString_NotInit() {
        Spiel spiel = new Spiel();
        assertEquals("--", spiel.evaluateToreABestateigtString());
    }

    @Test
    void testEvaluateToreABestateigtString_WithValue() {
        Spiel spiel = new Spiel();
        spiel.setToreABestaetigt(3);
        assertEquals("03", spiel.evaluateToreABestateigtString());
    }

    @Test
    void testEvaluateToreBBestateigtString_NotInit() {
        Spiel spiel = new Spiel();
        assertEquals("--", spiel.evaluateToreBBestateigtString());
    }

    @Test
    void testGetWebsiteName_WithRealName() {
        Spiel spiel = new Spiel();
        spiel.setRealName("GrFin-MKl5");
        assertEquals("GrFin-MKl5", spiel.getWebsiteName());
    }

    @Test
    void testGetWebsiteName_GFinal() {
        Spiel spiel = new Spiel();
        spiel.setTyp(SpielEnum.GFINAL);
        spiel.setKategorieName("MKl5");
        spiel.setRealName("");
        assertEquals("GrFin-MKl5", spiel.getWebsiteName());
    }

    @Test
    void testGetWebsiteName_KFinal() {
        Spiel spiel = new Spiel();
        spiel.setTyp(SpielEnum.KFINAL);
        spiel.setKategorieName("MKl5");
        spiel.setRealName("");
        assertEquals("KlFin-MKl5", spiel.getWebsiteName());
    }

    @Test
    void testGetGruppe_WithMannschaftA() {
        Mannschaft m = createMannschaft("A", 1, 5, GeschlechtEnum.M);
        Gruppe g = new Gruppe();
        m.setGruppe(g);

        Spiel spiel = new Spiel();
        spiel.setMannschaftA(m);

        assertSame(g, spiel.getGruppe());
    }

    @Test
    void testGetGruppe_NoMannschaftA() {
        Spiel spiel = new Spiel();
        assertNull(spiel.getGruppe());
    }

    @Test
    void testResetTausch() {
        Spiel spiel = new Spiel();
        spiel.setHintauschOk(false);
        spiel.setHertauschOk(false);

        spiel.resetTausch();
        assertTrue(spiel.isHintauschOk());
        assertTrue(spiel.isHertauschOk());
    }

    @Test
    void testIsFinaleBekannt() {
        Spiel spiel = new Spiel();
        assertFalse(spiel.isFinaleBekannt());

        Mannschaft m = createMannschaft("A", 1, 5, GeschlechtEnum.M);
        spiel.setMannschaftA(m);
        assertTrue(spiel.isFinaleBekannt());
    }

    @Test
    void testToString_Platzhalter() {
        Spiel spiel = new Spiel();
        spiel.setPlatzhalter(true);
        assertEquals("-", spiel.toString());
    }

    @Test
    void testToStringSpieltage_WithMatch() {
        Mannschaft mA = createMannschaft("A", 1, 5, GeschlechtEnum.M);
        Mannschaft mB = createMannschaft("B", 2, 5, GeschlechtEnum.M);
        Spiel spiel = new Spiel();
        spiel.setMannschaftA(mA);
        spiel.setMannschaftB(mB);

        String result = spiel.toStringSpieltage();
        assertTrue(result.contains("@"));
    }

    @Test
    void testToStringSpieltage_Final() {
        Spiel spiel = new Spiel();
        spiel.setTyp(SpielEnum.GFINAL);
        spiel.setKategorieName("MKl5");
        spiel.setRealName("");

        String result = spiel.toStringSpieltage();
        assertTrue(result.contains("*"));
    }
}
