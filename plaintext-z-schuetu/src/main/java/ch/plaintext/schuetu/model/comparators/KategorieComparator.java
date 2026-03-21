package ch.plaintext.schuetu.model.comparators;

import ch.plaintext.schuetu.entity.Kategorie;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

/**
 * Vergleicht Kategorien aufgrund Geschlecht und Klasse
 */
public class KategorieComparator implements Comparator<Kategorie>, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public int compare(Kategorie arg0, Kategorie arg1) {
        int geschlecht = arg0.getGruppeA().getGeschlecht().compareTo(arg1.getGruppeA().getGeschlecht());
        if (geschlecht == 0) {

            List<Integer> klassenA = arg0.getKlassen();
            Integer klA = 0;
            for (Integer klasseEnum : klassenA) {
                if (klA < klasseEnum) {
                    klA = klasseEnum;
                }
            }
            List<Integer> klassenB = arg1.getKlassen();
            Integer klB = 0;
            for (Integer klasseEnum : klassenB) {
                if (klB < klasseEnum) {
                    klB = klasseEnum;
                }
            }

            return klA.compareTo(klB);
        }
        return 0;
    }
}
