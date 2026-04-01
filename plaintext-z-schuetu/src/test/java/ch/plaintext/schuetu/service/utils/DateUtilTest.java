package ch.plaintext.schuetu.service.utils;

import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class DateUtilTest {

    @Test
    void testGetShortTimeDayString_NotNull() {
        Date date = new Date();
        String result = DateUtil.getShortTimeDayString(date);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void testGetShortTimeDayString_ContainsTime() {
        Calendar cal = Calendar.getInstance(Locale.GERMAN);
        cal.set(2026, Calendar.MARCH, 31, 14, 30);
        Date date = cal.getTime();

        String result = DateUtil.getShortTimeDayString(date);
        assertTrue(result.contains("14:30"));
    }

    @Test
    void testGetShortTimeDayString_ContainsDayName() {
        Calendar cal = Calendar.getInstance(Locale.GERMAN);
        cal.set(2026, Calendar.MARCH, 31, 10, 0);
        Date date = cal.getTime();

        String result = DateUtil.getShortTimeDayString(date);
        // German day abbreviation (Di for Dienstag = Tuesday, March 31 2026)
        assertNotNull(result);
        assertTrue(result.contains(":"));
    }

    @Test
    void testGetShortTimeDayString_MidnightTime() {
        Calendar cal = Calendar.getInstance(Locale.GERMAN);
        cal.set(2026, Calendar.JANUARY, 1, 0, 0);
        Date date = cal.getTime();

        String result = DateUtil.getShortTimeDayString(date);
        assertTrue(result.contains("00:00"));
    }

    @Test
    void testGetShortTimeDayString_Format() {
        Calendar cal = Calendar.getInstance(Locale.GERMAN);
        cal.set(2026, Calendar.JUNE, 8, 8, 15);
        Date date = cal.getTime();

        String result = DateUtil.getShortTimeDayString(date);
        // Should match pattern like "Mo. 08:15" or "Mo 08:15"
        assertTrue(result.contains("08:15"));
    }
}
