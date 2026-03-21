package ch.plaintext.schuetu.service.report;

import ch.plaintext.schuetu.service.GameSelectionHolder;
import ch.plaintext.schuetu.service.websiteinfo.WebsiteInfoService;
import ch.plaintext.schuetu.entity.Kategorie;
import ch.plaintext.schuetu.entity.Mannschaft;
import ch.plaintext.schuetu.entity.Spiel;
import ch.plaintext.schuetu.model.comparators.GenericSorter;
import ch.plaintext.schuetu.repository.MannschaftRepository;
import ch.plaintext.schuetu.repository.SpielRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@Scope("session")
@Slf4j
public class HeftliDumper {

    @Autowired
    private WebsiteInfoService info;
    @Autowired
    private MannschaftRepository mannschaftRepository;
    @Autowired
    private SpielRepository spielRepository;
    @Autowired
    private GameSelectionHolder game;

    public List<List<String>> getSpiele() {
        List<List<String>> ret = new ArrayList<>();
        for (Spiel spiel : spielRepository.findGruppenSpielAsc(game.getGameName())) {
            ret.add(this.printSpielZeile(spiel));
        }
        return ret;
    }

    public List<List<String>> getTeams() {
        List<List<String>> ret = new ArrayList<>();
        List<Mannschaft> ma = mannschaftRepository.findByGame(this.game.getGame().getModel().getGameName());
        GenericSorter.sortAsc(ma, "getShortKatName");
        for (Mannschaft zeile : ma) { ret.add(printTeam(zeile)); }
        return ret;
    }

    public List<List<String>> getFinale() {
        List<List<String>> ret = new ArrayList<>();
        for (Spiel spiel : spielRepository.findFinalSpielAsc(game.getGameName())) {
            SimpleDateFormat form = new SimpleDateFormat("HH:mm");
            String zeit = form.format(spiel.getStart());
            String day = new SimpleDateFormat("EEEE").format(spiel.getStart());
            day = day.replace("Saturday", "Sa"); day = day.replace("Sunday", "So");
            String sp = spiel.toString();
            sp = sp.replace("KlFin-", "Kl. Final "); sp = sp.replace("GrFin-", "Gr. Final ");
            sp = sp.replace("MKl", "Maedchen Kl"); sp = sp.replace("KKl", "Knaben Kl");
            List<String> list = new ArrayList<>();
            list.add(day + " " + zeit); list.add("" + spiel.getPlatz()); list.add(sp);
            ret.add(list);
        }
        return ret;
    }

    public List<List<String>> getKategorien() {
        List<List<String>> ret = new ArrayList<>();
        Set<Kategorie> kategorien = new HashSet<>();
        for (Spiel spiel : spielRepository.findGruppenSpielAsc(game.getGameName())) {
            kategorien.add(spiel.getMannschaftA().getKategorie());
        }
        List<Kategorie> kat = new ArrayList<>(kategorien);
        GenericSorter.sortAsc(kat, "getShortKatName");
        for (Kategorie kat2 : kat) { ret.addAll(this.printKategorie(kat2)); }
        return ret;
    }

    private List<String> printSpielZeile(Spiel spiel) {
        List<String> ret = new ArrayList<>();
        SimpleDateFormat form = new SimpleDateFormat("HH:mm");
        String zeit = form.format(spiel.getStart());
        String day = new SimpleDateFormat("EEEE").format(spiel.getStart());
        day = day.replace("Saturday", "Sa"); day = day.replace("Sunday", "So");
        ret.add(day + " " + zeit); ret.add("" + spiel.getPlatz());
        ret.add(spiel.getMannschaftA().getName()); ret.add(spiel.getMannschaftB().getName());
        return ret;
    }

    private List<List<String>> printKategorie(Kategorie in) {
        List<List<String>> ret = new ArrayList<>();
        List<String> ll = new ArrayList<>();
        ll.add("Gruppe " + in.getName()); ll.add(""); ll.add("");
        ret.add(ll);
        for (Mannschaft m : in.getMannschaften()) {
            List<String> mm = new ArrayList<>();
            mm.add(m.getName()); mm.add(m.getSchulhaus()); mm.add("" + m.getKlasse());
            ret.add(mm);
        }
        return ret;
    }

    private List<String> printTeam(Mannschaft in) {
        List<String> ret = new ArrayList<>();
        ret.add(in.getShortKatName()); ret.add(in.getName()); ret.add(in.getSchulhaus());
        ret.add("" + in.getKlasse());
        ret.add(in.getCaptain2Vorname() + " " + in.getCaptain2Name());
        ret.add(in.getBegleitpersonAnrede() + " " + in.getBegleitperson2Vorname() + " " + in.getBegleitperson2Name());
        return ret;
    }

}
