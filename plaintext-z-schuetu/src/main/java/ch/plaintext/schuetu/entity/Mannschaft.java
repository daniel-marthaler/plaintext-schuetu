package ch.plaintext.schuetu.entity;

import ch.plaintext.framework.SuperModel;
import ch.plaintext.schuetu.model.CreationDateProvider;
import ch.plaintext.schuetu.model.enums.GeschlechtEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import jakarta.persistence.*;
import java.util.*;

/**
 * Mannschaft Entity
 */
@Entity
@Slf4j
@Data
@EqualsAndHashCode(callSuper = false, exclude = {"gruppeB", "gruppeA", "konflikt"})
public class Mannschaft extends SuperModel implements CreationDateProvider {

    @Transient
    private String doppelterBetreuer;

    private Date creationdate = new Date();

    private String game = "";

    private String nickname = "";

    private String farbe = "";

    private int teamNummer = 0;

    private int klasse = 0;

    private String gr;

    private GeschlechtEnum geschlecht = GeschlechtEnum.M;

    private int anzahlSpieler = 0;

    private String schulhaus = "";

    private Integer spielJahr = 2000;

    private String esr = "";

    private String klassenBezeichnung;

    private String captainName = "";
    private String captain2Name = "";
    private String captain2Vorname = "";

    private String captainStrasse = "";

    private String captainPLZOrt = "";

    private String captainTelefon = "";

    private String captainEmail = "";

    private String begleitpersonAnrede = "";

    private String begleitpersonName = "";
    private String begleitperson2Name = "";
    private String begleitperson2Vorname = "";

    private String begleitpersonStrasse = "";

    private String begleitpersonPLZOrt = "";

    private String begleitpersonTelefon = "";

    private String color = "Blau";

    private String begleitpersonEmail = "";

    private String spielWunschHint = "";

    @Column(columnDefinition = "text")
    private String notizen = "";

    @Transient
    private boolean konflikt = false;

    private Boolean disqualifiziert = false;

    @ManyToOne(fetch = FetchType.EAGER)
    private Gruppe gruppeA = null;

    @ManyToOne(fetch = FetchType.EAGER)
    private Gruppe gruppeB = null;

    public String getBegleitpersonVorname() {
        if (begleitpersonName == null || begleitpersonName.isEmpty() || !begleitpersonName.contains(" ")) {
            return begleitpersonName;
        }

        return StringUtils.split(begleitpersonName, " ")[0];
    }

    public String getBegleitpersonNameNach() {
        return begleitpersonName.replace(getBegleitpersonVorname(), "");
    }

    public String getBegleitpersonPLZ() {
        if (begleitpersonPLZOrt == null || begleitpersonPLZOrt.isEmpty() || !begleitpersonPLZOrt.contains(" ")) {
            return begleitpersonPLZOrt;
        }

        return StringUtils.split(begleitpersonPLZOrt, " ")[0];
    }

    public String getBegleitpersonOrt() {
        return begleitpersonPLZOrt.replace(getBegleitpersonPLZ(), "").trim();
    }

    public String getName() {

        String nu = "";
        if (this.teamNummer < 10) {
            nu = "0";
        }

        if (this.teamNummer == 0) {

            if (this.konflikt) {
                return "" + this.geschlecht + this.klasse + "XX@";
            }
            return "" + this.geschlecht + this.klasse + "XX";

        }
        String nick = "";
        if (this.nickname != null && !this.nickname.isEmpty()) {
            nick = " (" + this.nickname + ")";
        }
        if (this.konflikt) {
            return "" + this.geschlecht + this.klasse + nu + this.teamNummer + nick + "@";
        }
        return "" + this.geschlecht + this.klasse + nu + this.teamNummer + nick;
    }

    public String getNameNoNickname() {
        String name = getName().replace("(", "");
        name = name.replace(")", "");
        if (nickname != null) {
            name = name.replace(nickname, "");
        }
        name = name.trim();
        return name.toUpperCase();
    }

    public String getShortKatName() {
        return geschlecht.toString() + klasse;
    }

    public boolean isMemberofGroupA() {
        return this.gruppeA.getMannschaften().contains(this);
    }

    // berechnungen
    public int getSpieleAbgeschlossen() {
        int i = 0;
        final List<Spiel> list = this.getSpiele();
        for (final Spiel spiel : list) {
            final Integer sp = spiel.getPunkteVonMannschaft(this);
            if (sp > -1) {
                i++;
            }
        }
        return i;
    }

    public int getPunkteTotal() {
        int punkte = -1;
        final List<Spiel> list = this.getSpiele();
        for (final Spiel spiel : list) {
            final int sp = spiel.getPunkteVonMannschaft(this);
            if (sp > -1) {
                if (punkte == -1) {
                    punkte = sp;
                } else {
                    punkte = punkte + sp;
                }
            }
        }
        return punkte;
    }

