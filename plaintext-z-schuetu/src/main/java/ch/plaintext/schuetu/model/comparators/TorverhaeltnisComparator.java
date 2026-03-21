package ch.plaintext.schuetu.model.comparators;

import ch.plaintext.schuetu.model.ranglistensortierung.RanglisteneintragZeile;

import java.util.Comparator;

/**
 * Vergleicht Ranglisteneintraege aufgrund Tordifferenz
 */
public class TorverhaeltnisComparator implements Comparator<RanglisteneintragZeile> {

    @Override
    public int compare(final RanglisteneintragZeile spA, final RanglisteneintragZeile spB) {
        if (spA.getTordifferenz() > spB.getTordifferenz()) {
            return -1;
        }

        if (spA.getTordifferenz() == spB.getTordifferenz()) {
            return 0;
        }

        return 1;
    }

}
