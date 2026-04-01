package ch.plaintext.schuetu.entity;

import ch.plaintext.schuetu.model.TurnierException;
import ch.plaintext.schuetu.model.enums.GeschlechtEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GruppeTest {

    private Gruppe gruppe;
    private Kategorie kategorie;

    @BeforeEach
    void setUp() {
        gruppe = new Gruppe();
        kategorie = new Kategorie();
        gruppe.setKategorie(kategorie);
        kategorie.setGruppeA(gruppe);
    }

    private Mannschaft createMannschaft(GeschlechtEnum geschlecht, int klasse, int teamNummer) {
        Mannschaft m = new Mannschaft();
        m.setGeschlecht(geschlecht);
        m.setKlasse(klasse);
        m.setTeamNummer(teamNummer);
        m.setGruppe(gruppe);
        return m;
    }

    // === getName ===

    @Test
    void testGetName_NoKategorie() {
        Gruppe g = new Gruppe();
        g.setGeschlecht(GeschlechtEnum.M);
        String name = g.getName();
        assertTrue(name.contains("_OHNE_KATEGORIE"));
    }

    @Test
    void testGetName_WithKategorie() {
        List<Mannschaft> mannschaften = new ArrayList<>();
        mannschaften.add(createMannschaft(GeschlechtEnum.M, 5, 1));
        gruppe.setMannschaften(mannschaften);

        String name = gruppe.getName();
        assertNotNull(name);
        assertTrue(name.contains("Gr"));
    }

    // === getGeschlecht ===

    @Test
    void testGetGeschlecht_AlreadySet() {
        gruppe.setGeschlecht(GeschlechtEnum.K);
        assertEquals(GeschlechtEnum.K, gruppe.getGeschlecht());
    }

    @Test
    void testGetGeschlecht_FromMannschaften() {
        Gruppe g = new Gruppe();
        List<Mannschaft> mannschaften = new ArrayList<>();
        Mannschaft m = new Mannschaft();
        m.setGeschlecht(GeschlechtEnum.K);
        mannschaften.add(m);
        g.setMannschaften(mannschaften);

        assertEquals(GeschlechtEnum.K, g.getGeschlecht());
    }

    @Test
    void testGetGeschlecht_Null() {
        Gruppe g = new Gruppe();
        assertNull(g.getGeschlecht());
    }

    // === addMannschaft ===

    @Test
    void testAddMannschaft_FirstMannschaft() throws TurnierException {
        Mannschaft m = createMannschaft(GeschlechtEnum.M, 5, 1);
        gruppe.addMannschaft(m);
        assertEquals(1, gruppe.getMannschaften().size());
    }

    @Test
    void testAddMannschaft_SecondMannschaft() throws TurnierException {
        Mannschaft m1 = createMannschaft(GeschlechtEnum.M, 5, 1);
        Mannschaft m2 = createMannschaft(GeschlechtEnum.M, 5, 2);
        // First call: size is 0, so it just adds (size <= 1)
        gruppe.addMannschaft(m1);
        // Second call: size is 1, so it still just adds (size <= 1)
        gruppe.addMannschaft(m2);
        assertEquals(2, gruppe.getMannschaften().size());
    }

    @Test
    void testAddMannschaft_ThirdSameGeschlecht() throws TurnierException {
        Mannschaft m1 = createMannschaft(GeschlechtEnum.M, 5, 1);
        Mannschaft m2 = createMannschaft(GeschlechtEnum.M, 5, 2);
        Mannschaft m3 = createMannschaft(GeschlechtEnum.M, 5, 3);
        gruppe.addMannschaft(m1);
        gruppe.addMannschaft(m2);
        // Third call: size is 2, validates gender consistency
        gruppe.addMannschaft(m3);
        assertEquals(3, gruppe.getMannschaften().size());
    }

    // === evaluateFloorKlasse ===

    @Test
    void testEvaluateFloorKlasse_NoMannschaften() {
        Gruppe g = new Gruppe();
        assertEquals(-1, g.evaluateFloorKlasse());
    }

    @Test
    void testEvaluateFloorKlasse_WithMannschaften() {
        Gruppe g = new Gruppe();
        List<Mannschaft> mannschaften = new ArrayList<>();
        Mannschaft m1 = new Mannschaft();
        m1.setKlasse(5);
        Mannschaft m2 = new Mannschaft();
        m2.setKlasse(3);
        Mannschaft m3 = new Mannschaft();
        m3.setKlasse(4);
        mannschaften.add(m1);
        mannschaften.add(m2);
        mannschaften.add(m3);
        g.setMannschaften(mannschaften);

        assertEquals(3, g.evaluateFloorKlasse());
    }

    @Test
    void testEvaluateFloorKlasse_SingleMannschaft() {
        Gruppe g = new Gruppe();
        List<Mannschaft> mannschaften = new ArrayList<>();
        Mannschaft m = new Mannschaft();
        m.setKlasse(6);
        mannschaften.add(m);
        g.setMannschaften(mannschaften);

        assertEquals(6, g.evaluateFloorKlasse());
    }
}
