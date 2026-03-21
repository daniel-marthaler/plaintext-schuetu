package ch.plaintext.schuetu.model.comparators;

import ch.plaintext.schuetu.entity.Spiel;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Vergleicht Spiele aufgrund ihrer Startzeit
 */
public class SpielZeitComparator implements Comparator<Spiel>, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public int compare(Spiel arg0, Spiel arg1) {

        if (arg0 == null || arg1 == null || arg0.getStart() == null || arg1.getStart() == null) {
            return 0;
        }

        if (arg0.getStart().getTime() < arg1.getStart().getTime()) {
            return -1;
        }

        if (arg0.getStart().getTime() > arg1.getStart().getTime()) {
            return 1;

        }

        return 0;
    }

}
