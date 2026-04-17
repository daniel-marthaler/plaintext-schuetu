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
import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.annotation.PreDestroy;

/**
 * Backing Bean fuer den Integrationstest-Tab.
 * Ermoeglicht automatisches Durchspielen eines kompletten Turniers.
 * Nur aktiv wenn der Spielname mit "Test" beginnt.
 * Die Threads laufen unabhaengig von der HTTP-Session weiter,
 * werden aber bei Session-Destroy sauber gestoppt.
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

    // Thread-safe Flags fuer sofortigen Stop
    private final AtomicBoolean speakerActive = new AtomicBoolean(false);
    private final AtomicBoolean schiriActive = new AtomicBoolean(false);
    private final AtomicBoolean kontrolleurActive = new AtomicBoolean(false);

    // Erfasste Referenzen fuer Background-Threads
    private SpielDurchfuehrung capturedDurchfuehrung;
    private ResultateVerarbeiter capturedResultate;
    private String capturedGameName;

    @Getter
    private String speakerStatus = "Gestoppt";
    @Getter
    private String schiriStatus = "Gestoppt";
    @Getter
    private String kontrolleurStatus = "Gestoppt";

    public boolean isSpeakerRunning() { return speakerActive.get(); }
    public boolean isSchiriRunning() { return schiriActive.get(); }
    public boolean isKontrolleurRunning() { return kontrolleurActive.get(); }

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
        if (!isTestGame() || speakerActive.get()) return;
        captureGameReferences();
        speakerActive.set(true);
        speakerStatus = "Laeuft";
        speakerFuture = executor.scheduleWithFixedDelay(this::speakerTick, 0, 2, TimeUnit.SECONDS);
        log.info("Auto-Speaker gestartet");
    }

    public void stopSpeaker() {
        speakerActive.set(false);
        if (speakerFuture != null) speakerFuture.cancel(true);
        speakerStatus = "Gestoppt";
    }

    private void speakerTick() {
        if (!speakerActive.get()) return;
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
        if (!isTestGame() || schiriActive.get()) return;
        captureGameReferences();
        schiriActive.set(true);
        schiriStatus = "Laeuft";
        schiriFuture = executor.scheduleWithFixedDelay(this::schiriTick, 1, 2, TimeUnit.SECONDS);
        log.info("Auto-Schiri gestartet");
    }

    public void stopSchiri() {
        schiriActive.set(false);
        if (schiriFuture != null) schiriFuture.cancel(true);
        schiriStatus = "Gestoppt";
    }

    private void schiriTick() {
        if (!schiriActive.get()) return;
        try {
            List<Spiel> einzutragende = testService.findAllEinzutragende(capturedGameName);
            for (Spiel spiel : einzutragende) {
                if (!schiriActive.get()) return;
                String result = testService.autoEintragen(spiel.getId());
                if (result != null) {
                    schiriStatus = "Eingetragen: " + result;
                }
            }
        } catch (Exception e) {
            schiriStatus = "Fehler: " + e.getMessage();
            log.error("Auto-Schiri Fehler", e);
        }
    }

    // --- Auto-Kontrolleur ---

    public void startKontrolleur() {
        if (!isTestGame() || kontrolleurActive.get()) return;
        captureGameReferences();
        kontrolleurActive.set(true);
        kontrolleurStatus = "Laeuft";
        kontrolleurFuture = executor.scheduleWithFixedDelay(this::kontrolleurTick, 2, 2, TimeUnit.SECONDS);
        log.info("Auto-Kontrolleur gestartet");
    }

    public void stopKontrolleur() {
        kontrolleurActive.set(false);
        if (kontrolleurFuture != null) kontrolleurFuture.cancel(true);
        kontrolleurStatus = "Gestoppt";
    }

    private void kontrolleurTick() {
        if (!kontrolleurActive.get()) return;
        try {
            List<Spiel> zuBestaetigen = testService.findAllZuBestaetigen(capturedGameName);
            for (Spiel spiel : zuBestaetigen) {
                if (!kontrolleurActive.get()) return;
                String result = testService.autoBestaetigen(spiel.getId(), capturedResultate);
                if (result != null) {
                    kontrolleurStatus = "Bestaetigt: " + result;
                }
            }
        } catch (Exception e) {
            kontrolleurStatus = "Fehler: " + e.getMessage();
            log.error("Auto-Kontrolleur Fehler", e);
        }
    }

    @PreDestroy
    public void destroy() {
        log.info("IntegrationTestBackingBean wird zerstoert - stoppe alle Threads");
        stopAll();
        executor.shutdownNow();
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
