package ch.plaintext.schuetu.service.einstellungen;

import ch.plaintext.schuetu.entity.Spiel;
import ch.plaintext.schuetu.service.Game;
import ch.plaintext.schuetu.service.GameSelectionHolder;
import ch.plaintext.schuetu.service.spieldurchfuehrung.EintragerService;
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
 *
 * Die Game-Referenz wird beim Start erfasst, damit die Background-Threads
 * nicht auf den Session-Scope zugreifen muessen.
 */
@Component
@Scope("session")
@Slf4j
public class IntegrationTestBackingBean {

    @Autowired
    private GameSelectionHolder gameSelectionHolder;

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(3);

    private ScheduledFuture<?> speakerFuture;
    private ScheduledFuture<?> schiriFuture;
    private ScheduledFuture<?> kontrolleurFuture;

    // Erfasste Referenzen fuer Background-Threads (kein Session-Scope noetig)
    private SpielDurchfuehrung capturedDurchfuehrung;
    private EintragerService capturedEintragen;

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

    /**
     * Erfasst die Game-Referenzen aus dem Session-Scope (wird im HTTP-Thread aufgerufen).
     */
    private void captureGameReferences() {
        Game game = gameSelectionHolder.getGame();
        capturedDurchfuehrung = game.getDurchfuehrung();
        capturedEintragen = game.getEintragen();
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
        if (speakerFuture != null) {
            speakerFuture.cancel(false);
        }
        speakerRunning = false;
        speakerStatus = "Gestoppt";
        log.info("Auto-Speaker gestoppt");
    }

    private void speakerTick() {
        try {
            if (!capturedDurchfuehrung.getList2ZumVorbereiten().isEmpty() && capturedDurchfuehrung.getReadyToVorbereiten()) {
                capturedDurchfuehrung.vorbereitet();
                speakerStatus = "Vorbereitet";
                log.debug("Auto-Speaker: vorbereitet()");
                return;
            }

            if (!capturedDurchfuehrung.getList3Vorbereitet().isEmpty() && capturedDurchfuehrung.getReadyToSpielen()) {
                capturedDurchfuehrung.spielen();
                speakerStatus = "Gestartet";
                log.debug("Auto-Speaker: spielen()");
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
        if (schiriFuture != null) {
            schiriFuture.cancel(false);
        }
        schiriRunning = false;
        schiriStatus = "Gestoppt";
        log.info("Auto-Schiri gestoppt");
    }

    private void schiriTick() {
        try {
            List<Spiel> einzutragende = capturedEintragen.findAllEinzutragende();

            for (Spiel spiel : einzutragende) {
                if (spiel.getMannschaftA() != null && spiel.getMannschaftB() != null) {
                    int toreA = spiel.getMannschaftA().getTeamNummer() % 10;
                    int toreB = spiel.getMannschaftB().getTeamNummer() % 10;
                    spiel.setToreA(toreA);
                    spiel.setToreB(toreB);
                    capturedEintragen.eintragen(List.of(spiel), String.valueOf(spiel.getId()), "Auto-Schiri");
                    schiriStatus = "Eingetragen: " + spiel.getIdString() + " (" + toreA + ":" + toreB + ")";
                    log.debug("Auto-Schiri: {} = {}:{}", spiel.getIdString(), toreA, toreB);
                }
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
        if (kontrolleurFuture != null) {
            kontrolleurFuture.cancel(false);
        }
        kontrolleurRunning = false;
        kontrolleurStatus = "Gestoppt";
        log.info("Auto-Kontrolleur gestoppt");
    }

    private void kontrolleurTick() {
        try {
            List<Spiel> zuBestaetigen = capturedEintragen.findAllZuBestaetigen();

            for (Spiel spiel : zuBestaetigen) {
                capturedEintragen.bestaetigen(List.of(spiel), String.valueOf(spiel.getId()), "ok");
                kontrolleurStatus = "Bestaetigt: " + spiel.getIdString();
                log.debug("Auto-Kontrolleur: bestaetigt {}", spiel.getIdString());
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
