package ch.plaintext.schuetu.entity;

import ch.plaintext.framework.SuperModel;
import ch.plaintext.schuetu.model.CreationDateProvider;
import ch.plaintext.schuetu.model.enums.PlatzEnum;
import ch.plaintext.schuetu.model.enums.SpielEnum;
import ch.plaintext.schuetu.model.enums.SpielZeilenPhaseEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import java.util.Locale;
import jakarta.persistence.Transient;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Ein Spiel, es wird mittels Enum unterschieden ob es sich um ein Gruppen- oder
 * Finalspiel handelt
 */
@Entity
@Data
@Slf4j
@EqualsAndHashCode(callSuper = false)
public class Spiel extends SuperModel implements CreationDateProvider {

    private static final int NOT_INIT_FLAG = -1;
    private static final int PUNKTE_UNENTSCHIEDEN = 1;
    private static final int PUNKTE_NIEDERLAGE = 0;
    private static final int PUNKTE_SIEG = 3;
    private static final long serialVersionUID = 1L;
    private Date creationdate = new Date();
    private String game;
    private SpielEnum typ = SpielEnum.GRUPPE;
    // fuer den Fall, dass in einer Gruppe 2 Grosse Finale vorhanden sind
    private Boolean changedGrossToKlein = false;

    private PlatzEnum platz = null;
    private Date start = null;

    private int toreA = NOT_INIT_FLAG;
    private int toreB = NOT_INIT_FLAG;

    private int toreABestaetigt = NOT_INIT_FLAG;
    private int toreBBestaetigt = NOT_INIT_FLAG;

    private String idString = null;

    private boolean fertigGespielt = false;

    private boolean amSpielen = false;
    private boolean fertigEingetragen = false;
    private boolean fertigBestaetigt = false;
    private boolean zurueckgewiesen = false;

    private String realName = "";

    @Transient
    private boolean hintauschOk = true;

    @Transient
    private boolean hertauschOk = true;

    @Transient
    private boolean platzhalter = false;

    @Transient
    private String finalspieleBekanntAm = "";

    // hilfsfeld zum ausgeben der finale, falls noch keine mannschaft bestimmt
    // wurde
    private String kategorieName;

    private String klasse = "";

    private String eintrager;

    @Column(columnDefinition = "text")
    private String notizen = "";

    // dient dazu den spiel zeilen zustand im xls abzulegen
    private SpielZeilenPhaseEnum spielZeilenPhase = SpielZeilenPhaseEnum.A_ANSTEHEND;

    @Transient
    private int mannschaftAId;

    @Transient
    private int mannschaftBId;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    private Mannschaft mannschaftA;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    private Mannschaft mannschaftB;

    @ManyToOne(fetch = FetchType.LAZY)
    private Schiri schiri;

    private String schiriName;

    private String kontrolle;

    public Spiel() {

    }

    public String getFarbe() {

        String farbe = "";

        if (mannschaftA != null && mannschaftB != null) {

            if (mannschaftA.getFarbe().equals(mannschaftB.getFarbe())) {
                farbe = mannschaftA.getFarbe();
            }

        }

        if (farbe != null && farbe.isEmpty()) {
            return "white";
        }

        farbe = farbe.toLowerCase();
        farbe = farbe.trim().replace("/", "");
        return "red";

    }

    public void resetTausch() {
        this.hintauschOk = true;
        this.hertauschOk = true;
    }

    public String getMannschaftAName() {

        if (mannschaftA != null) {
            return mannschaftA.getName();
        } else if (typ == SpielEnum.GFINAL) {
            return "A, GF";
        } else {
            return "A, KF ";
        }

    }

    // websitetemaplat.vm & info.xhtml
    public String evaluateToreABestateigtString() {
        if (this.toreABestaetigt == NOT_INIT_FLAG) {
            return "--";
        }
        return String.format("%02d", this.toreABestaetigt);
    }

    // websitetemaplat.vm & info.xhtml
    public String evaluateToreBBestateigtString() {
        if (this.toreBBestaetigt == NOT_INIT_FLAG) {
            return "--";
        }
        return String.format("%02d", this.toreBBestaetigt);
    }

    public String getMannschaftBName() {

        if (mannschaftB != null) {
            return mannschaftB.getName();
        } else if (typ == SpielEnum.GFINAL) {
            return "B, GF";
        } else {
            return "B, KF ";
        }

    }

    public boolean getFertiggespielt() {
        return !((this.toreABestaetigt == NOT_INIT_FLAG) || (this.toreBBestaetigt == NOT_INIT_FLAG));
    }

