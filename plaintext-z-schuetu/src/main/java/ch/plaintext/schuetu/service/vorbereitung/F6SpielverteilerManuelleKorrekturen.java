package ch.plaintext.schuetu.service.vorbereitung;

import ch.plaintext.schuetu.entity.SpielZeile;
import ch.plaintext.schuetu.repository.KorrekturPersistence;
import ch.plaintext.schuetu.repository.SpielZeilenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class F6SpielverteilerManuelleKorrekturen {

    @Autowired private SpielZeilenRepository spielzeilenRepo;
    @Autowired private KorrekturPersistence korrekturPersistence;
    @Autowired private VertauschungsUtil util;

    public void korrekturenVornehmen() {
        List<String> korr = korrekturPersistence.getKorrekturen("spielvertauschung");
        if (korr == null || korr.isEmpty()) { log.info("starte manuelle Korrektur nicht, keine werte vorhanden"); return; }
        Iterable<SpielZeile> iter = spielzeilenRepo.findAll();
        util.korrekturenVornehmen(iter, korr);
        spielzeilenRepo.saveAll(iter);
        log.info("starte manuelle korrektur: ende");
    }
}
