package ch.plaintext.schuetu.model.comparators;

import ch.plaintext.schuetu.entity.Kategorie;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Vergleicht Kategorien aufgrund Klasse und Geschlecht
 */
public class KategorieKlasseUndGeschlechtComparator implements Comparator<Kategorie>, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public int compare(Kategorie a, Kategorie b) {

        if (a.evaluateLowestClass() < b.evaluateLowestClass()) {
            return -1;
        }

        if (a.evaluateLowestClass() > b.evaluateLowestClass()) {
            return 1;
        }

        if (a.getGruppeA() == null || b.getGruppeB() == null) {
            return 0;
        }

        return a.getGruppeA().getGeschlecht().compareTo(b.getGruppeA().getGeschlecht());

    }
}
