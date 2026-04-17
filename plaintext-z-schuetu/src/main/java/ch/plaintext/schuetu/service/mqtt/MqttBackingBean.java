package ch.plaintext.schuetu.service.mqtt;

import ch.plaintext.schuetu.entity.GameModel;
import ch.plaintext.schuetu.service.GameSelectionHolder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Backing Bean fuer den MQTT-Tab in den Einstellungen.
 * Verwaltet die Verbindung und den Modus (IN/OUT).
 */
@Component
@Scope("session")
@Slf4j
public class MqttBackingBean {

    @Autowired
    private MqttEventService mqttEventService;

    @Autowired
    private GameSelectionHolder gameSelectionHolder;

    @Autowired
    private ApplicationContext ctx;

    @Getter
    private String connectionStatus = "Nicht verbunden";

    private MqttEventConsumer activeConsumer;

    public boolean isConnected() {
        return mqttEventService.isConnected();
    }

    public void connect() {
        if (!gameSelectionHolder.hasGame()) {
            connectionStatus = "Kein Spiel ausgewaehlt";
            return;
        }

        GameModel model = gameSelectionHolder.getGame().getModel();
        if (!model.isMqttEnabled()) {
            connectionStatus = "MQTT ist deaktiviert";
            return;
        }

        String brokerUrl = model.getMqttBrokerUrl() + ":" + model.getMqttPort();
        String topic = model.getMqttTopic();

        try {
            mqttEventService.connectDynamic(brokerUrl, topic);

            if ("IN".equals(model.getMqttMode())) {
                subscribeForIncoming(topic);
            }

            connectionStatus = "Verbunden mit " + brokerUrl + " (" + model.getMqttMode() + ")";
            log.info("MQTT verbunden: {} mode={}", brokerUrl, model.getMqttMode());
        } catch (Exception e) {
            connectionStatus = "Fehler: " + e.getMessage();
            log.error("MQTT Verbindungsfehler", e);
        }
    }

    private void subscribeForIncoming(String topic) {
        try {
            activeConsumer = ctx.getBean(MqttEventConsumer.class);
            mqttEventService.getClient().subscribe(topic + "/#", 1, activeConsumer);
            log.info("MQTT subscribed to {}/#", topic);
        } catch (MqttException e) {
            log.error("MQTT Subscribe Fehler: {}", e.getMessage());
            connectionStatus = "Verbunden, aber Subscribe fehlgeschlagen: " + e.getMessage();
        }
    }

    public void disconnect() {
        mqttEventService.disconnect();
        activeConsumer = null;
        connectionStatus = "Nicht verbunden";
        log.info("MQTT getrennt");
    }
}
