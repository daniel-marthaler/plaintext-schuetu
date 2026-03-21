package ch.plaintext.schuetu.service.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtil {

    public static String getShortTimeDayString(Date d) {
        SimpleDateFormat fm = new SimpleDateFormat("EEE HH:mm", Locale.GERMAN);
        return fm.format(d);
    }

}