    public int getToreKassiert(final Mannschaft m) {
        int ret = 0;
        if (m.equals(this.getMannschaftA())) {
            if (this.toreBBestaetigt > NOT_INIT_FLAG) {
                ret = this.toreBBestaetigt;
            }
        }

        if (m.equals(this.getMannschaftB())) {
            if (this.toreABestaetigt > NOT_INIT_FLAG) {
                ret = this.toreABestaetigt;
            }
        }

        return ret;
    }

    public int getToreErziehlt(final Mannschaft m) {
        int ret = 0;
        if (m.equals(this.getMannschaftA())) {
            if (this.toreABestaetigt > NOT_INIT_FLAG) {
                ret = this.toreABestaetigt;
            }
        }

        if (m.equals(this.getMannschaftB())) {
            if (this.toreBBestaetigt > NOT_INIT_FLAG) {
                ret = this.toreBBestaetigt;
            }

        }

        return ret;
    }

    public int getPunkteA() {

        if ((this.getToreABestaetigt() < 0) || (this.getToreABestaetigt() < 0)) {
            return NOT_INIT_FLAG;
        }

        // punkte
        if (this.getToreABestaetigt() == this.getToreBBestaetigt()) {
            return PUNKTE_UNENTSCHIEDEN;
        } else if (this.getToreABestaetigt() < this.getToreBBestaetigt()) {
            return PUNKTE_NIEDERLAGE;
        } else {
            return PUNKTE_SIEG;
        }
    }

    public int getPunkteB() {

        if ((this.getToreABestaetigt() < 0) || (this.getToreABestaetigt() < 0)) {
            return NOT_INIT_FLAG;
        }

        // punkte
        if (this.getToreABestaetigt() == this.getToreBBestaetigt()) {
            return 1;
        } else if (this.getToreABestaetigt() > this.getToreBBestaetigt()) {
            return PUNKTE_NIEDERLAGE;
        } else {
            return PUNKTE_SIEG;
        }
    }

    public int getPunkteVonMannschaft(final Mannschaft m) {

        if ((this.getPunkteA() < 0) || (this.getPunkteB() < 0)) {
            return NOT_INIT_FLAG;
        }

        if (m.equals(this.getMannschaftA())) {
            return this.getPunkteA();
        }

        if (m.equals(this.getMannschaftB())) {
            return this.getPunkteB();
        }
        return NOT_INIT_FLAG;

    }

    public Gruppe getGruppe() {
        if (this.mannschaftA != null) {
            return this.mannschaftA.getGruppe();
        }
        return null;
    }

    public String getWebsiteName() {
        if (!this.realName.isEmpty()) {
            return this.realName;
        }
        String ret = "";

        if (this.typ == SpielEnum.GFINAL) {
            ret = ret + "GrFin-" + this.getKategorieName();
        } else if (this.typ == SpielEnum.KFINAL) {
            ret = ret + "KlFin-" + this.getKategorieName();
        } else {
            ret = ret + this.getTyp();
        }
        return ret;
    }

    public String toString2() {
        return "Spiel{" +
                "id=" + getId() +
                ", platz=" + platz +
                ", toreA=" + toreA +
                ", toreB=" + toreB +
                ", toreABestaetigt=" + toreABestaetigt +
                ", toreBBestaetigt=" + toreBBestaetigt +
                ", spielZeilenPhase=" + spielZeilenPhase +
                ", mannschaftAId=" + mannschaftAId +
                ", mannschaftBId=" + mannschaftBId +
                ", schiri=" + schiri +
                '}';
    }

    @Override
    public String toString() {

        if (platzhalter) {
            return "-";
        }

        String ret = "";

        if (this.mannschaftA != null) {
            ret = this.getMannschaftAName() + ":" + this.getMannschaftBName();
        } else if (this.typ == SpielEnum.GFINAL) {
            if (this.realName != null && !this.realName.isEmpty()) {
                return this.realName;
            }

            ret = ret + "GrFin-" + this.getKategorieName();
        } else if (this.typ == SpielEnum.KFINAL) {
            ret = ret + "KlFin-" + this.getKategorieName();
        } else {
            ret = "_";
        }

        if (ret.isEmpty()) {
            return " ";
        }

        if (getKlasse() != null) {
            return ret + getKlasse();
        }

        return ret;
    }

    public String toStringSpieltage() {
        if (toString().contains(":")) {
            return toString() + " @";
        }
        if (toString().contains("Fin")) {
            return toString() + " *";
        }
        return toString();
    }

    public boolean isFinaleBekannt() {
        return this.mannschaftA != null;
    }

    public String getDateFormatted() {
        SimpleDateFormat fmt = new SimpleDateFormat("EEE HH:mm", Locale.GERMAN);
        return fmt.format(start);
    }

}
