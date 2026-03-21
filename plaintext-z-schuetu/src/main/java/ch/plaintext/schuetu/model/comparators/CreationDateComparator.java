package ch.plaintext.schuetu.model.comparators;

import ch.plaintext.schuetu.model.CreationDateProvider;

import java.util.Comparator;

/**
 * Vergleicht Objekte aufgrund ihres Erstellungsdatums
 */
public class CreationDateComparator implements Comparator<CreationDateProvider> {

    @Override
    public int compare(CreationDateProvider arg0, CreationDateProvider arg1) {

        if (arg0.getCreationdate().before(arg1.getCreationdate())) {
            return -1;
        }

        if (arg0.getCreationdate().after(arg1.getCreationdate())) {
            return 1;
        }

        return 0;
    }

}
