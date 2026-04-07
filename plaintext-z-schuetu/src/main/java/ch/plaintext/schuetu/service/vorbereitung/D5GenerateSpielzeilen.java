package ch.plaintext.schuetu.service.vorbereitung;

import ch.plaintext.schuetu.service.Game;
import ch.plaintext.schuetu.entity.SpielZeile;
import ch.plaintext.schuetu.model.enums.SpielTageszeit;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class D5GenerateSpielzeilen {

    private static final int MITTAG = 12;

    public List<SpielZeile> initZeilen(boolean sonntag, Game game) {
        List<SpielZeile> zeilen;
        DateTime st = new DateTime(game.getModel().getStart(), DateTimeZone.forID("Europe/Zurich"));
        DateTime samstagD = new DateTime(st);
        DateTime sonntagD = samstagD.plusDays(1);
        if (!sonntag) {
            zeilen = createZeilen(samstagD, false, game.getModel().getPause(), game.getModel().getSpiellaengefinale(), game.getModel().getSpiellaenge());
        } else {
            zeilen = createZeilen(sonntagD, true, game.getModel().getPause(), game.getModel().getSpiellaengefinale(), game.getModel().getSpiellaenge());
        }
        return zeilen;
    }

    private List<SpielZeile> createZeilen(DateTime startIn, final boolean sonntag, Integer pause, Integer finallaenge, Integer laenge) {
        DateTime start = startIn;
        final int millis = start.getMillisOfDay();
        start = start.minusMillis(millis);
        start = start.plusHours(8);
        final DateTime end = start.plusHours(11);
        final List<SpielZeile> zeilen = new ArrayList<>();
        while (start.isBefore(end.getMillis())) {
            final SpielZeile zeile = new SpielZeile();
            if ((start.getHourOfDay() > MITTAG) && sonntag) { zeile.setFinale(true); }
            if (sonntag && (start.getHourOfDay() <= MITTAG)) { zeile.setSpieltageszeit(SpielTageszeit.SONNTAGMORGEN); }
            if (!sonntag && (start.getHourOfDay() < MITTAG)) { zeile.setSpieltageszeit(SpielTageszeit.SAMSTAGMORGEN); }
            if (!sonntag && (start.getHourOfDay() >= MITTAG)) { zeile.setSpieltageszeit(SpielTageszeit.SAMSTAGNACHMITTAG); }
            zeile.setStart(start.toDate());
            zeilen.add(zeile);
            zeile.setSonntag(sonntag);
            if (!zeile.isFinale()) { start = start.plusMinutes(pause + laenge); } else { start = start.plusMinutes(pause + finallaenge); }
        }
        return zeilen;
    }
}
