package ch.plaintext.schuetu.service.websiteinfo.model;

import ch.plaintext.schuetu.model.enums.GeschlechtEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class KlassenrangZeile {

    @Getter
    @Setter
    private int klasse;

    private GeschlechtEnum geschlecht;

    private List<MannschaftEintrag> mannschaften = new ArrayList<>();

    public KlassenrangZeile() {
        mannschaften.add(new MannschaftEintrag());
        mannschaften.add(new MannschaftEintrag());
        mannschaften.add(new MannschaftEintrag());
        mannschaften.add(new MannschaftEintrag());
    }

    public List<List<String>> getAsZeilen() {

        List<List<String>> zeilen = new ArrayList<>();

        List<String> erste = new ArrayList<>();
        erste.add("");
        if (!mannschaften.get(0).getName().equals("M0XX")) {
            erste.add(mannschaften.get(0).getName());
        } else {
            erste.add("");
        }
        if (!mannschaften.get(1).getName().equals("M0XX")) {
            erste.add(mannschaften.get(1).getName());
        } else {
            erste.add("");
        }
        if (!mannschaften.get(2).getName().equals("M0XX")) {
            erste.add(mannschaften.get(2).getName());
        } else {
            erste.add("");
        }
        if (!mannschaften.get(3).getName().equals("M0XX")) {
            erste.add(mannschaften.get(3).getName());
        } else {
            erste.add("");
        }

        zeilen.add(erste);

        List<String> zweite = new ArrayList<>();
        zweite.add("Schulhaus");
        zweite.add(mannschaften.get(0).getSchulhaus());
        zweite.add(mannschaften.get(1).getSchulhaus());
        zweite.add(mannschaften.get(2).getSchulhaus());
        zweite.add(mannschaften.get(3).getSchulhaus());

        zeilen.add(zweite);

        List<String> dritte = new ArrayList<>();
        dritte.add("Captain");
        dritte.add(mannschaften.get(0).getCaptain());
        dritte.add(mannschaften.get(1).getCaptain());
        dritte.add(mannschaften.get(2).getCaptain());
        dritte.add(mannschaften.get(3).getCaptain());

        zeilen.add(dritte);

        List<String> vierte = new ArrayList<>();
        vierte.add("Begleitperson");
        vierte.add(mannschaften.get(0).getBegleitperson());
        vierte.add(mannschaften.get(1).getBegleitperson());
        vierte.add(mannschaften.get(2).getBegleitperson());
        vierte.add(mannschaften.get(3).getBegleitperson());

        zeilen.add(vierte);

        return zeilen;
    }

    public String getName() {
        StringBuilder builder = new StringBuilder();
        if (geschlecht == GeschlechtEnum.K) {
            builder.append("Knaben ");
        } else {
            builder.append("Maedchen ");
        }
        builder.append(klasse);
        builder.append(". Klasse");
        return builder.toString();
    }

    public String getNameInverse() {
        StringBuilder builder = new StringBuilder();
        builder.append(klasse);
        builder.append(". Klasse");
        if (geschlecht == GeschlechtEnum.K) {
            builder.append(" Knaben");
        } else {
            builder.append(" Maedchen");
        }
        return builder.toString();
    }

    public List<MannschaftEintrag> getMannschaften() {
        return this.mannschaften;
    }

    public void addNext(ch.plaintext.schuetu.entity.Mannschaft mannschaft) {
        for (MannschaftEintrag m : mannschaften) {
            if (!m.hasMannschaft()) {
                m.setMannschaft(mannschaft);
                return;
            }
        }
    }

    public GeschlechtEnum getGeschlecht() {
        return geschlecht;
    }

    public void setGeschlecht(GeschlechtEnum geschlecht) {
        this.geschlecht = geschlecht;
    }
}
