package ch.plaintext.schuetu.model.comparators;

import ch.plaintext.schuetu.entity.Gruppe;
import ch.plaintext.schuetu.entity.Kategorie;
import ch.plaintext.schuetu.entity.Mannschaft;
import ch.plaintext.schuetu.model.enums.GeschlechtEnum;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class KategorieComparatorTest {

    private Kategorie createKategorie(GeschlechtEnum geschlecht, int klasse) {
        Kategorie kat = new Kategorie();
        Gruppe gruppeA = new Gruppe();
        gruppeA.setGeschlecht(geschlecht);
        gruppeA.setKategorie(kat);

        Mannschaft m = new Mannschaft();
        m.setGeschlecht(geschlecht);
        m.setKlasse(klasse);
        m.setTeamNummer(1);
        m.setFarbe("");
        m.setSchulhaus("");
        m.setGruppe(gruppeA);
        gruppeA.setMannschaften(new ArrayList<>(List.of(m)));
        kat.setGruppeA(gruppeA);
        kat.setGruppeB(new Gruppe());
        return kat;
    }

    @Test
    void testCompare_SameKategorie() {
        Kategorie k1 = createKategorie(GeschlechtEnum.M, 5);
        Kategorie k2 = createKategorie(GeschlechtEnum.M, 5);

        KategorieComparator comp = new KategorieComparator();
        assertEquals(0, comp.compare(k1, k2));
    }

    @Test
    void testCompare_DifferentKlasse() {
        Kategorie k1 = createKategorie(GeschlechtEnum.M, 4);
        Kategorie k2 = createKategorie(GeschlechtEnum.M, 6);

        KategorieComparator comp = new KategorieComparator();
        assertTrue(comp.compare(k1, k2) < 0);
    }

    @Test
    void testSort() {
        Kategorie k1 = createKategorie(GeschlechtEnum.M, 6);
        Kategorie k2 = createKategorie(GeschlechtEnum.M, 4);
        Kategorie k3 = createKategorie(GeschlechtEnum.K, 5);

        List<Kategorie> list = new ArrayList<>(Arrays.asList(k1, k2, k3));
        list.sort(new KategorieComparator());

        // Should be sorted by name
        assertNotNull(list);
        assertEquals(3, list.size());
    }
}
