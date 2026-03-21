package ch.plaintext.schuetu.model.comparators;

import ch.plaintext.schuetu.entity.Spiel;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Vergleicht Spiele aufgrund ihrer beiden Mannschaftsnamen
 */
public class SpielMannschaftsnamenComparator implements Comparator<Spiel>, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public int compare(Spiel arg0, Spiel arg1) {

        String aName = arg0.getMannschaftAName() + arg0.getMannschaftBName();
        String bName = arg1.getMannschaftAName() + arg1.getMannschaftBName();

        if (aName.compareTo(bName) < 0) {
            return -1;
        }

        if (aName.compareTo(bName) > 0) {
            return 1;

        }

        return 0;
    }

}
