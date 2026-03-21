package ch.plaintext.schuetu.model.comparators;

import ch.plaintext.schuetu.entity.Mannschaft;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Vergleicht Mannschaften aufgrund deren Namen und anschliessend aufgrund des
 * Erstellungsdatums (fuer Nummerierung)
 */
public class MannschaftsCreationDateComparator implements Comparator<Mannschaft>, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public int compare(Mannschaft arg0, Mannschaft arg1) {

        MannschaftsNameComparator comp = new MannschaftsNameComparator();

        int res = comp.compare(arg0, arg1);

        if (res == 0) {
            if (arg0.getCreationdate().before(arg1.getCreationdate())) {
                return -1;
            }

            if (arg0.getCreationdate().after(arg1.getCreationdate())) {
                return 1;
            }
        }

        return 0;
    }

}