    public int getGesammtzahlGruppenSpiele() {
        return this.getSpiele().size();
    }

    public int getGeschosseneTore() {
        int tore = 0;
        final List<Spiel> list = this.getSpiele();
        for (final Spiel spiel : list) {
            final int sp = spiel.getToreErziehlt(this);
            tore = tore + sp;
        }
        return tore;
    }

    public int getKassierteTore() {
        int tore = 0;
        final List<Spiel> list = this.getSpiele();
        for (final Spiel spiel : list) {
            final int sp = spiel.getToreKassiert(this);
            tore = tore + sp;
        }
        return tore;
    }

    public int getTorverhaeltnis() {
        return getGeschosseneTore() - getKassierteTore();
    }

    public List<Spiel> getSpiele() {
        final Set<Spiel> spieleVonGruppe = this.getGruppe().getKategorie().getSpiele();

        final List<Spiel> spieleResult = new ArrayList<>();
        for (final Spiel spiel : spieleVonGruppe) {
            if (spiel.getMannschaftA().getName().equals(this.getName()) || spiel.getMannschaftB().getName().equals(this.getName())) {
                spieleResult.add(spiel);
            }
        }
        return spieleResult;
    }

    public Kategorie getKategorie() {
        if (this.gruppeA != null) {
            return this.gruppeA.getKategorie();
        }
        return null;
    }

    // normale getter & setter

    @Deprecated
    public Gruppe getGruppeB() {
        return this.gruppeB;
    }

    @Deprecated
    public void setGruppeB(final Gruppe gruppeB) {
        this.gruppeB = gruppeB;
    }

    public Gruppe getGruppe() {
        return this.gruppeA;
    }

    public void setGruppe(final Gruppe gruppe) {
        this.gruppeA = gruppe;
    }

    @Deprecated
    public String getIdString() {
        log.info("FAKE !!! MUSS WEG ");
        return UUID.randomUUID().toString();
    }

    public String toString2() {
        return "Mannschaft{" + "anzahlSpieler=" + this.anzahlSpieler + ", teamNummer=" + this.teamNummer + ", klasse=" + this.klasse + ", geschlecht=" + this.geschlecht + ", spielJahr="
                + ", schulhaus='" + this.schulhaus + '\'' + ", klassenBezeichnung='" + this.klassenBezeichnung + '\'' + ", captainName='" + this.captainName + '\''
                + ", captainStrasse='" + this.captainStrasse + '\'' + ", captainPLZOrt='" + this.captainPLZOrt + '\'' + ", captainTelefon='" + this.captainTelefon + '\'' + ", captainEmail='"
                + this.captainEmail + '\'' + ", begleitpersonName='" + this.begleitpersonName + '\'' + ", begleitpersonStrasse='" + this.begleitpersonStrasse + '\'' + ", begleitpersonPLZOrt='"
                + this.begleitpersonPLZOrt + '\'' + ", begleitpersonTelefon='" + this.begleitpersonTelefon + '\'' + ", begleitpersonEmail='" + this.begleitpersonEmail + '\'' + ", notizen='"
                + this.notizen + '\'' + ", konflikt=" + this.konflikt +
                ", creationDate=" +
                '}';
    }

    @Override
    public String toString() {
        return "Mannschaft [" + getName() + "]";
    }

    public String getSpielWunschHint() {
        return spielWunschHint;
    }

    public void setSpielWunschHint(String spielWunschHint) {
        this.spielWunschHint = spielWunschHint;
    }

    public int getTeamNummer() {
        return this.teamNummer;
    }

    public String getGeschlechtString() {
        if (this.geschlecht == GeschlechtEnum.M) {
            return "m";
        }
        if (this.geschlecht == GeschlechtEnum.K) {
            return "k";
        }
        return "";
    }

    public void setGeschlechtString(String geschlechtIn) {
        String geschlechtS = geschlechtIn.toLowerCase();
        if (geschlechtS.equals("k")) {
            this.geschlecht = GeschlechtEnum.K;
        }
        if (geschlechtS.equals("m")) {
            this.geschlecht = GeschlechtEnum.M;
        }
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getAnrede() {
        return this.begleitpersonAnrede;
    }

    public String getNameVorname() {
        return this.begleitpersonName;
    }

    public String getVorname() {
        if (this.begleitpersonName != null && this.begleitpersonName.contains(" ")) {
            return this.begleitpersonName.split(" ")[0];
        }
        return this.begleitpersonName;
    }

    public String getStrasse() {
        return this.begleitpersonStrasse;
    }

    public String getPLZOrt() {
        return this.begleitpersonName;
    }

}
