package ch.plaintext.schuetu.service;

import ch.plaintext.schuetu.service.spieldurchfuehrung.EintragerService;
import ch.plaintext.schuetu.service.spieldurchfuehrung.PenaltyService;
import ch.plaintext.schuetu.service.spieldurchfuehrung.SpielDurchfuehrung;
import ch.plaintext.schuetu.service.zeit.Zeitgeber;
import ch.plaintext.schuetu.entity.GameModel;
import ch.plaintext.schuetu.entity.Mannschaft;
import ch.plaintext.schuetu.entity.Spiel;
import ch.plaintext.schuetu.repository.GameRepository;
import ch.plaintext.schuetu.repository.MannschaftRepository;
import ch.plaintext.schuetu.repository.SpielRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Erstellt neue Game Instanzen oder laed diese aus der datenbank und stellt sie
 * wieder her
 */
@Component
@Scope("prototype")
@Data
public class Game {

    Set<GameConnectable> connectables = ConcurrentHashMap.newKeySet();

    @Autowired
    private MannschaftRepository mannschaftsRepo;

    @Autowired
    private GameRepository gameRepo;

    @Autowired
    private SpielRepository spielRepository;

    @Autowired
    private Zeitgeber zeit;

    @Autowired
    private SpielDurchfuehrung durchfuehrung;

    private EintragerService eintragen;

    private ResultateVerarbeiter resultate;

    private PenaltyService penalty;

    @Autowired
    private ApplicationContext ctx;

    private GameModel model;

    public void addGameConnectable(GameConnectable conn) {
        connectables.add(conn);
    }

    public void removeGameConnectable(GameConnectable conn) {
        connectables.remove(conn);
    }

    public void recalculateResultate() {
        init();
    }

    public void spielzeitEinholen60() {
        if (zeit != null) {
            zeit.spielzeitEinholen60();
        }
        if (durchfuehrung != null) {
            durchfuehrung.spielzeitEinholen(60);
        }
    }

    @PostConstruct
    private void postConstruct() {
        durchfuehrung.setZeitgeber(zeit);
    }

    public void init() {
        resultate = ctx.getBean(ResultateVerarbeiter.class);
        resultate.setGame(this);

        eintragen = ctx.getBean(EintragerService.class);
        eintragen.setGame(this);

        penalty = ctx.getBean(PenaltyService.class);
        penalty.setGame(this);

        List<Spiel> spiele = spielRepository.findByGame(model.getGameName());
        for (Spiel spiel : spiele) {
            if (spiel.isFertigBestaetigt()) {
                resultate.signalFertigesSpiel(spiel.getId());
            }
        }
        neuRechnen();
        resultate.processQueueNow();
    }

    public void setModel(GameModel model) {
        this.model = model;
        this.model = gameRepo.save(this.model);
        durchfuehrung.setGame(this);

        zeit.setGame(this);
        connectables.add(durchfuehrung);
        durchfuehrung.setGame(this);
        durchfuehrung.setZeitgeber(zeit);
        durchfuehrung.getDurchfuehrungData().setGameName(model.getGameName());
    }

    public void setSpielPhase(String phase) {
        model.setSpielPhase(phase);
        setModel(model);
    }

    public void neuRechnen() {
        resultate.neuberechnenAlleKategorien();
    }

    public List<Mannschaft> findMannschaften() {
        return mannschaftsRepo.findByGame(model.getGameName());
    }

    public List<Mannschaft> findMannschaftenMitDuplikatserkennung() {
        List<Mannschaft> temp = mannschaftsRepo.findByGame(model.getGameName());

        List<Mannschaft> found = new ArrayList<>();
        List<Mannschaft> foundAll = new ArrayList<>();
        int foundcount = 0;

        for (int i = 0; i < temp.size(); i++) {

            boolean gotMatch = false;
            Mannschaft base = temp.get(i);

            for (int j = i + 1; j < temp.size(); j++) {

                Mannschaft check = temp.get(j);

                if (base.getBegleitperson2Name().trim().equalsIgnoreCase(check.getBegleitperson2Name())) {

                    if (base.getBegleitperson2Vorname().trim().equalsIgnoreCase(check.getBegleitperson2Vorname())) {
                        if (base.getBegleitpersonStrasse().trim().equalsIgnoreCase(check.getBegleitpersonStrasse())) {

                            if (!gotMatch && !foundAll.contains(base)) {
                                found.add(base);
                                gotMatch = true;
                                foundAll.add(base);
                                foundcount++;
                            }

                            if (!foundAll.contains(check)) {
                                foundAll.add(check);
                                found.add(check);
                            }

                        }

                    }

                }

            }

            // setzen
            int count = 0;

            for (Mannschaft m : found) {
                count = count + m.getAnzahlSpieler();
            }

            for (Mannschaft m : found) {
                m.setAnzahlSpieler(count);
                m.setDoppelterBetreuer("Doppelt Fall " + foundcount);
            }

            found.clear();

        }
        return temp;

    }

}
