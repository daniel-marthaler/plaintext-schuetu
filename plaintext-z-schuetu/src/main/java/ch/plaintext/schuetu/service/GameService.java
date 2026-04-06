package ch.plaintext.schuetu.service;

import ch.plaintext.schuetu.entity.*;
import ch.plaintext.schuetu.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.apache.commons.beanutils.BeanUtils;

import java.util.Date;
import java.util.List;

/**
 * Loeschen, Umbenennen und Kopieren eines Games
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

    @Autowired
    private SchiriRepository schiriRepo;

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

    @Transactional
    public void renameGame(String oldName, String newName) {
        log.info("Renaming game '{}' to '{}'", oldName, newName);

        // Mannschaften
        List<Mannschaft> mannschaften = mannschaftRepo.findByGame(oldName);
        for (Mannschaft m : mannschaften) {
            m.setGame(newName);
            mannschaftRepo.save(m);
        }

        // Spiele
        List<Spiel> spiele = spielRepo.findByGame(oldName);
        for (Spiel spiel : spiele) {
            spiel.setGame(newName);
            spielRepo.save(spiel);
        }

        // SpielZeilen
        List<SpielZeile> spielzeilen = spielzeilenRepo.findByGame(oldName);
        for (SpielZeile z : spielzeilen) {
            z.setGame(newName);
            spielzeilenRepo.save(z);
        }

        // Kategorien
        List<Kategorie> kategorien = kategorieRepository.findByGame(oldName);
        for (Kategorie k : kategorien) {
            k.setGame(newName);
            kategorieRepository.save(k);
        }

        // Schiris
        List<Schiri> schiris = schiriRepo.findByGame(oldName);
        for (Schiri s : schiris) {
            s.setGame(newName);
            schiriRepo.save(s);
        }

        // GameModel
        GameModel game = gameRepository.findByGameName(oldName);
        if (game != null) {
            game.setGameName(newName);
            gameRepository.save(game);
        }

        log.info("Game renamed from '{}' to '{}'", oldName, newName);
    }

    @Transactional
    public void copyGame(String sourceGameName, String newGameName) {
        log.info("Copying game '{}' to '{}'", sourceGameName, newGameName);

        GameModel source = gameRepository.findByGameName(sourceGameName);
        if (source == null) {
            log.warn("Source game '{}' not found", sourceGameName);
            return;
        }

        if (gameRepository.findByGameName(newGameName) != null) {
            log.warn("Target game '{}' already exists", newGameName);
            return;
        }

        // GameModel kopieren
        GameModel newGame = new GameModel();
        try {
            BeanUtils.copyProperties(newGame, source);
        } catch (Exception e) {
            log.error("Error copying GameModel", e);
            return;
        }
        newGame.setId(null);
        newGame.setGameName(newGameName);
        newGame.setCreationdate(new Date());
        newGame.setSpielPhase("anmeldung");
        newGame.setInitialisiert(false);
        gameRepository.save(newGame);

        // Mannschaften kopieren
        List<Mannschaft> mannschaften = mannschaftRepo.findByGame(sourceGameName);
        for (Mannschaft m : mannschaften) {
            Mannschaft copy = new Mannschaft();
            try {
                BeanUtils.copyProperties(copy, m);
            } catch (Exception e) {
                log.error("Error copying Mannschaft", e);
            }
            copy.setId(null);
            copy.setGame(newGameName);
            copy.setGruppe(null);
            copy.setGruppeB(null);
            copy.setTeamNummer(0);
            copy.setCreationdate(new Date());
            mannschaftRepo.save(copy);
        }

        // Schiris kopieren
        List<Schiri> schiris = schiriRepo.findByGame(sourceGameName);
        for (Schiri s : schiris) {
            Schiri copy = new Schiri();
            try {
                BeanUtils.copyProperties(copy, s);
            } catch (Exception e) {
                log.error("Error copying Schiri", e);
            }
            copy.setId(null);
            copy.setGame(newGameName);
            copy.setMatchcount(0);
            copy.setSpielIDs("");
            copy.setAktiviert(false);
            copy.setCreationdate(new Date());
            schiriRepo.save(copy);
        }

        log.info("Game copied from '{}' to '{}' with {} teams and {} referees",
                sourceGameName, newGameName, mannschaften.size(), schiris.size());
    }

}
