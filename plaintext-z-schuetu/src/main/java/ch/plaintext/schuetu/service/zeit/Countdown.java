package ch.plaintext.schuetu.service.zeit;

import org.joda.time.DateTime;

import java.text.SimpleDateFormat;

/**
 * Zaehlt rueckwaerts bis zu einem bestimmen Zeitpunkt
 */
public class Countdown {

    private DateTime ablauf;
    private DateTime letzte;
    private int dauer;

    private int sekundenToGo = 0;

    public Countdown(final DateTime jetzt, final int dauerInSeconds) {
        super();
        this.dauer = dauerInSeconds;
        this.ablauf = jetzt.plusSeconds(dauerInSeconds);
        this.letzte = jetzt;

        this.sekundenToGo = dauerInSeconds;

    }

    public void aufholen(int sekunden) {
        this.sekundenToGo = this.sekundenToGo - sekunden;
    }

    // speaker.xhtml
    public int getSekundenToGo() {
        if (sekundenToGo < 1) {
            return 0;
        }
        return sekundenToGo;
    }

    public Countdown(final DateTime jetzt, final DateTime bis) {
        super();
        this.dauer = bis.getSecondOfDay() - jetzt.getSecondOfDay();
        this.ablauf = bis;
        this.letzte = jetzt;
        this.sekundenToGo = this.dauer;
    }

    public void signalTime(final DateTime jetzt) {
        sekundenToGo = (int) (sekundenToGo - (jetzt.getMillis() - letzte.getMillis()) / 1000);
        this.letzte = jetzt;
    }

    public String getZeit() {
        final DateTime t = this.ablauf.minus(this.letzte.getMillis());

        if (sekundenToGo < 0) {
            return "00:00";
        }

        final SimpleDateFormat fmt = new SimpleDateFormat("mm:ss");
        String temp = fmt.format(t.toDate());
        if (temp.equals("59:59")) {
            temp = "00:00";
        }
        return temp;
    }

    public int getSecondsPlus2() {
        final DateTime t = this.ablauf.minus(this.letzte.getMillis());
        final int minutes = t.getMinuteOfHour();
        final int seconds = t.getSecondOfMinute();
        return (minutes * 60 * 1000) + (seconds * 1000) + 2000;
    }

    public boolean isFertig() {
        return getZeit().equals("00:00");
    }

}
