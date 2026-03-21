package ch.plaintext.schuetu.service.websiteinfo.model;

import ch.plaintext.schuetu.model.enums.GeschlechtEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class KlassenrangZeile {

    @Getter @Setter private int klasse;
    private GeschlechtEnum geschlecht;
    private List<MannschaftEintrag> mannschaften = new ArrayList<>();

    public KlassenrangZeile() {
        mannschaften.add(new MannschaftEintrag()); mannschaften.add(new MannschaftEintrag());
        mannschaften.add(new MannschaftEintrag()); mannschaften.add(new MannschaftEintrag());
    }

    public List<List<String>> getAsZeilen() {
        List<List<String>> zeilen = new ArrayList<>();
        List<String> erste = new ArrayList<>(); erste.add("");
        for (int i = 0; i < 4; i++) { erste.add(!mannschaften.get(i).getName().equals("M0XX") ? mannschaften.get(i).getName() : ""); }
        zeilen.add(erste);
        List<String> zweite = new ArrayList<>(); zweite.add("Schulhaus");
        for (int i = 0; i < 4; i++) { zweite.add(mannschaften.get(i).getSchulhaus()); }
        zeilen.add(zweite);
        List<String> dritte = new ArrayList<>(); dritte.add("Captain");
        for (int i = 0; i < 4; i++) { dritte.add(mannschaften.get(i).getCaptain()); }
        zeilen.add(dritte);
        List<String> vierte = new ArrayList<>(); vierte.add("Begleitperson");
        for (int i = 0; i < 4; i++) { vierte.add(mannschaften.get(i).getBegleitperson()); }
        zeilen.add(vierte);
        return zeilen;
    }

    public String getName() {
        StringBuilder builder = new StringBuilder();
        if (geschlecht == GeschlechtEnum.K) { builder.append("Knaben "); } else { builder.append("Maedchen "); }
        builder.append(klasse).append(". Klasse");
        return builder.toString();
    }

    public String getNameInverse() {
        StringBuilder builder = new StringBuilder();
        builder.append(klasse).append(". Klasse");
        if (geschlecht == GeschlechtEnum.K) { builder.append(" Knaben"); } else { builder.append(" Maedchen"); }
        return builder.toString();
    }

    public List<MannschaftEintrag> getMannschaften() { return this.mannschaften; }

    public void addNext(ch.plaintext.schuetu.entity.Mannschaft mannschaft) {
        for (MannschaftEintrag m : mannschaften) { if (!m.hasMannschaft()) { m.setMannschaft(mannschaft); return; } }
    }

    public GeschlechtEnum getGeschlecht() { return geschlecht; }
    public void setGeschlecht(GeschlechtEnum geschlecht) { this.geschlecht = geschlecht; }
}
