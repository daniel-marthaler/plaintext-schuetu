package ch.plaintext.schuetu.model.comparators;

import ch.plaintext.schuetu.model.ranglistensortierung.RanglisteneintragZeile;

import java.util.Comparator;

/**
 * Vergleicht Ranglisteneintraege aufgrund erzielte Tore
 */
public class MehrToreComparator implements Comparator<RanglisteneintragZeile> {

    @Override
    public int compare(final RanglisteneintragZeile spA, final RanglisteneintragZeile spB) {
        if (spA.getToreErziehlt() > spB.getToreErziehlt()) {
            return -1;
        }

        if (spA.getToreErziehlt() == spB.getToreErziehlt()) {
            return 0;
        }

        return 1;
    }
}
