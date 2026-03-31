package ch.plaintext.schuetu.entity;

import ch.plaintext.framework.SuperModel;
import ch.plaintext.schuetu.model.CreationDateProvider;
import ch.plaintext.schuetu.model.comparators.MannschaftsNameComparator;
import ch.plaintext.schuetu.model.comparators.SpielZeitComparator;
import ch.plaintext.schuetu.model.enums.SpielTageszeit;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import jakarta.persistence.*;
import java.util.*;

/**
 * Kategorie Entity
 */
@Entity
@Slf4j
@Data
@EqualsAndHashCode(callSuper = false, exclude = {"gruppeA", "gruppeB"})
public class Kategorie extends SuperModel implements CreationDateProvider {

    private Date creationdate = new Date();

    // beim erstellen der rangliste wird nicht gespeicher falls true
    private Boolean nosave = Boolean.FALSE;

    private String game;

    private String eintrager;

    private static final String KATEGORIEKUERZEL = "Kl";

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Spiel kleineFinal = null;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Spiel grosserFinal = null;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Spiel grosserfinal2 = null;

    @OneToOne(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
    private Gruppe gruppeA;

    @OneToOne(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
    private Gruppe gruppeB;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Penalty penaltyA = null;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Penalty penaltyB = null;

    private SpielTageszeit spielwunsch = SpielTageszeit.EGAL;

    @Transient
    private Boolean first = Boolean.FALSE;

    @Transient
    private Boolean last = Boolean.FALSE;

    private String notitzen;

    public String getName() {

        if (this.getGruppeA() == null) {
            return "invalide";
        }

        String retValue = "" + this.gruppeA.getGeschlecht() + Kategorie.KATEGORIEKUERZEL;
        if (this.gruppeA == null) {
            Kategorie.log.warn("!!! es wurde versucht einen kategorienamen auszugeben von einer kategorie ohne gruppeA: " + this.toString());
            return "" + retValue + "_OHNE_GRUPPE_A";
        }
        // klassenteil
        retValue += this.getKlassenString();
        return retValue;
    }

    public String getKlassenString() {
        // klassenteil
        String klasse;

        final List<Integer> klassen = getKlassen();
        StringBuilder klasseBuilder = new StringBuilder();
        for (final Integer klasseEnum : klassen) {
            klasseBuilder.append(klasseEnum).append("&");
        }
        klasse = klasseBuilder.toString();
        if (klasse.endsWith("&")) {
            klasse = klasse.substring(0, klasse.length() - 1);
        }
        return klasse;
    }

    public List<Integer> getKlassen() {

        final Set<Integer> result = new HashSet<>();

        if (this.gruppeA != null) {
            for (final Mannschaft mannschaft : this.gruppeA.getMannschaften()) {
                result.add(mannschaft.getKlasse());
            }
        }

        if (this.gruppeB != null) {
            for (final Mannschaft mannschaft : this.gruppeB.getMannschaften()) {
                result.add(mannschaft.getKlasse());
            }
        }

        final List<Integer> resultList = new ArrayList<>();
        resultList.addAll(result);
        Collections.sort(resultList);
        return resultList;
    }

    public List<Mannschaft> getMannschaften() {

        final Set<Mannschaft> result = new HashSet<>();

        if (this.gruppeA != null && this.gruppeA.getMannschaften() != null) {
            result.addAll(this.gruppeA.getMannschaften());
        }

        if (this.gruppeB != null && this.gruppeB.getMannschaften() != null) {
            result.addAll(this.gruppeB.getMannschaften());
        }

        final List<Mannschaft> resultList = new ArrayList<>();
        resultList.addAll(result);
        resultList.sort(new MannschaftsNameComparator());
        return resultList;
    }

    /**
     * Kategorie besteht aus mehreren Klassen, von einer Klasse ist aber nur
     * eine Mannschaft vertreten Wird benoetigt um zu bestimmen ob 2 grosse
     * Finale gespielt werden oder nicht
     *
     * @return true oder false
     */
    public boolean isMixedAndWithEinzelklasse() {
        Map<Integer, List<Mannschaft>> mannschaften = getMannschaftenProKlasse();

        for (List<Mannschaft> ms : mannschaften.values()) {
            if (ms.size() < 2) {
                return true;
            }
        }
        return false;
    }

    /**
     * Kategorie besteht aus mehreren Klassen, von einer Klasse ist aber nur
     * eine Mannschaft vertreten Wird benoetigt um zu bestimmen ob 2 grosse
     * Finale gespielt werden oder nicht
     *
     * @return true oder false
     */
    public int computeAnzahlFinale() {

        int ret = 0;

        Map<Integer, List<Mannschaft>> mannschaften = getMannschaftenProKlasse();

        for (List<Mannschaft> ms : mannschaften.values()) {
            if (ms.size() > 1) {
                ret++;
            }
            if (ms.size() > 3) {
                ret++;
            }
        }
        return ret;
    }

    public List<Mannschaft> getGroessereMannschaftsGruppe() {
        Map<Integer, List<Mannschaft>> mannschaften = getMannschaftenProKlasse();
        List<Mannschaft> biggest = null;
        for (List<Mannschaft> ms : mannschaften.values()) {
            if (biggest == null) {
                biggest = ms;
            }
            if (biggest.size() < ms.size()) {
                biggest = ms;
            }
        }
        return biggest;
    }

    public List<Mannschaft> getKleinereMannschaftsGruppe() {
        Map<Integer, List<Mannschaft>> mannschaften = getMannschaftenProKlasse();
        List<Mannschaft> smalest = null;
        for (List<Mannschaft> ms : mannschaften.values()) {
            if (smalest == null) {
                smalest = ms;
            }
            if (smalest.size() > ms.size()) {
                smalest = ms;
            }
        }
        return smalest;
    }

    public List<Spiel> getDirektbegegnungen(Mannschaft a, Mannschaft b) {

        List<Spiel> result = new ArrayList<>();
        Set<Spiel> alle = getSpiele();
        for (Spiel sp : alle) {
            if (sp.getMannschaftAName().equals(a.getName()) && sp.getMannschaftBName().equals(b.getName())) {
                result.add(sp);
            } else if (sp.getMannschaftBName().equals(a.getName()) && sp.getMannschaftAName().equals(b.getName())) {
                result.add(sp);
            }
        }
        return result;
    }

    /**
     * Ist immer dann der Fall, wenn nur 3 Manschaften in der Gruppe sind,
     * weniger als zwei werden einer anderen Kategorie zugeteilt mehr spielen
     * regulaer
     *
     * @return true oder false
     */
    public boolean hasVorUndRueckrunde() {
        return this.gruppeA != null && (gruppeA.getMannschaften().size() == 3);
    }

    public boolean has2Groups() {
        return this.gruppeB != null && this.gruppeB.getMannschaften().size() > 0;
    }

    public boolean isMixedKlassen() {
        int klasse = this.getMannschaften().get(0).getKlasse();
        for (Mannschaft m : this.getMannschaften()) {
            if (klasse != m.getKlasse()) {
                return true;
            }
        }
        return false;
    }

    private Map<Integer, List<Mannschaft>> getMannschaftenProKlasse() {
        Map<Integer, List<Mannschaft>> ret = new HashMap<>();
        for (Mannschaft mannschaft : getMannschaften()) {
            List<Mannschaft> ms = ret.get(mannschaft.getKlasse());
            if (ms == null) {
                ms = new ArrayList<>();
            }
            ms.add(mannschaft);
            ret.put(mannschaft.getKlasse(), ms);
        }
        return ret;
    }

    public Set<Spiel> getSpiele() {
        Set<Spiel> spiele = new HashSet<>();
        if (this.gruppeA != null) {
            spiele.addAll(this.gruppeA.getSpiele());
        }
        if (this.gruppeB != null) {
            spiele.addAll(this.gruppeB.getSpiele());
        }
        return spiele;
    }

    public List<Spiel> getSpieleSorted() {
        List<Spiel> spiele = new ArrayList<>();
        spiele.addAll(getSpiele());
        spiele.sort(new SpielZeitComparator());
        return spiele;
    }

    public Spiel getLatestSpiel() {
        return getSpieleSorted().remove(getSpieleSorted().size() - 1);
    }

    public boolean isGruppenspieleFertig() {

        List<Spiel> spiele = getSpieleSorted();

        for (Spiel spiel : spiele) {
            if (!spiel.isFertigBestaetigt()) {
                return false;
            }
        }
        return true;
    }

    public boolean isFertigGespielt() {

        if (this.isGruppenspieleFertig() && this.getGrosserFinal().isFertigBestaetigt()) {
            if (this.kleineFinal != null) {
                if (!this.kleineFinal.isFertigBestaetigt()) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public String getShortKatName() {
        return this.getMannschaften().get(0).getGeschlecht().toString() + this.getMannschaften().get(0).getKlasse();
    }

    public int evaluateLowestClass() {
        int result = 10;
        List<Integer> inte = this.getKlassen();
        if (inte == null || inte.isEmpty()) {
            return -1;
        }
        for (Integer i : inte) {
            if (i < result) {
                result = i;
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "Kategorie{" +
                "id=" + super.getId() +
                ", creationdate=" + creationdate +
                ", nosave=" + nosave +
                ", game='" + game + '\'' +
                ", eintrager='" + eintrager + '\'' +
                ", kleineFinal=" + kleineFinal +
                ", grosserFinal=" + grosserFinal +
                ", grosserfinal2=" + grosserfinal2 +
                ", penaltyA=" + penaltyA +
                ", penaltyB=" + penaltyB +
                ", spielwunsch=" + spielwunsch +
                ", first=" + first +
                ", last=" + last +
                ", notitzen='" + notitzen + '\'' +
                '}';
    }
}
