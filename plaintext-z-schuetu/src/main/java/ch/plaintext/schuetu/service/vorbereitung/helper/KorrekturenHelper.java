package ch.plaintext.schuetu.service.vorbereitung.helper;

import ch.plaintext.schuetu.entity.SpielZeile;
import ch.plaintext.schuetu.repository.KorrekturPersistence;
import ch.plaintext.schuetu.repository.SpielZeilenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@Deprecated
public class KorrekturenHelper {

    @Autowired private KorrekturPersistence persistence;
    @Autowired private SpielZeilenRepository repo;

    public void spielZeileKorrigieren(String id) {
        zeileKorrigieren(id);
        persistence.save("spielzeile", id);
    }

    private void zeileKorrigieren(String id) {
        SpielZeile zeile = repo.findById(Long.valueOf(id)).get();
        zeile.setPause(!zeile.isPause());
        repo.save(zeile);
    }

    public void spielzeilenkorrekturAusDbAnwenden() {
        List<String> korrekturen = persistence.getKorrekturen("spielzeile");
        for (String id : korrekturen) { zeileKorrigieren(id); }
    }
}
