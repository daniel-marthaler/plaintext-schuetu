package ch.plaintext.schuetu.model.comparators;

import ch.plaintext.schuetu.entity.Gruppe;
import ch.plaintext.schuetu.entity.Kategorie;
import ch.plaintext.schuetu.entity.Mannschaft;
import ch.plaintext.schuetu.model.enums.GeschlechtEnum;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class KategorieKlasseUndGeschlechtComparatorTest {

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
    void testCompare_DifferentKlasse() {
        Kategorie k1 = createKategorie(GeschlechtEnum.M, 4);
        Kategorie k2 = createKategorie(GeschlechtEnum.M, 6);

        KategorieKlasseUndGeschlechtComparator comp = new KategorieKlasseUndGeschlechtComparator();
        assertTrue(comp.compare(k1, k2) < 0);
    }

    @Test
    void testCompare_SameKlasse_SameGeschlecht() {
        Kategorie k1 = createKategorie(GeschlechtEnum.M, 5);
        Kategorie k2 = createKategorie(GeschlechtEnum.M, 5);

        KategorieKlasseUndGeschlechtComparator comp = new KategorieKlasseUndGeschlechtComparator();
        assertEquals(0, comp.compare(k1, k2));
    }

    @Test
    void testCompare_SameKlasse_DifferentGeschlecht() {
        Kategorie k1 = createKategorie(GeschlechtEnum.K, 5);
        Kategorie k2 = createKategorie(GeschlechtEnum.M, 5);

        KategorieKlasseUndGeschlechtComparator comp = new KategorieKlasseUndGeschlechtComparator();
        assertNotEquals(0, comp.compare(k1, k2));
    }
}
