package ch.plaintext.schuetu.service.mqtt;

import ch.plaintext.schuetu.entity.MqttConsumedMessage;
import ch.plaintext.schuetu.entity.Spiel;
import ch.plaintext.schuetu.repository.MqttConsumedMessageRepository;
import ch.plaintext.schuetu.repository.SpielRepository;
import ch.plaintext.schuetu.service.GameSelectionHolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.mqttv5.client.IMqttMessageListener;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Konsumiert MQTT Events im IN-Modus und fuehrt die entsprechenden Aktionen aus.
 * Deduplizierung ueber Hash in der Datenbank.
 */
@Component
@Scope("prototype")
@Slf4j
public class MqttEventConsumer implements IMqttMessageListener {

    @Autowired
    private MqttConsumedMessageRepository messageRepo;

    @Autowired
    private SpielRepository spielRepo;

    @Autowired
    private GameSelectionHolder gameSelectionHolder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public void messageArrived(String topic, MqttMessage mqttMessage) {
        try {
            String payload = new String(mqttMessage.getPayload(), StandardCharsets.UTF_8);
            @SuppressWarnings("unchecked")
            Map<String, Object> data = objectMapper.readValue(payload, Map.class);

            String hash = (String) data.get("hash");
            if (hash == null || hash.isBlank()) {
                log.warn("MQTT Nachricht ohne Hash empfangen auf {}", topic);
                return;
            }

            // Deduplizierung
            if (messageRepo.existsByHash(hash)) {
                log.debug("MQTT Nachricht bereits verarbeitet: {}", hash);
                return;
            }

            // Event-Typ aus Topic extrahieren
            String eventType = extractEventType(topic);

            // Nachricht in DB speichern
            MqttConsumedMessage msg = new MqttConsumedMessage();
            msg.setHash(hash);
            msg.setEventType(eventType);
            msg.setPayload(payload);
            msg.setReceivedAt(new Date());
            msg.setGame(gameSelectionHolder.hasGame() ? gameSelectionHolder.getGameName() : "unknown");

            // Event verarbeiten
            processEvent(eventType, data);

            msg.setProcessed(true);
            messageRepo.save(msg);

            log.info("MQTT Event verarbeitet: {} (hash: {})", eventType, hash);
        } catch (Exception e) {
            log.error("Fehler beim Verarbeiten der MQTT Nachricht auf {}: {}", topic, e.getMessage(), e);
        }
    }

    private String extractEventType(String topic) {
        // Topic format: prefix/eventType (z.B. schuetu/events/spiel/eintragen)
        int lastSlash = topic.lastIndexOf('/');
        int secondLastSlash = topic.lastIndexOf('/', lastSlash - 1);
        if (secondLastSlash >= 0) {
            return topic.substring(secondLastSlash + 1);
        }
        return topic.substring(lastSlash + 1);
    }

    private void processEvent(String eventType, Map<String, Object> data) {
        if (!gameSelectionHolder.hasGame()) {
            log.warn("Kein Spiel ausgewaehlt, Event {} wird ignoriert", eventType);
            return;
        }

        switch (eventType) {
            case "spiel/vorbereiten" -> {
                var durchfuehrung = gameSelectionHolder.getGame().getDurchfuehrung();
                if (!durchfuehrung.getList2ZumVorbereiten().isEmpty()) {
                    durchfuehrung.vorbereitet();
                    log.info("MQTT IN: vorbereitet()");
                }
            }
            case "spiel/start" -> {
                var durchfuehrung = gameSelectionHolder.getGame().getDurchfuehrung();
                if (!durchfuehrung.getList3Vorbereitet().isEmpty()) {
                    durchfuehrung.spielen();
                    log.info("MQTT IN: spielen()");
                }
            }
            case "spiel/eintragen" -> {
                processEintragen(data);
            }
            case "spiel/kontrolle" -> {
                processKontrolle(data);
            }
            case "spiel/beendet" -> {
                log.info("MQTT IN: Spiel beendet Event empfangen");
            }
            case "game/phase" -> {
                log.info("MQTT IN: Phase Change Event empfangen: {}", data.get("newPhase"));
            }
            default -> log.debug("MQTT IN: Unbekannter Event-Typ: {}", eventType);
        }
    }

    private void processEintragen(Map<String, Object> data) {
        try {
            Long spielId = Long.valueOf(String.valueOf(data.get("spielId")));
            int toreA = (Integer) data.get("toreA");
            int toreB = (Integer) data.get("toreB");
            String eintrager = (String) data.getOrDefault("eintrager", "MQTT-Sync");

            Spiel spiel = spielRepo.findById(spielId).orElse(null);
            if (spiel != null && !spiel.isFertigEingetragen()) {
                spiel.setToreA(toreA);
                spiel.setToreB(toreB);
                spiel.setFertigEingetragen(true);
                spiel.setEintrager(eintrager);
                spielRepo.save(spiel);
                log.info("MQTT IN: Resultate eingetragen fuer Spiel {}: {}:{}", spielId, toreA, toreB);
            }
        } catch (Exception e) {
            log.error("MQTT IN: Fehler beim Eintragen: {}", e.getMessage());
        }
    }

    private void processKontrolle(Map<String, Object> data) {
        try {
            Long spielId = Long.valueOf(String.valueOf(data.get("spielId")));

            Spiel spiel = spielRepo.findById(spielId).orElse(null);
            if (spiel != null && spiel.isFertigEingetragen() && !spiel.isFertigBestaetigt()) {
                spiel.setFertigBestaetigt(true);
                spiel.setToreABestaetigt(spiel.getToreA());
                spiel.setToreBBestaetigt(spiel.getToreB());
                spielRepo.save(spiel);
                gameSelectionHolder.getGame().getResultate().signalFertigesSpiel(spielId);
                log.info("MQTT IN: Kontrolle durchgefuehrt fuer Spiel {}", spielId);
            }
        } catch (Exception e) {
            log.error("MQTT IN: Fehler bei Kontrolle: {}", e.getMessage());
        }
    }
}
