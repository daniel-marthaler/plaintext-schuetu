package ch.plaintext.schuetu.entity;

import ch.plaintext.framework.SuperModel;
import ch.plaintext.schuetu.model.CreationDateProvider;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.persistence.Entity;
import java.util.Date;

/**
 * Schiri User Entity
 */
@Entity
@Data
@EqualsAndHashCode(callSuper = false)
public class Schiri extends SuperModel implements CreationDateProvider {

    private Date creationdate = new Date();

    private String game;

    private boolean aktiviert = false;

    private int matchcount = 0;

    private String spielIDs = "";

    private String name;

    private String vorname;

    private String nachname;

    private String einteilung;

    private String telefon;

    private String passwordHash;

    private String loginName;

    public String getShName() {
        return vorname + " " + nachname;
    }

}
