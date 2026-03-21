package ch.plaintext.schuetu.model.comparators;

import ch.plaintext.schuetu.entity.Mannschaft;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Vergleicht Mannschaften aufgrund deren Namen
 */
public class MannschaftsNameComparator implements Comparator<Mannschaft>, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public int compare(Mannschaft arg0, Mannschaft arg1) {
        return arg0.getName().compareTo(arg1.getName());
    }

}
