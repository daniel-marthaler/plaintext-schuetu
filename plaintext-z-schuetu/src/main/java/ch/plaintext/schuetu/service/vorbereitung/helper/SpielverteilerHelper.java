package ch.plaintext.schuetu.service.vorbereitung.helper;

import ch.plaintext.schuetu.entity.Spiel;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SpielverteilerHelper {

    private static final String CONSUMED_KEY = "consumed";
    private static final String AVAILABLE_KEY = "available";
    private Map<String, Map<String, Integer>> map = new HashMap<>();

    public void init(List<Spiel> spiele) {
        map.clear();
        for (Spiel spiel : spiele) {
            String katname = spiel.getMannschaftA().getKategorie().getName();
            Map<String, Integer> counter = map.get(katname);
            if (counter == null) { counter = new HashMap<>(); counter.put(CONSUMED_KEY, 0); counter.put(AVAILABLE_KEY, 0); }
            Integer avn = counter.get(AVAILABLE_KEY); avn = avn + 1; counter.put(AVAILABLE_KEY, avn);
            map.put(katname, counter);
        }
    }

    public boolean isFirstSpielNotInGruppe(Spiel spiel) {
        String katname = spiel.getMannschaftA().getKategorie().getName();
        return map.get(katname).get(CONSUMED_KEY) != 0;
    }

    public void consumeSpiel(Spiel spiel) {
        String katname = spiel.getMannschaftA().getKategorie().getName();
        Map<String, Integer> counter = map.get(katname);
        counter.put(AVAILABLE_KEY, counter.get(AVAILABLE_KEY) - 1);
        counter.put(CONSUMED_KEY, counter.get(CONSUMED_KEY) + 1);
        map.put(katname, counter);
    }
}
