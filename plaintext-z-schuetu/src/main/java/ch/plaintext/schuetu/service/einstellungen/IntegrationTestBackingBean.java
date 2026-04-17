package ch.plaintext.schuetu.service.einstellungen;

import ch.plaintext.schuetu.entity.Spiel;
import ch.plaintext.schuetu.service.Game;
import ch.plaintext.schuetu.service.GameSelectionHolder;
import ch.plaintext.schuetu.service.ResultateVerarbeiter;
import ch.plaintext.schuetu.service.spieldurchfuehrung.SpielDurchfuehrung;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Backing Bean fuer den Integrationstest-Tab.
 * Ermoeglicht automatisches Durchspielen eines kompletten Turniers.
 * Nur aktiv wenn der Spielname mit "Test" beginnt.
 */
@Component
@Scope("session")
@Slf4j
public class IntegrationTestBackingBean {

    @Autowired
    private GameSelectionHolder gameSelectionHolder;

    @Autowired
    private IntegrationTestService testService;

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(3);

    private ScheduledFuture<?> speakerFuture;
    private ScheduledFuture<?> schiriFuture;
    private ScheduledFuture<?> kontrolleurFuture;

    // Erfasste Referenzen fuer Background-Threads
    private SpielDurchfuehrung capturedDurchfuehrung;
    private ResultateVerarbeiter capturedResultate;
    private String capturedGameName;

    @Getter
    private boolean speakerRunning = false;
    @Getter
    private boolean schiriRunning = false;
    @Getter
    private boolean kontrolleurRunning = false;
    @Getter
    private String speakerStatus = "Gestoppt";
    @Getter
    private String schiriStatus = "Gestoppt";
    @Getter
    private String kontrolleurStatus = "Gestoppt";

    public boolean isTestGame() {
        if (!gameSelectionHolder.hasGame()) {
            return false;
        }
        String gameName = gameSelectionHolder.getGame().getModel().getGameName();
        return gameName != null && gameName.startsWith("Test");
    }

    private void captureGameReferences() {
        Game game = gameSelectionHolder.getGame();
        capturedDurchfuehrung = game.getDurchfuehrung();
        capturedResultate = game.getResultate();
        capturedGameName = game.getModel().getGameName();
    }

    // --- Auto-Speaker ---

    public void startSpeaker() {
        if (!isTestGame() || speakerRunning) return;
        captureGameReferences();
        speakerRunning = true;
        speakerStatus = "Laeuft";
        speakerFuture = executor.scheduleWithFixedDelay(this::speakerTick, 0, 2, TimeUnit.SECONDS);
        log.info("Auto-Speaker gestartet");
    }

    public void stopSpeaker() {
        if (speakerFuture != null) speakerFuture.cancel(false);
        speakerRunning = false;
        speakerStatus = "Gestoppt";
    }

    private void speakerTick() {
        try {
            if (!capturedDurchfuehrung.getList2ZumVorbereiten().isEmpty() && capturedDurchfuehrung.getReadyToVorbereiten()) {
                capturedDurchfuehrung.vorbereitet();
                speakerStatus = "Vorbereitet";
                return;
            }
            if (!capturedDurchfuehrung.getList3Vorbereitet().isEmpty() && capturedDurchfuehrung.getReadyToSpielen()) {
                capturedDurchfuehrung.spielen();
                speakerStatus = "Gestartet";
            }
        } catch (Exception e) {
            speakerStatus = "Fehler: " + e.getMessage();
            log.error("Auto-Speaker Fehler", e);
        }
    }

    // --- Auto-Schiri ---

    public void startSchiri() {
        if (!isTestGame() || schiriRunning) return;
        captureGameReferences();
        schiriRunning = true;
        schiriStatus = "Laeuft";
        schiriFuture = executor.scheduleWithFixedDelay(this::schiriTick, 1, 2, TimeUnit.SECONDS);
        log.info("Auto-Schiri gestartet");
    }

    public void stopSchiri() {
        if (schiriFuture != null) schiriFuture.cancel(false);
        schiriRunning = false;
        schiriStatus = "Gestoppt";
    }

    private void schiriTick() {
        try {
            List<Spiel> einzutragende = testService.findAllEinzutragende(capturedGameName);
            for (Spiel spiel : einzutragende) {
                testService.autoEintragen(spiel.getId());
                int toreA = spiel.getMannschaftA() != null ? spiel.getMannschaftA().getTeamNummer() % 10 : 0;
                int toreB = spiel.getMannschaftB() != null ? spiel.getMannschaftB().getTeamNummer() % 10 : 0;
                schiriStatus = "Eingetragen: " + spiel.getIdString() + " (" + toreA + ":" + toreB + ")";
            }
        } catch (Exception e) {
            schiriStatus = "Fehler: " + e.getMessage();
            log.error("Auto-Schiri Fehler", e);
        }
    }

    // --- Auto-Kontrolleur ---

    public void startKontrolleur() {
        if (!isTestGame() || kontrolleurRunning) return;
        captureGameReferences();
        kontrolleurRunning = true;
        kontrolleurStatus = "Laeuft";
        kontrolleurFuture = executor.scheduleWithFixedDelay(this::kontrolleurTick, 2, 2, TimeUnit.SECONDS);
        log.info("Auto-Kontrolleur gestartet");
    }

    public void stopKontrolleur() {
        if (kontrolleurFuture != null) kontrolleurFuture.cancel(false);
        kontrolleurRunning = false;
        kontrolleurStatus = "Gestoppt";
    }

    private void kontrolleurTick() {
        try {
            List<Spiel> zuBestaetigen = testService.findAllZuBestaetigen(capturedGameName);
            for (Spiel spiel : zuBestaetigen) {
                testService.autoBestaetigen(spiel.getId(), capturedResultate);
                kontrolleurStatus = "Bestaetigt: " + spiel.getIdString();
            }
        } catch (Exception e) {
            kontrolleurStatus = "Fehler: " + e.getMessage();
            log.error("Auto-Kontrolleur Fehler", e);
        }
    }

    // --- Alle starten/stoppen ---

    public void startAll() {
        startSpeaker();
        startSchiri();
        startKontrolleur();
    }

    public void stopAll() {
        stopSpeaker();
        stopSchiri();
        stopKontrolleur();
    }
}
