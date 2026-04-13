package ch.plaintext.schuetu.entity;

import ch.plaintext.schuetu.model.enums.GeschlechtEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MannschaftBusinessLogicTest {

    @Test
    void testGetName_WithTeamNummer() {
        Mannschaft m = new Mannschaft();
        m.setGeschlecht(GeschlechtEnum.M);
        m.setKlasse(5);
        m.setTeamNummer(3);

        assertEquals("M503", m.getName());
    }

    @Test
    void testGetName_WithTeamNummerAbove10() {
        Mannschaft m = new Mannschaft();
        m.setGeschlecht(GeschlechtEnum.K);
        m.setKlasse(4);
        m.setTeamNummer(12);

        assertEquals("K412", m.getName());
    }

    @Test
    void testGetName_WithTeamNummerZero() {
        Mannschaft m = new Mannschaft();
        m.setGeschlecht(GeschlechtEnum.M);
        m.setKlasse(5);
        m.setTeamNummer(0);

        assertEquals("M5XX", m.getName());
    }

    @Test
    void testGetName_WithNickname() {
        Mannschaft m = new Mannschaft();
        m.setGeschlecht(GeschlechtEnum.M);
        m.setKlasse(5);
        m.setTeamNummer(3);
        m.setNickname("Tigers");

        assertEquals("M503 (Tigers)", m.getName());
    }

    @Test
    void testGetName_WithKonflikt() {
        Mannschaft m = new Mannschaft();
        m.setGeschlecht(GeschlechtEnum.M);
        m.setKlasse(5);
        m.setTeamNummer(3);
        m.setKonflikt(true);

        assertEquals("M503@", m.getName());
    }

    @Test
    void testGetNameNoNickname() {
        Mannschaft m = new Mannschaft();
        m.setGeschlecht(GeschlechtEnum.M);
        m.setKlasse(5);
        m.setTeamNummer(3);
        m.setNickname("Tigers");

        assertEquals("M503", m.getNameNoNickname());
    }

    @Test
    void testGetShortKatName() {
        Mannschaft m = new Mannschaft();
        m.setGeschlecht(GeschlechtEnum.K);
        m.setKlasse(4);

        assertEquals("K4", m.getShortKatName());
    }

    @Test
    void testGetGeschlechtString_M() {
        Mannschaft m = new Mannschaft();
        m.setGeschlecht(GeschlechtEnum.M);
        assertEquals("m", m.getGeschlechtString());
    }

    @Test
    void testGetGeschlechtString_K() {
        Mannschaft m = new Mannschaft();
        m.setGeschlecht(GeschlechtEnum.K);
        assertEquals("k", m.getGeschlechtString());
    }

    @Test
    void testSetGeschlechtString_K() {
        Mannschaft m = new Mannschaft();
        m.setGeschlechtString("k");
        assertEquals(GeschlechtEnum.K, m.getGeschlecht());
    }

    @Test
    void testSetGeschlechtString_M() {
        Mannschaft m = new Mannschaft();
        m.setGeschlechtString("m");
        assertEquals(GeschlechtEnum.M, m.getGeschlecht());
    }

    @Test
    void testGetBegleitpersonVorname_WithSpace() {
        Mannschaft m = new Mannschaft();
        m.setBegleitpersonName("Max Muster");
        assertEquals("Max", m.getBegleitpersonVorname());
    }

    @Test
    void testGetBegleitpersonVorname_NoSpace() {
        Mannschaft m = new Mannschaft();
        m.setBegleitpersonName("Max");
        assertEquals("Max", m.getBegleitpersonVorname());
    }

    @Test
    void testGetBegleitpersonVorname_Empty() {
        Mannschaft m = new Mannschaft();
        m.setBegleitpersonName("");
        assertEquals("", m.getBegleitpersonVorname());
    }

    @Test
    void testGetBegleitpersonNameNach() {
        Mannschaft m = new Mannschaft();
        m.setBegleitpersonName("Max Muster");
        assertEquals(" Muster", m.getBegleitpersonNameNach());
    }

    @Test
    void testGetBegleitpersonPLZ() {
        Mannschaft m = new Mannschaft();
        m.setBegleitpersonPLZOrt("3076 Worb");
        assertEquals("3076", m.getBegleitpersonPLZ());
    }

    @Test
    void testGetBegleitpersonOrt() {
        Mannschaft m = new Mannschaft();
        m.setBegleitpersonPLZOrt("3076 Worb");
        assertEquals("Worb", m.getBegleitpersonOrt());
    }

    @Test
    void testGetVorname_WithSpace() {
        Mannschaft m = new Mannschaft();
        m.setBegleitpersonName("Max Muster");
        assertEquals("Max", m.getVorname());
    }

    @Test
    void testGetVorname_NoSpace() {
        Mannschaft m = new Mannschaft();
        m.setBegleitpersonName("Max");
        assertEquals("Max", m.getVorname());
    }

    @Test
    void testGetKategorie_WithGruppe() {
        Mannschaft m = new Mannschaft();
        Gruppe g = new Gruppe();
        Kategorie k = new Kategorie();
        g.setKategorie(k);
        m.setGruppe(g);

        assertSame(k, m.getKategorie());
    }

    @Test
    void testGetKategorie_NoGruppe() {
        Mannschaft m = new Mannschaft();
        assertNull(m.getKategorie());
    }

    @Test
    void testDefaultValues() {
        Mannschaft m = new Mannschaft();
        assertEquals(GeschlechtEnum.M, m.getGeschlecht());
        assertEquals(0, m.getTeamNummer());
        assertEquals(0, m.getKlasse());
        assertEquals("", m.getFarbe());
        assertEquals("", m.getSchulhaus());
        assertEquals("Blau", m.getColor());
        assertFalse(m.isKonflikt());
    }

    @Test
    void testToString() {
        Mannschaft m = new Mannschaft();
        m.setGeschlecht(GeschlechtEnum.M);
        m.setKlasse(5);
        m.setTeamNummer(3);

        assertTrue(m.toString().contains("M503"));
    }
}
