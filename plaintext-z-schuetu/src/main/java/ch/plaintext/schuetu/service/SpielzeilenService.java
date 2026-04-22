package ch.plaintext.schuetu.service;

import ch.plaintext.schuetu.entity.Spiel;
import ch.plaintext.schuetu.entity.SpielZeile;
import ch.plaintext.schuetu.model.enums.PlatzEnum;
import ch.plaintext.schuetu.repository.SpielRepository;
import ch.plaintext.schuetu.repository.SpielZeilenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Spielzeilen manuell Vertauschen und Vertauschungen recorden
 */
@Component
@Slf4j
public class SpielzeilenService {

    @Autowired
    private GameSelectionHolder gameHolder;

    @Autowired
    private SpielRepository spielRepository;

    @Autowired
    private SpielZeilenRepository spielZeilenRepository;

    public void spielZeitenAnpassen() {
        spielZeitenAnpassen(gameHolder.getGameName());
    }

    public void spielZeitenAnpassen(String gameName) {

        if (gameName == null || gameName.isEmpty()) {
            log.debug("spielZeitenAnpassen skipped: no gameName");
            return;
        }

        List<SpielZeile> zeilen = spielZeilenRepository.findByGame(gameName);

        for (SpielZeile spielZeile : zeilen) {
            Spiel spiel = spielZeile.getA();
            if (spiel != null && !spiel.isPlatzhalter() && spiel.getId() > 0) {
                spiel.setStart(spielZeile.getStart());
                spiel.setPlatz(PlatzEnum.A);
                spielRepository.save(spiel);
            }
            spiel = spielZeile.getB();
            if (spiel != null && !spiel.isPlatzhalter() && spiel.getId() > 0) {
                spiel.setStart(spielZeile.getStart());
                spiel.setPlatz(PlatzEnum.B);
                spielRepository.save(spiel);
            }
            spiel = spielZeile.getC();
            if (spiel != null && !spiel.isPlatzhalter() && spiel.getId() > 0) {
                spiel.setStart(spielZeile.getStart());
                spiel.setPlatz(PlatzEnum.C);
                spielRepository.save(spiel);
            }
            spiel = spielZeile.getD();
            if (spiel != null && !spiel.isPlatzhalter() && spiel.getId() > 0) {
                spiel.setStart(spielZeile.getStart());
                spiel.setPlatz(PlatzEnum.D);
                spielRepository.save(spiel);
            }
        }

    }

}
