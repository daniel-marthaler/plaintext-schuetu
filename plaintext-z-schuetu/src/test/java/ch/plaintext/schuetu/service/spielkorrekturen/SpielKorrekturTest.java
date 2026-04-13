package ch.plaintext.schuetu.service.spielkorrekturen;

import ch.plaintext.schuetu.entity.Mannschaft;
import ch.plaintext.schuetu.entity.Spiel;
import ch.plaintext.schuetu.model.enums.GeschlechtEnum;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class SpielKorrekturTest {

    private Mannschaft createMannschaft(String nickname, int teamNr) {
        Mannschaft m = new Mannschaft();
        m.setNickname(nickname);
        m.setTeamNummer(teamNr);
        m.setKlasse(5);
        m.setGeschlecht(GeschlechtEnum.M);
        m.setFarbe("");
        m.setSchulhaus("");
        return m;
    }

    @Test
    void testSetSpiel_WithMannschaften() {
        Spiel spiel = new Spiel();
        spiel.setId(42L);
        spiel.setIdString("X123");
        spiel.setStart(new Date());
        spiel.setEintrager("TestUser");
        spiel.setFertigEingetragen(true);
        spiel.setNotizen("Some notes");
        spiel.setKontrolle("ok");

        Mannschaft mA = createMannschaft("A", 1);
        Mannschaft mB = createMannschaft("B", 2);
        spiel.setMannschaftA(mA);
        spiel.setMannschaftB(mB);

        SpielKorrektur sk = new SpielKorrektur();
        sk.setSpiel(spiel);

        assertTrue(sk.getNamesP().contains(mA.getName()));
        assertTrue(sk.getNamesP().contains(mB.getName()));
        assertEquals("X123", sk.getIdP());
        assertEquals("TestUser", sk.getEintragerP());
        assertTrue(sk.isEingetragenP());
        assertTrue(sk.isBemerkungP());
        assertEquals("ok", sk.getKontrolleP());
    }

    @Test
    void testSetSpiel_WithoutMannschaften() {
        Spiel spiel = new Spiel();
        spiel.setId(42L);
        spiel.setIdString("X123");
        spiel.setStart(new Date());

        SpielKorrektur sk = new SpielKorrektur();
        sk.setSpiel(spiel);

        assertNull(sk.getNamesP());
    }

    @Test
    void testGetId() {
        Spiel spiel = new Spiel();
        spiel.setId(42L);
        spiel.setStart(new Date());

        SpielKorrektur sk = new SpielKorrektur();
        sk.setSpiel(spiel);

        assertEquals("42", sk.getId());
    }

    @Test
    void testSetId() {
        Spiel spiel = new Spiel();
        spiel.setStart(new Date());

        SpielKorrektur sk = new SpielKorrektur();
        sk.setSpiel(spiel);
        sk.setId("99");

        assertEquals(99L, sk.getSpiel().getId());
    }

    @Test
    void testGetTore_NotBestaetigt() {
        Spiel spiel = new Spiel();
        spiel.setStart(new Date());

        SpielKorrektur sk = new SpielKorrektur();
        sk.setSpiel(spiel);

        assertEquals("-", sk.getTore());
    }

    @Test
    void testGetTore_Bestaetigt() {
        Spiel spiel = new Spiel();
        spiel.setStart(new Date());
        spiel.setFertigBestaetigt(true);
        spiel.setToreABestaetigt(3);
        spiel.setToreBBestaetigt(1);

        SpielKorrektur sk = new SpielKorrektur();
        sk.setSpiel(spiel);

        assertEquals("3 : 1", sk.getTore());
    }

    @Test
    void testGetSetA() {
        Spiel spiel = new Spiel();
        spiel.setStart(new Date());
        spiel.setToreABestaetigt(5);

        SpielKorrektur sk = new SpielKorrektur();
        sk.setSpiel(spiel);

        assertEquals(5, sk.getA());
        sk.setA(7);
        assertEquals(7, sk.getA());
    }

    @Test
    void testGetSetB() {
        Spiel spiel = new Spiel();
        spiel.setStart(new Date());
        spiel.setToreBBestaetigt(2);

        SpielKorrektur sk = new SpielKorrektur();
        sk.setSpiel(spiel);

        assertEquals(2, sk.getB());
        sk.setB(4);
        assertEquals(4, sk.getB());
    }

    @Test
    void testGetSetNotitzen() {
        Spiel spiel = new Spiel();
        spiel.setStart(new Date());
        spiel.setNotizen("Test note");

        SpielKorrektur sk = new SpielKorrektur();
        sk.setSpiel(spiel);

        assertEquals("Test note", sk.getNotitzen());
        sk.setNotitzen("Updated note");
        assertEquals("Updated note", sk.getNotitzen());
    }

    @Test
    void testGetStart() {
        Date start = new Date();
        Spiel spiel = new Spiel();
        spiel.setStart(start);

        SpielKorrektur sk = new SpielKorrektur();
        sk.setSpiel(spiel);

        assertSame(start, sk.getStart());
    }

    @Test
    void testBemerkungP_EmptyNotizen() {
        Spiel spiel = new Spiel();
        spiel.setStart(new Date());
        spiel.setNotizen("");

        SpielKorrektur sk = new SpielKorrektur();
        sk.setSpiel(spiel);

        assertFalse(sk.isBemerkungP());
    }
}
