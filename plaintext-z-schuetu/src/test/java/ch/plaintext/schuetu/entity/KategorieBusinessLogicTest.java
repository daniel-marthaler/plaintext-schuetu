package ch.plaintext.schuetu.entity;

import ch.plaintext.schuetu.model.enums.GeschlechtEnum;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class KategorieBusinessLogicTest {

    private Mannschaft createMannschaft(int teamNr, int klasse, GeschlechtEnum geschlecht) {
        Mannschaft m = new Mannschaft();
        m.setTeamNummer(teamNr);
        m.setKlasse(klasse);
        m.setGeschlecht(geschlecht);
        m.setFarbe("");
        m.setSchulhaus("");
        return m;
    }

    private Kategorie createKategorieWithMannschaften(int count, int klasse) {
        Kategorie kat = new Kategorie();
        Gruppe gruppeA = new Gruppe();
        gruppeA.setGeschlecht(GeschlechtEnum.M);
        gruppeA.setKategorie(kat);

        List<Mannschaft> mannschaften = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            Mannschaft m = createMannschaft(i, klasse, GeschlechtEnum.M);
            m.setGruppe(gruppeA);
            mannschaften.add(m);
        }
        gruppeA.setMannschaften(mannschaften);
        kat.setGruppeA(gruppeA);
        kat.setGruppeB(new Gruppe());
        return kat;
    }

    @Test
    void testGetName() {
        Kategorie kat = createKategorieWithMannschaften(5, 5);
        assertEquals("MKl5", kat.getName());
    }

    @Test
    void testGetName_NoGruppeA() {
        Kategorie kat = new Kategorie();
        assertEquals("invalide", kat.getName());
    }

    @Test
    void testGetKlassen_SingleKlasse() {
        Kategorie kat = createKategorieWithMannschaften(5, 5);
        List<Integer> klassen = kat.getKlassen();
        assertEquals(1, klassen.size());
        assertEquals(5, klassen.get(0));
    }

    @Test
    void testGetKlassen_MultipleKlassen() {
        Kategorie kat = new Kategorie();
        Gruppe gruppeA = new Gruppe();
        gruppeA.setGeschlecht(GeschlechtEnum.M);
        gruppeA.setKategorie(kat);

        List<Mannschaft> mannschaften = new ArrayList<>();
        mannschaften.add(createMannschaft(1, 4, GeschlechtEnum.M));
        mannschaften.add(createMannschaft(2, 5, GeschlechtEnum.M));
        for (Mannschaft m : mannschaften) { m.setGruppe(gruppeA); }
        gruppeA.setMannschaften(mannschaften);
        kat.setGruppeA(gruppeA);
        kat.setGruppeB(new Gruppe());

        List<Integer> klassen = kat.getKlassen();
        assertEquals(2, klassen.size());
        assertEquals(4, klassen.get(0));
        assertEquals(5, klassen.get(1));
    }

    @Test
    void testGetMannschaften() {
        Kategorie kat = createKategorieWithMannschaften(5, 5);
        List<Mannschaft> mannschaften = kat.getMannschaften();
        assertEquals(5, mannschaften.size());
    }

    @Test
    void testHasVorUndRueckrunde_ThreeMannschaften() {
        Kategorie kat = createKategorieWithMannschaften(3, 5);
        assertTrue(kat.hasVorUndRueckrunde());
    }

    @Test
    void testHasVorUndRueckrunde_FiveMannschaften() {
        Kategorie kat = createKategorieWithMannschaften(5, 5);
        assertFalse(kat.hasVorUndRueckrunde());
    }

    @Test
    void testHas2Groups_NoGruppeB() {
        Kategorie kat = createKategorieWithMannschaften(5, 5);
        assertFalse(kat.has2Groups());
    }

    @Test
    void testHas2Groups_EmptyGruppeB() {
        Kategorie kat = createKategorieWithMannschaften(5, 5);
        Gruppe gruppeB = new Gruppe();
        gruppeB.setKategorie(kat);
        kat.setGruppeB(gruppeB);
        assertFalse(kat.has2Groups());
    }

    @Test
    void testIsMixedKlassen_SameKlasse() {
        Kategorie kat = createKategorieWithMannschaften(5, 5);
        assertFalse(kat.isMixedKlassen());
    }

    @Test
    void testIsMixedKlassen_DifferentKlassen() {
        Kategorie kat = new Kategorie();
        Gruppe gruppeA = new Gruppe();
        gruppeA.setGeschlecht(GeschlechtEnum.M);
        gruppeA.setKategorie(kat);

        List<Mannschaft> mannschaften = new ArrayList<>();
        mannschaften.add(createMannschaft(1, 4, GeschlechtEnum.M));
        mannschaften.add(createMannschaft(2, 5, GeschlechtEnum.M));
        for (Mannschaft m : mannschaften) { m.setGruppe(gruppeA); }
        gruppeA.setMannschaften(mannschaften);
        kat.setGruppeA(gruppeA);
        kat.setGruppeB(new Gruppe());

        assertTrue(kat.isMixedKlassen());
    }

    @Test
    void testComputeAnzahlFinale_SingleKlasse4Teams() {
        Kategorie kat = createKategorieWithMannschaften(4, 5);
        // 1 klasse with 4 teams: > 1 gives 1, > 3 gives 1 => total 2
        assertEquals(2, kat.computeAnzahlFinale());
    }

    @Test
    void testComputeAnzahlFinale_SingleKlasse3Teams() {
        Kategorie kat = createKategorieWithMannschaften(3, 5);
        // 1 klasse with 3 teams: > 1 gives 1, not > 3 => total 1
        assertEquals(1, kat.computeAnzahlFinale());
    }

    @Test
    void testIsMixedAndWithEinzelklasse_No() {
        Kategorie kat = createKategorieWithMannschaften(4, 5);
        assertFalse(kat.isMixedAndWithEinzelklasse());
    }

    @Test
    void testIsMixedAndWithEinzelklasse_Yes() {
        Kategorie kat = new Kategorie();
        Gruppe gruppeA = new Gruppe();
        gruppeA.setGeschlecht(GeschlechtEnum.M);
        gruppeA.setKategorie(kat);

        List<Mannschaft> mannschaften = new ArrayList<>();
        mannschaften.add(createMannschaft(1, 4, GeschlechtEnum.M));
        mannschaften.add(createMannschaft(2, 5, GeschlechtEnum.M));
        mannschaften.add(createMannschaft(3, 5, GeschlechtEnum.M));
        for (Mannschaft m : mannschaften) { m.setGruppe(gruppeA); }
        gruppeA.setMannschaften(mannschaften);
        kat.setGruppeA(gruppeA);
        kat.setGruppeB(new Gruppe());

        assertTrue(kat.isMixedAndWithEinzelklasse());
    }

    @Test
    void testGetGroessereMannschaftsGruppe() {
        Kategorie kat = new Kategorie();
        Gruppe gruppeA = new Gruppe();
        gruppeA.setGeschlecht(GeschlechtEnum.M);
        gruppeA.setKategorie(kat);

        List<Mannschaft> mannschaften = new ArrayList<>();
        mannschaften.add(createMannschaft(1, 4, GeschlechtEnum.M));
        mannschaften.add(createMannschaft(2, 5, GeschlechtEnum.M));
        mannschaften.add(createMannschaft(3, 5, GeschlechtEnum.M));
        for (Mannschaft m : mannschaften) { m.setGruppe(gruppeA); }
        gruppeA.setMannschaften(mannschaften);
        kat.setGruppeA(gruppeA);
        kat.setGruppeB(new Gruppe());

        List<Mannschaft> biggest = kat.getGroessereMannschaftsGruppe();
        assertEquals(2, biggest.size());
        assertEquals(5, biggest.get(0).getKlasse());
    }

    @Test
    void testGetKleinereMannschaftsGruppe() {
        Kategorie kat = new Kategorie();
        Gruppe gruppeA = new Gruppe();
        gruppeA.setGeschlecht(GeschlechtEnum.M);
        gruppeA.setKategorie(kat);

        List<Mannschaft> mannschaften = new ArrayList<>();
        mannschaften.add(createMannschaft(1, 4, GeschlechtEnum.M));
        mannschaften.add(createMannschaft(2, 5, GeschlechtEnum.M));
        mannschaften.add(createMannschaft(3, 5, GeschlechtEnum.M));
        for (Mannschaft m : mannschaften) { m.setGruppe(gruppeA); }
        gruppeA.setMannschaften(mannschaften);
        kat.setGruppeA(gruppeA);
        kat.setGruppeB(new Gruppe());

        List<Mannschaft> smallest = kat.getKleinereMannschaftsGruppe();
        assertEquals(1, smallest.size());
        assertEquals(4, smallest.get(0).getKlasse());
    }

    @Test
    void testGetSpiele_EmptyGroups() {
        Kategorie kat = new Kategorie();
        assertTrue(kat.getSpiele().isEmpty());
    }

    @Test
    void testEvaluateLowestClass() {
        Kategorie kat = new Kategorie();
        Gruppe gruppeA = new Gruppe();
        gruppeA.setGeschlecht(GeschlechtEnum.M);
        gruppeA.setKategorie(kat);

        List<Mannschaft> mannschaften = new ArrayList<>();
        mannschaften.add(createMannschaft(1, 4, GeschlechtEnum.M));
        mannschaften.add(createMannschaft(2, 6, GeschlechtEnum.M));
        for (Mannschaft m : mannschaften) { m.setGruppe(gruppeA); }
        gruppeA.setMannschaften(mannschaften);
        kat.setGruppeA(gruppeA);
        kat.setGruppeB(new Gruppe());

        assertEquals(4, kat.evaluateLowestClass());
    }

    @Test
    void testKlassenString() {
        Kategorie kat = createKategorieWithMannschaften(4, 5);
        assertEquals("5", kat.getKlassenString());
    }
}
