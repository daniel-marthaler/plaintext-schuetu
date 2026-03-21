package ch.plaintext.schuetu.service.mobile;

import ch.plaintext.schuetu.service.utils.DateUtil;
import ch.plaintext.schuetu.entity.Kategorie;
import ch.plaintext.schuetu.entity.Mannschaft;
import ch.plaintext.schuetu.repository.KategorieRepository;
import ch.plaintext.schuetu.repository.MannschaftRepository;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Component
@Scope("session")
public class MatchInfoService {

    private Map<String, String> mannschaftsNamen = new TreeMap<>();
    private Map<String, String> finalSpielPaarungBekannt = new HashMap<>();
    private Map<String, Mannschaft> mannschaftenM = new HashMap<>();
    private boolean init = false;

    @Autowired
    private MannschaftRepository mrepo;
    @Autowired
    private KategorieRepository krepo;

    public void init(String game) {
        if (init) { return; }
        for (Mannschaft mannschaft : mrepo.findByGame(game)) {
            mannschaftenM.put(mannschaft.getNameNoNickname(), mannschaft);
            mannschaftsNamen.put(mannschaft.getName(), mannschaft.getNameNoNickname());
        }
        init = true;
    }

    public Map<String, String> getMannschaftsNamen() { return mannschaftsNamen; }

    public String evaluateFinalSpielPaarungBekannt(String katName, String game) {
        if (finalSpielPaarungBekannt.size() < 1) { updateFinalSpielPaarungBekannt(game); }
        return finalSpielPaarungBekannt.get(katName);
    }

    public String evaluateFinalSpielPaarungBekannt(Mannschaft mann, String game) {
        return evaluateFinalSpielPaarungBekannt(mann.getKategorie().getName(), game);
    }

    private void updateFinalSpielPaarungBekannt(String gm) {
        for (Kategorie kategorie : krepo.findByGame(gm)) {
            Date start = kategorie.getLatestSpiel().getStart();
            DateTime startJ = new DateTime(start);
            startJ = startJ.plusMinutes(15);
            finalSpielPaarungBekannt.put(kategorie.getName(), DateUtil.getShortTimeDayString(startJ.toDate()));
        }
    }

    public Mannschaft getMannschaftByName(String name) {
        return this.mannschaftenM.get(name.toUpperCase());
    }

}
