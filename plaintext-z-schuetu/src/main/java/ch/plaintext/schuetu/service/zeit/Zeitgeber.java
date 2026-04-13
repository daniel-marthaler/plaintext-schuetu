package ch.plaintext.schuetu.service.zeit;

import ch.plaintext.schuetu.service.Game;
import ch.plaintext.schuetu.service.GameConnectable;
import ch.plaintext.schuetu.service.spieldurchfuehrung.SpielDurchfuehrung;
import ch.plaintext.schuetu.model.enums.SpielPhasenEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Gibt die Zeit in Sekundenpulsen vor und uebermittelt diese seinem Game
 */
@Component
@Scope("prototype")
@Slf4j
public class Zeitgeber implements GameConnectable {

    @Setter
    public SpielDurchfuehrung durchfuehrung;

    @Setter
    @Getter
    private int add = 60;

    @Setter
    private Long zeitVorher = Long.valueOf(0);

    @Setter
    private Integer abweichungZuSpielzeit = Integer.valueOf(0);

    private Long zeit = Long.valueOf(0);

    @Getter
    private boolean gameRunning = false;

    private Game game;

    final SimpleDateFormat jscriptf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Scheduled(fixedRate = 1000)
    @Transactional
    public void run() {

        if (durchfuehrung != null) {
            durchfuehrung.pulse();
        }

        if (game == null) {
            log.debug("Zeitgeber: kein Game selektiert");
            return;
        }

        if (game.getModel().isStartJetzt()) {
            zeit = System.currentTimeMillis();
        } else {

            if (zeitVorher.longValue() != game.getModel().getStart().getTime()) {
                zeit = game.getModel().getStart().getTime();
                zeitVorher = game.getModel().getStart().getTime();
                abweichungZuSpielzeit = 0;
            }

            zeit = zeit + game.getModel().getVerschnellerungsFaktor() * 1000;
        }

        if (!gameRunning) {
            this.abweichungZuSpielzeit = this.abweichungZuSpielzeit - (game.getModel().getVerschnellerungsFaktor() * 1000);
        }

    }

    public synchronized void stopGame(String grund) {
        if (!isGameStarted()) {
            Zeitgeber.log.info("zeitgeber: stopGame() -> ohne effekt, weil bereits gestoppt:" + grund);
        } else {
            this.gameRunning = false;
            Zeitgeber.log.info("zeitgeber: stopGame() -> mit Grund: " + grund);
            abweichungZuSpielzeit = abweichungZuSpielzeit + (game.getModel().getVerschnellerungsFaktor() * 1000);
            Zeitgeber.log.info("zeitgeber: pause: " + this.abweichungZuSpielzeit / 1000 + " sekunden abweichung");

        }

    }

    public synchronized void startGame(int seconds, String grund) {
        Zeitgeber.log.info("zeitgeber: startGame() -> aufholung:" + grund + " -> " + seconds);
        this.abweichungZuSpielzeit = this.abweichungZuSpielzeit + seconds * 1000;
        Zeitgeber.log.info("zeitgeber: startGame() -> aufholung von " + seconds + " sekunden = abweichung: " + this.abweichungZuSpielzeit / 1000 + " sekunden");
        this.gameRunning = true;
    }

    public void spielzeitEinholen60() {
        if (this.abweichungZuSpielzeit < -60000) {
            this.abweichungZuSpielzeit = this.abweichungZuSpielzeit + 60000;
        } else {
            this.abweichungZuSpielzeit = 0;
        }
    }

    public boolean isGameStarted() {
        return gameRunning;
    }

    public int getVerspaetung() {
        return abweichungZuSpielzeit / 1000;
    }

    public String spielzeitVerspaetung() {
        int sekunden = Math.abs(getVerspaetung());
        int rest = sekunden % 60;
        int minuten = sekunden / 60;
        DecimalFormat df2 = new DecimalFormat("00");
        return df2.format(minuten) + ":" + df2.format(rest);
    }

    @Override
    public Game getGame() {
        return game;
    }

    @Override
    public void setGame(Game game) {
        this.game = game;
    }

    public String getRichtigeZeit() {

        if (game.getModel().getPhase() != SpielPhasenEnum.F_SPIELEN && game.getModel().getPhase() != SpielPhasenEnum.G_ABGESCHLOSSEN) {
            return "ausgeschaltet in phase:  " + game.getModel().getPhase().toString();
        }

        final SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss (dd.MM.yy)");
        Date time = new Date(this.zeit);
        return fmt.format(time);
    }

    public long getRichtigeZeitMillis() {
        return zeit;
    }

    public long getSpielZeitInMillis() {
        return zeit + abweichungZuSpielzeit;
    }

    public String getSpielZeit() {

        if (game.getModel().getPhase() != SpielPhasenEnum.F_SPIELEN && game.getModel().getPhase() != SpielPhasenEnum.G_ABGESCHLOSSEN) {
            return "ausgeschaltet: " + game.getModel().getPhase().toString();
        }

        final SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss (dd.MM.yy)");
        return fmt.format(new Date(zeit + abweichungZuSpielzeit));
    }

    public void add() {
        this.zeit = this.zeit + (add * 1000);
    }

}
