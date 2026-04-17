package ch.plaintext.schuetu.service.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;

/**
 * MQTT Event Service - publishes tournament events to an MQTT broker.
 * Supports dynamic configuration from GameModel and retained messages with unique hash IDs.
 */
@Component
@Slf4j
public class MqttEventService {

    @Value("${plaintext.mqtt.enabled:false}")
    private boolean enabled;

    @Value("${plaintext.mqtt.broker-url:tcp://192.168.1.224:1883}")
    private String brokerUrl;

    @Value("${plaintext.mqtt.client-id:schuetu-prod}")
    private String clientId;

    @Value("${plaintext.mqtt.topic-prefix:schuetu/events}")
    private String topicPrefix;

    private MqttClient client;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Dynamische Konfiguration (kann zur Laufzeit ueberschrieben werden)
    private String dynamicBrokerUrl;
    private String dynamicTopicPrefix;
    private boolean dynamicEnabled = false;

    @PostConstruct
    public void init() {
        if (!enabled) {
            log.info("MQTT is disabled");
            return;
        }
        connect();
    }

    /**
     * Verbindung mit den Standard-Einstellungen aus application.yml herstellen.
     */
    public void connect() {
        connectTo(brokerUrl, clientId);
    }

    /**
     * Verbindung mit dynamischen Einstellungen herstellen (aus GameModel).
     */
    public void connectDynamic(String brokerUrlWithPort, String topic) {
        this.dynamicBrokerUrl = brokerUrlWithPort;
        this.dynamicTopicPrefix = topic;
        this.dynamicEnabled = true;
        connectTo(brokerUrlWithPort, clientId + "-" + UUID.randomUUID().toString().substring(0, 8));
    }

    private void connectTo(String url, String cid) {
        disconnect();
        try {
            client = new MqttClient(url, cid);
            MqttConnectionOptions options = new MqttConnectionOptions();
            options.setAutomaticReconnect(true);
            options.setCleanStart(true);
            options.setConnectionTimeout(5);
            client.connect(options);
            log.info("MQTT connected to {} as {}", url, cid);
        } catch (MqttException e) {
            log.warn("MQTT connection failed (broker not available): {}", e.getMessage());
            client = null;
        }
    }

    @PreDestroy
    public void disconnect() {
        if (client != null && client.isConnected()) {
            try {
                client.disconnect();
                client.close();
                log.info("MQTT disconnected");
            } catch (MqttException e) {
                log.warn("MQTT disconnect error: {}", e.getMessage());
            }
        }
        client = null;
    }

    /**
     * Publishes an event to the MQTT broker with retain=true and a unique hash.
     */
    public void publishEvent(String eventType, Map<String, Object> data) {
        if (!isEffectivelyEnabled()) {
            log.debug("MQTT not connected, skipping event: {}", eventType);
            return;
        }

        try {
            // Eindeutigen Hash generieren
            String hash = generateHash(eventType, data);
            data.put("hash", hash);

            String prefix = dynamicEnabled ? dynamicTopicPrefix : topicPrefix;
            String topic = prefix + "/" + eventType;
            String payload = objectMapper.writeValueAsString(data);
            MqttMessage message = new MqttMessage(payload.getBytes(StandardCharsets.UTF_8));
            message.setQos(1);
            message.setRetained(true);
            client.publish(topic, message);
            log.debug("MQTT published to {}: {}", topic, payload);
        } catch (Exception e) {
            log.warn("MQTT publish failed for event {}: {}", eventType, e.getMessage());
        }
    }

    /**
     * Erzeugt einen eindeutigen Hash fuer eine Nachricht.
     */
    private String generateHash(String eventType, Map<String, Object> data) {
        try {
            String content = eventType + ":" + System.nanoTime() + ":" + data.toString();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes).substring(0, 16);
        } catch (Exception e) {
            return UUID.randomUUID().toString().substring(0, 16);
        }
    }

    public boolean isConnected() {
        return client != null && client.isConnected();
    }

    private boolean isEffectivelyEnabled() {
        return (enabled || dynamicEnabled) && client != null && client.isConnected();
    }

    public MqttClient getClient() {
        return client;
    }

    public String getEffectiveTopicPrefix() {
        return dynamicEnabled ? dynamicTopicPrefix : topicPrefix;
    }
}
