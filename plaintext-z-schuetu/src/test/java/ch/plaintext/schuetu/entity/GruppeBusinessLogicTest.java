package ch.plaintext.schuetu.entity;

import ch.plaintext.schuetu.model.TurnierException;
import ch.plaintext.schuetu.model.enums.GeschlechtEnum;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GruppeBusinessLogicTest {

    private Mannschaft createMannschaft(int teamNr, int klasse, GeschlechtEnum geschlecht) {
        Mannschaft m = new Mannschaft();
        m.setTeamNummer(teamNr);
        m.setKlasse(klasse);
        m.setGeschlecht(geschlecht);
        m.setFarbe("");
        m.setSchulhaus("");
        return m;
    }

    @Test
    void testAddMannschaft_FirstMannschaft() throws TurnierException {
        Gruppe g = new Gruppe();
        Mannschaft m = createMannschaft(1, 5, GeschlechtEnum.M);
        g.addMannschaft(m);
        assertEquals(1, g.getMannschaften().size());
    }

    @Test
    void testAddMannschaft_SecondMannschaft() throws TurnierException {
        Gruppe g = new Gruppe();
        Mannschaft m1 = createMannschaft(1, 5, GeschlechtEnum.M);
        Mannschaft m2 = createMannschaft(2, 5, GeschlechtEnum.M);
        g.addMannschaft(m1);
        g.addMannschaft(m2);
        assertEquals(2, g.getMannschaften().size());
    }

    @Test
    void testAddMannschaft_MixedGenderThrows() throws TurnierException {
        Gruppe g = new Gruppe();
        Mannschaft m1 = createMannschaft(1, 5, GeschlechtEnum.M);
        Mannschaft m2 = createMannschaft(2, 5, GeschlechtEnum.K);
        Mannschaft m3 = createMannschaft(3, 5, GeschlechtEnum.M);

        g.addMannschaft(m1);
        g.addMannschaft(m2);

        // The check compares existing mannschaften against basis (index 0 = M).
        // m2 is K which differs from basis M, so adding m3 should throw.
        assertThrows(TurnierException.class, () -> g.addMannschaft(m3));
    }

    @Test
    void testGetGeschlecht_FromMannschaften() {
        Gruppe g = new Gruppe();
        Mannschaft m = createMannschaft(1, 5, GeschlechtEnum.K);
        g.setMannschaften(List.of(m));

        assertEquals(GeschlechtEnum.K, g.getGeschlecht());
    }

    @Test
    void testGetGeschlecht_Explicit() {
        Gruppe g = new Gruppe();
        g.setGeschlecht(GeschlechtEnum.M);
        assertEquals(GeschlechtEnum.M, g.getGeschlecht());
    }

    @Test
    void testEvaluateFloorKlasse_Empty() {
        Gruppe g = new Gruppe();
        assertEquals(-1, g.evaluateFloorKlasse());
    }

    @Test
    void testEvaluateFloorKlasse_WithMannschaften() {
        Gruppe g = new Gruppe();
        List<Mannschaft> mannschaften = new ArrayList<>();
        mannschaften.add(createMannschaft(1, 5, GeschlechtEnum.M));
        mannschaften.add(createMannschaft(2, 3, GeschlechtEnum.M));
        mannschaften.add(createMannschaft(3, 7, GeschlechtEnum.M));
        g.setMannschaften(mannschaften);

        assertEquals(3, g.evaluateFloorKlasse());
    }

    @Test
    void testGetName_WithKategorie() {
        Kategorie kat = new Kategorie();
        Gruppe gruppeA = new Gruppe();
        gruppeA.setGeschlecht(GeschlechtEnum.M);
        gruppeA.setKategorie(kat);

        List<Mannschaft> mannschaften = new ArrayList<>();
        mannschaften.add(createMannschaft(1, 5, GeschlechtEnum.M));
        for (Mannschaft m : mannschaften) { m.setGruppe(gruppeA); }
        gruppeA.setMannschaften(mannschaften);
        kat.setGruppeA(gruppeA);

        String name = gruppeA.getName();
        assertTrue(name.contains("M"));
        assertTrue(name.contains("5"));
    }

    @Test
    void testGetName_NoKategorie() {
        Gruppe g = new Gruppe();
        g.setGeschlecht(GeschlechtEnum.M);
        String name = g.getName();
        assertTrue(name.contains("OHNE_KATEGORIE"));
    }

    @Test
    void testDefaultValues() {
        Gruppe g = new Gruppe();
        assertNotNull(g.getMannschaften());
        assertNotNull(g.getSpiele());
        assertNotNull(g.getCreationdate());
    }
}
