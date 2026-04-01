package ch.plaintext.schuetu.service;

import ch.plaintext.schuetu.service.vorbereitung.B1KategorienZuordner;
import ch.plaintext.schuetu.service.vorbereitung.C3MannschaftenAufteiler;
import ch.plaintext.schuetu.service.vorbereitung.D4GeneratePaarungenAndSpiele;
import ch.plaintext.schuetu.service.websiteinfo.VelocityReplacer;
import ch.plaintext.schuetu.entity.GameModel;
import ch.plaintext.schuetu.entity.Kategorie;
import ch.plaintext.schuetu.entity.Spiel;
import ch.plaintext.schuetu.model.comparators.SpielZeitComparator;
import ch.plaintext.schuetu.repository.GameRepository;
import ch.plaintext.schuetu.repository.KategorieRepository;
import ch.plaintext.schuetu.repository.SpielRepository;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import java.util.List;

/**
 * Haelt ein gestartetes Game fuer die Session bereit
 */
@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
@Data
@Slf4j
@ToString(exclude = "menu")
public class GameSelectionHolder {

    private Game game;

    @Autowired
    private GameRoot root;

    // TODO: PlanungRootMenu was from old web package - wire up if needed
    // @Autowired
    // private PlanungRootMenu menu;

    @Autowired
    private VelocityReplacer website;

    @Autowired
    private SpielzeilenService spielZeitenAnpassen;

    @Autowired
    private GameRepository repo;

    // zur logik
    @Autowired
    private B1KategorienZuordner kategorieZuordnen;

    @Autowired
    private C3MannschaftenAufteiler mannschaftVerteilen;

    @Autowired
    private D4GeneratePaarungenAndSpiele spieleGenerator;

    @Autowired
    private KategorieRepository kategorieRepository;

    @Autowired
    private VelocityReplacer velocity;

    @Autowired
    private SpielRepository spielRepository;

    public Boolean hasGame() {
        return game != null;
    }

    public String selectGame(String name) {
        game = root.selectGame(name);
        if (game == null) {
            return "dashboard.htm";
        }
        velocity.dump(game.getModel().getGameName());
        return "dashboard.htm";
    }

    public void save() {
        GameModel model = repo.save(game.getModel());
        game.setModel(model);
        website.dump(model.getGameName(), model.getWebsiteId(), model.getWebsiteUrl());
    }

    public boolean isAnmeldung() {
        if (game == null) {
            log.info("game not selected");
            return false;
        }
        return game.getModel().getSpielPhase().equals("anmeldung");
    }

    public void phaseAnmeldung() {
        try {
            toKategorie();
            game.getModel().setSpielPhase("kategorie");
            game.setModel(repo.save(game.getModel()));
        } catch (Exception e) {
            log.error("Fehler beim Wechsel zur Anmeldephase: {}", e.getMessage(), e);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Fehler beim Phasenwechsel",
                            "Kategorie-Zuordnung fehlgeschlagen: " + e.getMessage()));
        }
    }

    public void neueZeit() {
        this.save();
    }

    public void phaseKategoriezuordnung() {
        toSpieltage();
        game.getModel().setSpielPhase("spieltage");
        game.setModel(repo.save(game.getModel()));
    }

    public void neuRechnen() {
        game.neuRechnen();
    }

    private void toKategorie() {
        log.info("wechsel zu: kategorie");
        kategorieZuordnen.automatischeZuordnung(game.getModel().getGameName());
    }

    private void toSpieltage() {
        log.info("wechsel zu: spieltage");
        log.info("*** mannschaftVerteilen.mannschaftenVerteilen");
        mannschaftVerteilen.mannschaftenVerteilen(game.getModel().getGameName(), game.getModel());

        log.info("*** spieleGenerator.generatPaarungenAndSpiele");
        spieleGenerator.generatPaarungenAndSpiele(game.getModel().getGameName());
    }

    public void phaseSpielen() {
        log.info("wechsel to phaseSpielen()");
        game.getModel().setSpielPhase("spielen");
        spielZeitenAnpassen.spielZeitenAnpassen();
        game.setModel(repo.save(game.getModel()));
    }

    public String getGameName() {
        if (game == null) return "";
        return game.getModel().getGameName();
    }

    public List<Spiel> readAllSpiele() {
        List<Spiel> spiele = spielRepository.findByGame(getGameName());
        spiele.sort(new SpielZeitComparator());
        return spiele;
    }

    public Spiel findSpiel(String id) {
        return spielRepository.findById(Long.parseLong(id)).get();
    }

    public void doKorrektur(Spiel spiel) {
        spielRepository.save(spiel);
        game.getResultate().neuberechnenDerKategorie(spiel.getGruppe().getKategorie(), getGameName());
    }

}
