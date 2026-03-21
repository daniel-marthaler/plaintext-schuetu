package ch.plaintext.schuetu.service;

import ch.plaintext.schuetu.entity.*;
import ch.plaintext.schuetu.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Loeschen eines Games aus der DB
 */
@Service
@Slf4j
public class GameService {

    @Autowired
    private MannschaftRepository mannschaftRepo;

    @Autowired
    private SpielZeilenRepository spielzeilenRepo;

    @Autowired
    private SpielRepository spielRepo;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private KategorieRepository kategorieRepository;

    public void deleteGame(String gameName) {

        // Kategorien - remove fk's
        List<Kategorie> kategorien;
        try {
            kategorien = kategorieRepository.findByGame(gameName);
            for (Kategorie k : kategorien) {
                k.getSpiele().clear();
                k.getMannschaften().clear();
                k.getGruppeA().getMannschaften().clear();
                k.getGruppeB().getMannschaften().clear();

                k.getGruppeA().getSpiele().clear();
                k.getGruppeB().getSpiele().clear();

                k.setGrosserFinal(null);
                k.setKleineFinal(null);
                kategorieRepository.save(k);
            }
        } catch (Exception e) {
            log.info(e.getMessage());
        }

        // Spielzeilen
        List<SpielZeile> spielezeilen = spielzeilenRepo.findByGame(gameName);
        for (SpielZeile z : spielezeilen) {
            spielzeilenRepo.deleteById(z.getId());
        }

        // Spiele - remove Mannschaften
        List<Spiel> spiele = spielRepo.findByGame(gameName);
        for (Spiel spiel : spiele) {
            spiel.setMannschaftA(null);
            spiel.setMannschaftB(null);
            spielRepo.save(spiel);
        }

        // Spiele
        spiele = spielRepo.findByGame(gameName);
        for (Spiel spiel : spiele) {
            spielRepo.deleteById(spiel.getId());
        }

        // Mannschaften
        try {
            List<Mannschaft> mannschaften = mannschaftRepo.findByGame(gameName);
            for (Mannschaft m : mannschaften) {
                mannschaftRepo.deleteById(m.getId());
            }
        } catch (Exception e) {
            log.info(e.getMessage());
        }

        // Kategorien
        try {
            kategorien = kategorieRepository.findByGame(gameName);
            for (Kategorie k : kategorien) {
                kategorieRepository.deleteById(k.getId());
            }
        } catch (Exception e) {
            log.info(e.getMessage());
        }

        // Game
        GameModel game = gameRepository.findByGameName(gameName);
        gameRepository.delete(game);

    }

}
