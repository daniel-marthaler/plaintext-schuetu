package ch.plaintext.schuetu.entity;

import ch.plaintext.framework.SuperModel;
import ch.plaintext.schuetu.model.CreationDateProvider;
import ch.plaintext.schuetu.model.TurnierException;
import ch.plaintext.schuetu.model.enums.GeschlechtEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Gruppe Entity
 */
@Entity
@Slf4j
@Data
@EqualsAndHashCode(callSuper = false, exclude = {"kategorie"})
public class Gruppe extends SuperModel implements CreationDateProvider {

    private Date creationdate = new Date();

    private String game;

    private static final String GRUPPENKUERZEL = "Gr";

    @OneToOne(cascade = CascadeType.ALL)
    private Kategorie kategorie = null;

    private GeschlechtEnum geschlecht = null;

    @OneToMany(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH}, fetch = FetchType.EAGER)
    private List<Mannschaft> mannschaften = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Spiel> spiele = new ArrayList<>();

    /**
     * Dient dem ausfuellen des Geschlecht Feldes vor dem Speichern, uber den
     * getGeschlecht Aufruf
     */
    @PrePersist
    private void geschlechtFeldAusfuellen() {
        getGeschlecht();
    }

    /**
     * Gibt das Geschlecht der Gruppe zurueck. Dies geschieht aufgrund des
     * Geschlechs der Mannschaften falls eine Gemischte Gruppe gefunden wird,
     * wird null zurueckgegeben und eine Warning ins Log geschrieben
     *
     * @return geschlecht der gruppe
     */
    public GeschlechtEnum getGeschlecht() {

        if (geschlecht != null) {
            return geschlecht;
        }

        if (this.kategorie != null && this.kategorie.getMannschaften() != null && this.kategorie.getMannschaften().size() > 0) {
            geschlecht = this.kategorie.getMannschaften().get(0).getGeschlecht();
        }

        // fall, dass kategorie noch nicht gesetzt ist aber bereits mannschaften
        // in der gruppe sind
        if (this.mannschaften != null && this.mannschaften.size() > 0) {
            geschlecht = this.mannschaften.get(0).getGeschlecht();
        }

        return geschlecht;
    }

    public int evaluateFloorKlasse() {
        if (this.mannschaften == null || this.mannschaften.size() < 1) {
            return -1;
        }
        int ret = 11;
        for (Mannschaft m : this.mannschaften) {
            if (m.getKlasse() < ret) {
                ret = m.getKlasse();
            }
        }
        return ret;
    }

    /**
     * fuegt eine mannschaft der Liste hinzu wenn versucht wird eine mannschaft
     * einzufuegen, die eine gemischte gruppe zur folge haette, fliegt eine
     * TurnierException
     *
     * @param mannschaft die Mannschaft zum adden
     */
    public void addMannschaft(final Mannschaft mannschaft) throws TurnierException {
        GeschlechtEnum basis;
        if (this.mannschaften.size() > 1) {
            basis = this.mannschaften.get(0).getGeschlecht();
        } else {
            this.mannschaften.add(mannschaft);
            return;
        }

        for (final Mannschaft vergleich : this.mannschaften) {
            if (vergleich.getGeschlecht() != basis) {
                throw new TurnierException("!!! achtung, gemischte gruppen sind nicht erlaubt");
            }
        }
        this.getGeschlecht();
        this.mannschaften.add(mannschaft);
    }

    public String getName() {
        String retValue = "" + getGeschlecht() + Gruppe.GRUPPENKUERZEL;
        if (this.kategorie == null) {
            Gruppe.log.warn("!!! es wurde versucht einen gruppennamen auszugeben von einer gruppe ohne kategorie: " + this.toString());
            return "" + retValue + "_OHNE_KATEGORIE";
        }

        final String klasse = this.kategorie.getKlassenString();
        retValue += klasse;

        // A oder B?
        if ((this.kategorie.getGruppeA() == this) && (this.kategorie.getGruppeB() != null)) {
            retValue += "A";
        }
        if ((this.kategorie.getGruppeB() == this) && (this.kategorie.getGruppeA() != null)) {
            retValue += "B";
        }

        return retValue;
    }

    // getter & setter normal
    public List<Mannschaft> getMannschaften() {

        if (this.kategorie == null || this.kategorie.getGruppeB() == null || this.kategorie.getGruppeA() == null) {
            return this.mannschaften;
        }

        // workaround weil JPA anscheinend auf der kategorie den gruppennicht
        // die selben Mannschaften zuordnen kann
        if (this.kategorie != null && this == this.kategorie.getGruppeB() && this.kategorie.getGruppeA() != null && this.kategorie.getGruppeA().getMannschaften() != null
                && this.kategorie.getGruppeA().getMannschaften().size() == 3) {
            return this.kategorie.getGruppeA().getMannschaften();
        }

        return this.mannschaften;
    }

    public void setMannschaften(final List<Mannschaft> mannschaften) {
        this.mannschaften = mannschaften;
        // initialisiere Geschlecht
        this.getGeschlecht();
    }

}
