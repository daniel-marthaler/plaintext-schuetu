package ch.plaintext.schuetu.model.comparators;

import ch.plaintext.schuetu.entity.Penalty;
import ch.plaintext.schuetu.model.ranglistensortierung.RanglisteneintragZeile;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;

/**
 * Vergleicht Ranglisteneintraege aufgrund Penalty-Rang
 */
@Slf4j
public class PenaltyComparator implements Comparator<RanglisteneintragZeile> {

    private Penalty p = null;

    public PenaltyComparator(final Penalty p) {
        this.p = p;
    }

    @Override
    public int compare(final RanglisteneintragZeile spA, final RanglisteneintragZeile spB) {

        try {
            if (this.p.getRang(spA.getMannschaft()) < this.p.getRang(spB.getMannschaft())) {
                return -1;
            }
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
        }

        try {
            if (this.p.getRang(spA.getMannschaft()) == this.p.getRang(spB.getMannschaft())) {
                return 0;
            }
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
        }

        return 1;
    }
}
