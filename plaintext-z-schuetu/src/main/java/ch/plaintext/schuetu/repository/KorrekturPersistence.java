package ch.plaintext.schuetu.repository;

import ch.plaintext.schuetu.entity.Korrektur;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Sortiert die gewuenschten Korrektur Eintraege heraus und gibt eine Liste
 * zurueck, welche nach der Erstellung sortiert eingetragen wird
 */
@Component
public class KorrekturPersistence {

    @Autowired
    private KorrekturRepository repo;

    public void save(String typ, String value) {
        save(typ, value, null);
    }

    public void save(String typ, String value, String game) {
        Korrektur korr = new Korrektur();
        korr.setTyp(typ);
        korr.setWert(value);
        korr.setGame(game != null ? game : "");
        korr.setReihenfolge(repo.count() + 1);
        repo.save(korr);
    }

    public List<String> getKorrekturen(String typ) {
        List<String> ret = new ArrayList<>();
        for (Korrektur ko : findByTyp(typ)) {
            if (ko.getTyp().equalsIgnoreCase(typ)) {
                ret.add(ko.getWert());
            }
        }
        return ret;
    }

    private List<Korrektur> findByTyp(String typ) {
        List<Korrektur> ret = new ArrayList<>();
        for (Korrektur ko : repo.findAll()) {
            if (ko.getTyp().equalsIgnoreCase(typ)) {
                ret.add(ko);
            }
        }
        return ret;
    }

    public void deteleAll(String typ) {
        for (Korrektur ko : repo.findAll()) {
            if (ko.getTyp().equalsIgnoreCase(typ)) {
                repo.delete(ko);
            }
        }
    }
}
