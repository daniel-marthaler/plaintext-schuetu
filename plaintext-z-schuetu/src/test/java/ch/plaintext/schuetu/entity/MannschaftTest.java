package ch.plaintext.schuetu.entity;

import ch.plaintext.schuetu.model.enums.GeschlechtEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MannschaftTest {

    private Mannschaft mannschaft;

    @BeforeEach
    void setUp() {
        mannschaft = new Mannschaft();
        mannschaft.setGeschlecht(GeschlechtEnum.M);
        mannschaft.setKlasse(5);
        mannschaft.setTeamNummer(3);
    }

    // === getName ===

    @Test
    void testGetName() {
        assertEquals("M503", mannschaft.getName());
    }

    @Test
    void testGetName_SingleDigit() {
        mannschaft.setTeamNummer(7);
        assertEquals("M507", mannschaft.getName());
    }

    @Test
    void testGetName_DoubleDigit() {
        mannschaft.setTeamNummer(12);
        assertEquals("M512", mannschaft.getName());
    }

    @Test
    void testGetName_ZeroTeamNummer() {
        mannschaft.setTeamNummer(0);
        assertEquals("M5XX", mannschaft.getName());
    }

    @Test
    void testGetName_ZeroTeamNummer_Konflikt() {
        mannschaft.setTeamNummer(0);
        mannschaft.setKonflikt(true);
        assertEquals("M5XX@", mannschaft.getName());
    }

    @Test
    void testGetName_WithNickname() {
        mannschaft.setNickname("Tigers");
        assertEquals("M503 (Tigers)", mannschaft.getName());
    }

    @Test
    void testGetName_WithKonflikt() {
        mannschaft.setKonflikt(true);
        assertEquals("M503@", mannschaft.getName());
    }

    @Test
    void testGetName_WithNicknameAndKonflikt() {
        mannschaft.setNickname("Tigers");
        mannschaft.setKonflikt(true);
        assertEquals("M503 (Tigers)@", mannschaft.getName());
    }

    // === getNameNoNickname ===

    @Test
    void testGetNameNoNickname() {
        mannschaft.setNickname("Tigers");
        String name = mannschaft.getNameNoNickname();
        assertFalse(name.contains("Tigers"));
        assertFalse(name.contains("("));
        assertTrue(name.equals(name.toUpperCase()));
    }

    // === getShortKatName ===

    @Test
    void testGetShortKatName() {
        assertEquals("M5", mannschaft.getShortKatName());
    }

    @Test
    void testGetShortKatName_Knaben() {
        mannschaft.setGeschlecht(GeschlechtEnum.K);
        mannschaft.setKlasse(3);
        assertEquals("K3", mannschaft.getShortKatName());
    }

    // === getGeschlechtString ===

    @Test
    void testGetGeschlechtString_M() {
        mannschaft.setGeschlecht(GeschlechtEnum.M);
        assertEquals("m", mannschaft.getGeschlechtString());
    }

    @Test
    void testGetGeschlechtString_K() {
        mannschaft.setGeschlecht(GeschlechtEnum.K);
        assertEquals("k", mannschaft.getGeschlechtString());
    }

    // === setGeschlechtString ===

    @Test
    void testSetGeschlechtString_M() {
        mannschaft.setGeschlechtString("m");
        assertEquals(GeschlechtEnum.M, mannschaft.getGeschlecht());
    }

    @Test
    void testSetGeschlechtString_K() {
        mannschaft.setGeschlechtString("k");
        assertEquals(GeschlechtEnum.K, mannschaft.getGeschlecht());
    }

    // === getBegleitpersonVorname ===

    @Test
    void testGetBegleitpersonVorname_FullName() {
        mannschaft.setBegleitpersonName("Hans Müller");
        assertEquals("Hans", mannschaft.getBegleitpersonVorname());
    }

    @Test
    void testGetBegleitpersonVorname_SingleName() {
        mannschaft.setBegleitpersonName("Müller");
        assertEquals("Müller", mannschaft.getBegleitpersonVorname());
    }

    @Test
    void testGetBegleitpersonVorname_Empty() {
        mannschaft.setBegleitpersonName("");
        assertEquals("", mannschaft.getBegleitpersonVorname());
    }

    // === getBegleitpersonPLZ / getBegleitpersonOrt ===

    @Test
    void testGetBegleitpersonPLZ() {
        mannschaft.setBegleitpersonPLZOrt("3076 Worb");
        assertEquals("3076", mannschaft.getBegleitpersonPLZ());
    }

    @Test
    void testGetBegleitpersonOrt() {
        mannschaft.setBegleitpersonPLZOrt("3076 Worb");
        assertEquals("Worb", mannschaft.getBegleitpersonOrt());
    }

    // === getTorverhaeltnis (needs Gruppe setup, simplified test) ===

    @Test
    void testDefaultValues() {
        Mannschaft m = new Mannschaft();
        assertEquals(GeschlechtEnum.M, m.getGeschlecht());
        assertEquals(0, m.getTeamNummer());
        assertEquals(0, m.getKlasse());
        assertEquals(0, m.getAnzahlSpieler());
        assertEquals("", m.getSchulhaus());
        assertEquals("Blau", m.getColor());
        assertFalse(m.getDisqualifiziert());
        assertFalse(m.isKonflikt());
    }

    // === toString ===

    @Test
    void testToString() {
        assertEquals("Mannschaft [M503]", mannschaft.toString());
    }
}
