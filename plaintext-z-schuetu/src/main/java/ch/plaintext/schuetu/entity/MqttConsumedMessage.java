package ch.plaintext.schuetu.entity;

import ch.plaintext.framework.SuperModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * Speichert konsumierte MQTT-Nachrichten zur Deduplizierung.
 * Beim Neustart kann anhand der gespeicherten Hashes ermittelt werden,
 * welche Nachrichten bereits verarbeitet wurden.
 */
@Entity
@Table(name = "mqtt_consumed_message")
@Data
@EqualsAndHashCode(callSuper = false)
public class MqttConsumedMessage extends SuperModel {

    @Column(nullable = false, unique = true)
    private String hash;

    @Column(nullable = false)
    private String eventType;

    @Column(columnDefinition = "TEXT")
    private String payload;

    @Column(nullable = false)
    private Date receivedAt = new Date();

    private boolean processed = false;

    private String game;
}
