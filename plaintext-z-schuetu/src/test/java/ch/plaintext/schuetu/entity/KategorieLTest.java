package ch.plaintext.schuetu.entity;

import ch.plaintext.schuetu.model.enums.GeschlechtEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class KategorieLTest {

    private Kategorie kategorie;
    private Gruppe gruppeA;
    private Gruppe gruppeB;

    @BeforeEach
    void setUp() {
        kategorie = new Kategorie();
        gruppeA = new Gruppe();
        gruppeB = new Gruppe();

        gruppeA.setKategorie(kategorie);
        gruppeB.setKategorie(kategorie);

        kategorie.setGruppeA(gruppeA);
    }

    private Mannschaft createMannschaft(GeschlechtEnum geschlecht, int klasse, int teamNummer) {
        Mannschaft m = new Mannschaft();
        m.setGeschlecht(geschlecht);
        m.setKlasse(klasse);
        m.setTeamNummer(teamNummer);
        m.setGruppe(gruppeA);
        return m;
    }

    // === getName ===

    @Test
    void testGetName_NoGruppeA() {
        Kategorie k = new Kategorie();
        assertEquals("invalide", k.getName());
    }

    @Test
    void testGetName_WithMannschaften() {
        List<Mannschaft> mannschaften = new ArrayList<>();
        mannschaften.add(createMannschaft(GeschlechtEnum.M, 5, 1));
        mannschaften.add(createMannschaft(GeschlechtEnum.M, 5, 2));
        gruppeA.setMannschaften(mannschaften);
        gruppeA.setGeschlecht(GeschlechtEnum.M);

        String name = kategorie.getName();
        assertTrue(name.startsWith("M"));
        assertTrue(name.contains("Kl"));
        assertTrue(name.contains("5"));
    }

    // === getKlassen ===

    @Test
    void testGetKlassen_SingleKlasse() {
        List<Mannschaft> mannschaften = new ArrayList<>();
        mannschaften.add(createMannschaft(GeschlechtEnum.M, 5, 1));
        mannschaften.add(createMannschaft(GeschlechtEnum.M, 5, 2));
        gruppeA.setMannschaften(mannschaften);

        List<Integer> klassen = kategorie.getKlassen();
        assertEquals(1, klassen.size());
        assertEquals(5, klassen.get(0));
    }

    @Test
    void testGetKlassen_MultipleKlassen() {
        List<Mannschaft> mannschaften = new ArrayList<>();
        mannschaften.add(createMannschaft(GeschlechtEnum.M, 3, 1));
        mannschaften.add(createMannschaft(GeschlechtEnum.M, 4, 2));
        mannschaften.add(createMannschaft(GeschlechtEnum.M, 5, 3));
        gruppeA.setMannschaften(mannschaften);

        List<Integer> klassen = kategorie.getKlassen();
        assertEquals(3, klassen.size());
        assertEquals(3, klassen.get(0));
        assertEquals(4, klassen.get(1));
        assertEquals(5, klassen.get(2));
    }

    @Test
    void testGetKlassen_BothGroups() {
        List<Mannschaft> mannschaftenA = new ArrayList<>();
        mannschaftenA.add(createMannschaft(GeschlechtEnum.M, 3, 1));
        gruppeA.setMannschaften(mannschaftenA);

        Mannschaft mB = createMannschaft(GeschlechtEnum.M, 4, 2);
        mB.setGruppe(gruppeB);
        List<Mannschaft> mannschaftenB = new ArrayList<>();
        mannschaftenB.add(mB);
        gruppeB.setMannschaften(mannschaftenB);
        kategorie.setGruppeB(gruppeB);

        List<Integer> klassen = kategorie.getKlassen();
        assertEquals(2, klassen.size());
        assertTrue(klassen.contains(3));
        assertTrue(klassen.contains(4));
    }

    // === getMannschaften ===

    @Test
    void testGetMannschaften_OnlyGruppeA() {
        List<Mannschaft> mannschaften = new ArrayList<>();
        mannschaften.add(createMannschaft(GeschlechtEnum.M, 5, 1));
        mannschaften.add(createMannschaft(GeschlechtEnum.M, 5, 2));
        gruppeA.setMannschaften(mannschaften);

        List<Mannschaft> result = kategorie.getMannschaften();
        assertEquals(2, result.size());
    }

    @Test
    void testGetMannschaften_NoMannschaften() {
        List<Mannschaft> result = kategorie.getMannschaften();
        assertEquals(0, result.size());
    }

    // === getKlassenString ===

    @Test
    void testGetKlassenString_Single() {
        List<Mannschaft> mannschaften = new ArrayList<>();
        mannschaften.add(createMannschaft(GeschlechtEnum.M, 5, 1));
        gruppeA.setMannschaften(mannschaften);

        assertEquals("5", kategorie.getKlassenString());
    }

    @Test
    void testGetKlassenString_Multiple() {
        List<Mannschaft> mannschaften = new ArrayList<>();
        mannschaften.add(createMannschaft(GeschlechtEnum.M, 3, 1));
        mannschaften.add(createMannschaft(GeschlechtEnum.M, 4, 2));
        gruppeA.setMannschaften(mannschaften);

        String result = kategorie.getKlassenString();
        assertTrue(result.contains("3"));
        assertTrue(result.contains("4"));
        assertTrue(result.contains("&"));
    }

    // === evaluateLowestClass ===

    @Test
    void testEvaluateLowestClass() {
        List<Mannschaft> mannschaften = new ArrayList<>();
        mannschaften.add(createMannschaft(GeschlechtEnum.M, 5, 1));
        mannschaften.add(createMannschaft(GeschlechtEnum.M, 3, 2));
        mannschaften.add(createMannschaft(GeschlechtEnum.M, 4, 3));
        gruppeA.setMannschaften(mannschaften);

        assertEquals(3, kategorie.evaluateLowestClass());
    }

    @Test
    void testEvaluateLowestClass_NoMannschaften() {
        assertEquals(-1, kategorie.evaluateLowestClass());
    }
}
