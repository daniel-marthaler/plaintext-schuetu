package ch.plaintext.schuetu.entity;

import ch.plaintext.framework.SuperModel;
import ch.plaintext.schuetu.model.CreationDateProvider;
import ch.plaintext.schuetu.model.TurnierException;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Penalty Entity
 */
@SuppressWarnings("StringConcatenationInLoop")
@Entity
@Data
@Slf4j
@EqualsAndHashCode(callSuper = false)
public class Penalty extends SuperModel implements CreationDateProvider {

    private Date creationdate = new Date();

    private String game;

    public static final String LEER = "bitte_reihenfolge_angeben";

    private static final long serialVersionUID = 1L;

    private String reihenfolgeOrig = LEER;

    private String reihenfolge = LEER;

    private String idString;

    private boolean gespielt = false;
    private boolean bestaetigt = false;

    @ManyToOne
    private Gruppe gruppe = null;

    @OneToMany(fetch = FetchType.EAGER)
    private List<Mannschaft> finalList = new ArrayList<>();

    public void addMannschaftInitial(final Mannschaft m) {
        this.finalList.add(m);
        reihenfolgeOrig = "";
        for (Mannschaft m2 : finalList) {
            reihenfolgeOrig = reihenfolgeOrig + "," + m2.getNameNoNickname();
        }
        reihenfolgeOrig = reihenfolgeOrig.substring(1);
    }

    public void addMannschaft(final Mannschaft m) {
        this.finalList.add(m);
        reihenfolge = "";
        for (Mannschaft m2 : finalList) {
            reihenfolge = reihenfolge + "," + m2.getNameNoNickname();
        }
        reihenfolge = reihenfolge.substring(1);
        reihenfolge = reihenfolge.toUpperCase();
    }

    public boolean contains(final Mannschaft m) {
        for (final Mannschaft ma : this.finalList) {
            if (ma.getNameNoNickname().endsWith(m.getNameNoNickname())) {
                return true;
            }
        }
        return false;
    }

    public int getRang(final Mannschaft m) throws TurnierException {
        if (!this.gespielt) {
            log.warn("penalty noch nicht gespielt");
            throw new TurnierException("penalty noch nicht gespielt");
        }
        int i = 1;
        for (final Mannschaft m2 : this.getFinalList()) {
            if (m.getNameNoNickname().equals(m2.getNameNoNickname())) {
                return i;
            }
            i++;
        }
        return -1;
    }

    public void setGr(final Gruppe gr) {
        this.gruppe = gr;
    }

    // normale getter & setter
    public Gruppe getKategorie() {
        return this.gruppe;
    }

    public void setKategorie(final Gruppe kategorie) {
        this.gruppe = kategorie;
    }

    public boolean isBestaetigt() {
        return this.bestaetigt;
    }

    public void setBestaetigt(final boolean bestaetigt) {
        this.bestaetigt = bestaetigt;
    }

    public boolean isGespielt() {
        return this.gespielt;
    }

    public void setGespielt(final boolean gespielt) {
        this.gespielt = gespielt;
    }

    public List<Mannschaft> getFinalList() {
        List<Mannschaft> result = new ArrayList<>();

        String vergleich = "";
        if (!this.reihenfolge.equals(LEER)) {
            vergleich = this.reihenfolge;
        } else {
            vergleich = this.reihenfolgeOrig;
        }

        String[] re = vergleich.split(",");

        for (String str : re) {
            for (Mannschaft m : this.finalList) {
                if (str.equalsIgnoreCase(m.getNameNoNickname())) {
                    result.add(m);
                }
            }
        }
        return result;

    }

    public void setFinalList(List<Mannschaft> in) {
        this.finalList.clear();
        for (Mannschaft m : in) {
            this.addMannschaft(m);
        }
    }

    public List<Mannschaft> getRealFinalList() {
        return this.finalList;
    }

    @Override
    public String toString() {
        return toMannschaftsString();
    }

    public String toMannschaftsString() {

        if (finalList.size() == 0) {
            return "penalty ohne mannschaften " + this.idString;
        }

        StringBuilder stringBuilder = new StringBuilder();
        Mannschaft latest = null;
        for (Mannschaft m : finalList) {
            stringBuilder.append(m.getNameNoNickname()).append(",");
            latest = m;
        }
        String ret = stringBuilder.toString();
        return ret.replace(latest.getNameNoNickname() + ",", latest.getNameNoNickname().toUpperCase());
    }

    public String getReihenfolge() {
        return reihenfolge;
    }

    public void setReihenfolge(String reihenfolge) {
        this.reihenfolge = reihenfolge;
    }

    public String getIdString() {
        return idString;
    }

    public void setIdString(String idString) {
        this.idString = idString;
    }

    public String getReihenfolgeOrig() {
        return reihenfolgeOrig;
    }

    public void setReihenfolgeOrig(String reihenfolgeOrig) {
        this.reihenfolgeOrig = reihenfolgeOrig;
    }

}
