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
import java.util.Map;

/**
 * MQTT Event Service - publishes tournament events to an MQTT broker.
 * Fire-and-forget with graceful error handling when broker is unavailable.
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

    @PostConstruct
    public void init() {
        if (!enabled) {
            log.info("MQTT is disabled");
            return;
        }
        connect();
    }

    public void connect() {
        try {
            client = new MqttClient(brokerUrl, clientId);
            MqttConnectionOptions options = new MqttConnectionOptions();
            options.setAutomaticReconnect(true);
            options.setCleanStart(true);
            options.setConnectionTimeout(5);
            client.connect(options);
            log.info("MQTT connected to {} as {}", brokerUrl, clientId);
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
    }

    /**
     * Publishes an event to the MQTT broker. Fire-and-forget.
     *
     * @param eventType the event type, used as sub-topic (e.g. "game/select")
     * @param data      the event payload as key-value map
     */
    public void publishEvent(String eventType, Map<String, Object> data) {
        if (!enabled || client == null || !client.isConnected()) {
            log.debug("MQTT not connected, skipping event: {}", eventType);
            return;
        }

        try {
            String topic = topicPrefix + "/" + eventType;
            String payload = objectMapper.writeValueAsString(data);
            MqttMessage message = new MqttMessage(payload.getBytes(StandardCharsets.UTF_8));
            message.setQos(1);
            message.setRetained(false);
            client.publish(topic, message);
            log.debug("MQTT published to {}: {}", topic, payload);
        } catch (Exception e) {
            log.warn("MQTT publish failed for event {}: {}", eventType, e.getMessage());
        }
    }

    public boolean isConnected() {
        return enabled && client != null && client.isConnected();
    }
}
