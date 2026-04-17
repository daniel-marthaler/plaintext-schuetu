package ch.plaintext.schuetu.service.mqtt;

import ch.plaintext.PlaintextSecurity;
import ch.plaintext.schuetu.entity.Spiel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Publishes key user actions as MQTT events.
 * All methods are fire-and-forget - errors are logged but never propagated.
 */
@Component
@Slf4j
public class MqttEventPublisher {

    @Autowired
    private MqttEventService mqttEventService;

    @Autowired
    private PlaintextSecurity plaintextSecurity;

    /**
     * Game selected by user.
     */
    public void gameSelected(String gameName) {
        Map<String, Object> data = baseData();
        data.put("gameName", gameName);
        mqttEventService.publishEvent("game/select", data);
    }

    /**
     * Score entered for a game (Spiel).
     */
    public void spielEingetragen(Spiel spiel) {
        Map<String, Object> data = baseData();
        data.put("spielId", spiel.getId());
        data.put("toreA", spiel.getToreA());
        data.put("toreB", spiel.getToreB());
        if (spiel.getMannschaftA() != null) {
            data.put("mannschaftA", spiel.getMannschaftA().getName());
            data.put("mannschaftB", spiel.getMannschaftB().getName());
        }
        mqttEventService.publishEvent("spiel/eintragen", data);
    }

    /**
     * Score corrected (Spielkorrektur saved).
     */
    public void spielKorrektur(Spiel spiel) {
        Map<String, Object> data = baseData();
        data.put("spielId", spiel.getId());
        data.put("toreA", spiel.getToreABestaetigt());
        data.put("toreB", spiel.getToreBBestaetigt());
        if (spiel.getMannschaftA() != null) {
            data.put("mannschaftA", spiel.getMannschaftA().getName());
            data.put("mannschaftB", spiel.getMannschaftB().getName());
        }
        mqttEventService.publishEvent("spiel/korrektur", data);
    }

    /**
     * Score confirmed/controlled (Kontrolle).
     */
    public void spielKontrolle(Spiel spiel) {
        Map<String, Object> data = baseData();
        data.put("spielId", spiel.getId());
        if (spiel.getMannschaftA() != null) {
            data.put("mannschaftA", spiel.getMannschaftA().getName());
            data.put("mannschaftB", spiel.getMannschaftB().getName());
        }
        mqttEventService.publishEvent("spiel/kontrolle", data);
    }

    /**
     * SpielZeile in Vorbereitung gesetzt.
     */
    public void spielVorbereiten(String gameName) {
        Map<String, Object> data = baseData();
        data.put("gameName", gameName);
        mqttEventService.publishEvent("spiel/vorbereiten", data);
    }

    /**
     * Spiel gestartet.
     */
    public void spielGestartet(Spiel spiel) {
        Map<String, Object> data = baseData();
        data.put("spielId", spiel.getId());
        if (spiel.getMannschaftA() != null) {
            data.put("mannschaftA", spiel.getMannschaftA().getName());
            data.put("mannschaftB", spiel.getMannschaftB().getName());
        }
        mqttEventService.publishEvent("spiel/start", data);
    }

    /**
     * Spiel beendet (fertig gespielt).
     */
    public void spielBeendet(Spiel spiel) {
        Map<String, Object> data = baseData();
        data.put("spielId", spiel.getId());
        if (spiel.getMannschaftA() != null) {
            data.put("mannschaftA", spiel.getMannschaftA().getName());
            data.put("mannschaftB", spiel.getMannschaftB().getName());
        }
        mqttEventService.publishEvent("spiel/beendet", data);
    }

    /**
     * Phase changed for a game.
     */
    public void phaseChanged(String gameName, String newPhase) {
        Map<String, Object> data = baseData();
        data.put("gameName", gameName);
        data.put("newPhase", newPhase);
        mqttEventService.publishEvent("game/phase", data);
    }

    private Map<String, Object> baseData() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("user", getCurrentUser());
        data.put("timestamp", Instant.now().toString());
        return data;
    }

    private String getCurrentUser() {
        try {
            return plaintextSecurity.getUser();
        } catch (Exception e) {
            return "unknown";
        }
    }
}
