package ch.plaintext.schuetu.model.comparators;

import ch.plaintext.schuetu.entity.Kategorie;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Vergleicht Kategorien aufgrund deren Namen
 */
public class KategorieNameComparator implements Comparator<Kategorie>, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public int compare(Kategorie arg0, Kategorie arg1) {
        return arg0.getName().compareTo(arg1.getName());
    }
}
