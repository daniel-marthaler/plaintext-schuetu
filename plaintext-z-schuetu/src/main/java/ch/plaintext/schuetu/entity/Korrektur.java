package ch.plaintext.schuetu.entity;

import ch.plaintext.framework.SuperModel;
import ch.plaintext.schuetu.model.CreationDateProvider;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.persistence.Entity;
import jakarta.persistence.OrderColumn;
import java.util.Date;

/**
 * Dient dazu, Korrekturen bei der Kategoriezuordnung und bei der Spielzuordnung
 * aufzunehmen und zu speichern
 */
@Entity
@Data
@EqualsAndHashCode(callSuper = false)
public class Korrektur extends SuperModel implements CreationDateProvider {

    private Date creationdate = new Date();

    private String game;

    private String typ = null;

    private String wert = null;

    @OrderColumn
    private long reihenfolge;

}
