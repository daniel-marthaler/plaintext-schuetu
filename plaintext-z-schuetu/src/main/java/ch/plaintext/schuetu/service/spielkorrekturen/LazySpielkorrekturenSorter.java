package ch.plaintext.schuetu.service.spielkorrekturen;

import org.primefaces.model.SortOrder;

import java.lang.reflect.Field;
import java.util.Comparator;

public class LazySpielkorrekturenSorter implements Comparator<SpielKorrektur> {

    private String sortField;

    private SortOrder sortOrder;

    public LazySpielkorrekturenSorter(String sortField, SortOrder sortOrder) {
        this.sortField = sortField;
        this.sortOrder = sortOrder;
    }

    public int compare(SpielKorrektur car1, SpielKorrektur car2) {
        try {

            Field field = SpielKorrektur.class.getDeclaredField(this.sortField);
            field.setAccessible(true);
            String fieldValue1 = String.valueOf(field.get(car1));
            String fieldValue2 = String.valueOf(field.get(car2));

            int value = fieldValue1.compareTo(fieldValue2);

            return SortOrder.ASCENDING.equals(sortOrder) ? value : -1 * value;
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }
}
