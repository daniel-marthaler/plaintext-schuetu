package ch.plaintext.schuetu.model.comparators;

import ch.plaintext.schuetu.entity.Gruppe;
import ch.plaintext.schuetu.entity.Kategorie;
import ch.plaintext.schuetu.entity.Mannschaft;
import ch.plaintext.schuetu.model.enums.GeschlechtEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class KategorieNameComparatorTest {

    private KategorieNameComparator comparator;

    @BeforeEach
    void setUp() {
        comparator = new KategorieNameComparator();
    }

    private Kategorie createKategorie(GeschlechtEnum geschlecht, int klasse) {
        Kategorie k = new Kategorie();
        Gruppe g = new Gruppe();
        g.setKategorie(k);
        k.setGruppeA(g);

        Mannschaft m = new Mannschaft();
        m.setGeschlecht(geschlecht);
        m.setKlasse(klasse);
        m.setTeamNummer(1);
        m.setGruppe(g);

        List<Mannschaft> mannschaften = new ArrayList<>();
        mannschaften.add(m);
        g.setMannschaften(mannschaften);

        return k;
    }

    @Test
    void testCompare_SameName() {
        Kategorie k1 = createKategorie(GeschlechtEnum.M, 5);
        Kategorie k2 = createKategorie(GeschlechtEnum.M, 5);
        assertEquals(0, comparator.compare(k1, k2));
    }

    @Test
    void testCompare_DifferentGeschlecht() {
        Kategorie k1 = createKategorie(GeschlechtEnum.K, 5);
        Kategorie k2 = createKategorie(GeschlechtEnum.M, 5);
        assertTrue(comparator.compare(k1, k2) < 0);
    }

    @Test
    void testCompare_DifferentKlasse() {
        Kategorie k1 = createKategorie(GeschlechtEnum.M, 3);
        Kategorie k2 = createKategorie(GeschlechtEnum.M, 5);
        assertTrue(comparator.compare(k1, k2) < 0);
    }

    @Test
    void testSort() {
        Kategorie k1 = createKategorie(GeschlechtEnum.M, 5);
        Kategorie k2 = createKategorie(GeschlechtEnum.K, 3);
        Kategorie k3 = createKategorie(GeschlechtEnum.M, 3);

        List<Kategorie> list = Arrays.asList(k1, k2, k3);
        list.sort(comparator);

        // K should come before M, and within M: 3 before 5
        assertEquals(GeschlechtEnum.K, list.get(0).getGruppeA().getGeschlecht());
    }
}
