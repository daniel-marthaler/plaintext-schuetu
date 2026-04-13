package ch.plaintext.schuetu.service.utils;

import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class DateUtilTest2 {

    @Test
    void testGetShortTimeDayString_Saturday() {
        Calendar cal = Calendar.getInstance();
        cal.set(2026, Calendar.JUNE, 6, 10, 30, 0);
        Date date = cal.getTime();

        String result = DateUtil.getShortTimeDayString(date);
        assertNotNull(result);
        assertTrue(result.contains("10:30"));
    }

    @Test
    void testGetShortTimeDayString_ContainsDayAndTime() {
        Calendar cal = Calendar.getInstance();
        cal.set(2026, Calendar.JUNE, 7, 14, 0, 0);
        Date date = cal.getTime();

        String result = DateUtil.getShortTimeDayString(date);
        assertNotNull(result);
        // Should contain hour:minute format
        assertTrue(result.contains("14:00"));
    }

    @Test
    void testGetShortTimeDayString_NotNull() {
        String result = DateUtil.getShortTimeDayString(new Date());
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }
}
