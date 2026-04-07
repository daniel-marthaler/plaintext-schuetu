package ch.plaintext.schuetu.entity;

import ch.plaintext.framework.SuperModel;
import ch.plaintext.schuetu.model.CreationDateProvider;
import ch.plaintext.schuetu.model.enums.SpielTageszeit;
import ch.plaintext.schuetu.model.enums.SpielZeilenPhaseEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.persistence.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * SpielZeile Entity
 */
@Entity
@Data
@EqualsAndHashCode(callSuper = false)
public class SpielZeile extends SuperModel implements CreationDateProvider {

    private String guid = UUID.randomUUID().toString();

    private Date creationdate = new Date();

    private String game;

    private boolean pause = false;

    private boolean finale = false;

    private Date start = new Date();

    private boolean sonntag;

    private SpielTageszeit spieltageszeit;

    private SpielZeilenPhaseEnum phase = SpielZeilenPhaseEnum.A_ANSTEHEND;

    @Transient
    private boolean adisabled = false;

    @Transient
    private boolean bdisabled = false;

    @Transient
    private boolean cdisabled = false;

    @Transient
    private boolean ddisabled = false;

    @Transient
    private String konfliktText;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Spiel a;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Spiel b;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Spiel c;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Spiel d;

    private String gId = java.util.UUID.randomUUID().toString();

    public boolean checkEmty() {

        if (a != null) {
            return false;
        }
        if (b != null) {
            return false;
        }
        return c == null;

    }

    public Spiel getA() {
        if (a == null) {
            Spiel spiel = new Spiel();
            spiel.setPlatzhalter(true);
            return spiel;
        }
        return a;
    }

    public Spiel getB() {
        if (b == null) {
            Spiel spiel = new Spiel();
            spiel.setPlatzhalter(true);
            return spiel;
        }
        return b;
    }

    public Spiel getC() {
        if (c == null) {
            Spiel spiel = new Spiel();
            spiel.setPlatzhalter(true);
            return spiel;
        }
        return c;
    }

    public Spiel getD() {
        if (d == null) {
            Spiel spiel = new Spiel();
            spiel.setPlatzhalter(true);
            return spiel;
        }
        return d;
    }

    public boolean isKonflikt() {
        return !((this.konfliktText == null) || this.konfliktText.equals(""));
    }

    public void togglePause() {
        this.pause = !pause;
    }

    public List<Mannschaft> getAllMannschaften() {
        final List<Mannschaft> mannschaften = new ArrayList<>();

        if (this.a != null) {
            if (this.a.getMannschaftA() != null) {
                mannschaften.add(this.a.getMannschaftA());
            }
            if (this.a.getMannschaftB() != null) {
                mannschaften.add(this.a.getMannschaftB());
            }
        }
        if (this.b != null) {
            if (this.b.getMannschaftA() != null) {
                mannschaften.add(this.b.getMannschaftA());
            }
            if (this.b.getMannschaftB() != null) {
                mannschaften.add(this.b.getMannschaftB());
            }
        }
        if (this.c != null) {
            if (this.c.getMannschaftA() != null) {
                mannschaften.add(this.c.getMannschaftA());
            }
            if (this.c.getMannschaftB() != null) {
                mannschaften.add(this.c.getMannschaftB());
            }
        }
        if (this.d != null) {
            if (this.d.getMannschaftA() != null) {
                mannschaften.add(this.d.getMannschaftA());
            }
            if (this.d.getMannschaftB() != null) {
                mannschaften.add(this.d.getMannschaftB());
            }
        }
        return mannschaften;
    }

    public List<Spiel> getAllSpiele() {
        final List<Spiel> spiele = new ArrayList<>();
        if (this.a != null) {
            spiele.add(a);
        }
        if (this.b != null) {
            spiele.add(b);
        }
        if (this.c != null) {
            spiele.add(c);
        }
        return spiele;
    }

    public boolean isEmty() {
        return this.a == null && this.b == null && this.c == null;
    }

    public String getZeitAsString() {
        SimpleDateFormat form = new SimpleDateFormat("HH:mm");
        return form.format(this.start);
    }

    public String getTauschId() {
        if (this.isSonntag()) {
            return "so," + this.getZeitAsString() + ",";
        } else {
            return "sa," + this.getZeitAsString() + ",";
        }
    }

    public void setSpiel(Spiel spiel, String platz) {
        if (platz.equals("a")) {
            this.setA(spiel);
        }
        if (platz.equals("b")) {
            this.setB(spiel);
        }
        if (platz.equals("c")) {
            this.setC(spiel);
        }
        if (platz.equals("d")) {
            this.setD(spiel);
        }
    }

    public Spiel getSpiel(String platz) {
        if (platz.equals("a")) {
            return getA();
        }
        if (platz.equals("b")) {
            return getB();
        }
        if (platz.equals("c")) {
            return getC();
        }
        if (platz.equals("d")) {
            return getD();
        }
        return null;
    }

}
