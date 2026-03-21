package ch.plaintext.schuetu.model.ranglistensortierung;

import ch.plaintext.schuetu.model.comparators.MehrToreComparator;
import ch.plaintext.schuetu.model.enums.RangierungsgrundEnum;

import java.util.ArrayList;
import java.util.List;

/**
 * Auslagerung der Logik, welche nach mehr Toren sortiert
 */
public class MehrToreSortierer {

    private MehrToreSortierer() {

    }

    public static void sortNachMehrToren(RanglisteneintragHistorie ranglisteneintragHistorie) {

        List<RanglisteneintragZeile> zeilen = ranglisteneintragHistorie.getZeilen();

        final List<RanglisteneintragZeile> su = new ArrayList<>();
        int startindex = -1;
        RanglisteneintragZeile last = null;
        for (int i = 0; i < zeilen.size(); i++) {
            final RanglisteneintragZeile temp = zeilen.get(i);

            if ((su.size() > 1) && (last != null) && ((last.getTordifferenz() != temp.getTordifferenz()) || (last.getPunkte() != temp.getPunkte()))) {
                subSortNachMehrToren(su, startindex, ranglisteneintragHistorie);
                startindex = -1;
                su.clear();
            }

            if (temp.getRangierungsgrund().equals(RangierungsgrundEnum.WEITERSUCHEN)) {

                temp.setRangierungsgrund(RangierungsgrundEnum.MEHRTORE);

                su.add(temp);

                if (su.size() == 1) {
                    startindex = i;
                }
            }
            last = temp;
        }
        subSortNachMehrToren(su, startindex, ranglisteneintragHistorie);

    }

    private static void subSortNachMehrToren(final List<RanglisteneintragZeile> su, int startindexIn, RanglisteneintragHistorie ranglisteneintragHistorie) {

        int startindex = startindexIn;
        if (su.size() > 1) {

            su.sort(new MehrToreComparator());
            RanglisteneintragZeile last = null;

            for (final RanglisteneintragZeile ranglisteneintragZeile : su) {

                if (last != null) {
                    if (last.getToreErziehlt() == ranglisteneintragZeile.getToreErziehlt()) {
                        ranglisteneintragZeile.setRangierungsgrund(RangierungsgrundEnum.WEITERSUCHEN);
                        last.setRangierungsgrund(RangierungsgrundEnum.WEITERSUCHEN);
                    }
                }
                last = ranglisteneintragZeile;
            }

            for (final RanglisteneintragZeile ranglisteneintragZeile : su) {
                ranglisteneintragHistorie.getZeilen().set(startindex, ranglisteneintragZeile);
                startindex = startindex + 1;
            }

            su.clear();
        }
    }
}
