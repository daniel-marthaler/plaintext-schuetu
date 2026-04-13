package ch.plaintext.schuetu.service.zeit;

import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CountdownTest {

    @Test
    void testCountdownWithDuration() {
        DateTime now = new DateTime(2026, 6, 8, 10, 0, 0);
        Countdown countdown = new Countdown(now, 600); // 10 minutes

        assertEquals(600, countdown.getSekundenToGo());
        assertFalse(countdown.isFertig());
    }

    @Test
    void testCountdownWithTargetTime() {
        DateTime now = new DateTime(2026, 6, 8, 10, 0, 0);
        DateTime target = new DateTime(2026, 6, 8, 10, 10, 0);
        Countdown countdown = new Countdown(now, target);

        assertEquals(600, countdown.getSekundenToGo());
        assertFalse(countdown.isFertig());
    }

    @Test
    void testAufholen() {
        DateTime now = new DateTime(2026, 6, 8, 10, 0, 0);
        Countdown countdown = new Countdown(now, 600);

        countdown.aufholen(60);
        assertEquals(540, countdown.getSekundenToGo());
    }

    @Test
    void testSignalTime_AdvancesCountdown() {
        DateTime now = new DateTime(2026, 6, 8, 10, 0, 0);
        Countdown countdown = new Countdown(now, 600);

        DateTime later = now.plusSeconds(10);
        countdown.signalTime(later);

        assertEquals(590, countdown.getSekundenToGo());
    }

    @Test
    void testSignalTime_MultipleSignals() {
        DateTime now = new DateTime(2026, 6, 8, 10, 0, 0);
        Countdown countdown = new Countdown(now, 60);

        DateTime later1 = now.plusSeconds(20);
        countdown.signalTime(later1);
        assertEquals(40, countdown.getSekundenToGo());

        DateTime later2 = later1.plusSeconds(20);
        countdown.signalTime(later2);
        assertEquals(20, countdown.getSekundenToGo());
    }

    @Test
    void testGetSekundenToGo_NeverBelowZero() {
        DateTime now = new DateTime(2026, 6, 8, 10, 0, 0);
        Countdown countdown = new Countdown(now, 10);

        DateTime later = now.plusSeconds(20);
        countdown.signalTime(later);

        assertEquals(0, countdown.getSekundenToGo());
    }

    @Test
    void testIsFertig_WhenTimeExpired() {
        DateTime now = new DateTime(2026, 6, 8, 10, 0, 0);
        Countdown countdown = new Countdown(now, 10);

        DateTime later = now.plusSeconds(15);
        countdown.signalTime(later);

        assertTrue(countdown.isFertig());
    }

    @Test
    void testIsFertig_WhenTimeNotExpired() {
        DateTime now = new DateTime(2026, 6, 8, 10, 0, 0);
        Countdown countdown = new Countdown(now, 600);

        assertFalse(countdown.isFertig());
    }

    @Test
    void testGetZeit_Formatted() {
        DateTime now = new DateTime(2026, 6, 8, 10, 0, 0);
        Countdown countdown = new Countdown(now, 600);

        String zeit = countdown.getZeit();
        assertNotNull(zeit);
    }

    @Test
    void testGetZeit_WhenExpired() {
        DateTime now = new DateTime(2026, 6, 8, 10, 0, 0);
        Countdown countdown = new Countdown(now, 5);

        DateTime later = now.plusSeconds(10);
        countdown.signalTime(later);

        assertEquals("00:00", countdown.getZeit());
    }

    @Test
    void testAufholen_MultipleTimes() {
        DateTime now = new DateTime(2026, 6, 8, 10, 0, 0);
        Countdown countdown = new Countdown(now, 600);

        countdown.aufholen(60);
        countdown.aufholen(60);

        assertEquals(480, countdown.getSekundenToGo());
    }
}
