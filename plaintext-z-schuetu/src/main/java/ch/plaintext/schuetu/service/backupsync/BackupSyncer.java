package ch.plaintext.schuetu.service.backupsync;

import ch.plaintext.schuetu.service.Game;
import ch.plaintext.schuetu.service.utils.XstreamUtil;
import ch.plaintext.schuetu.entity.Spiel;
import ch.plaintext.schuetu.model.enums.SpielEnum;
import ch.plaintext.schuetu.model.enums.SpielZeilenPhaseEnum;
import ch.plaintext.schuetu.repository.SpielRepository;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Synchronizes game data from a backup source
 */
@Component
@Scope("prototype")
@Slf4j
public class BackupSyncer {

    @Getter
    private List<BackupSyncModel> eintraege = new ArrayList<>();

    // TODO: EmadIHTTP replaced - use RestTemplate or WebClient for HTTP calls
    // @Autowired
    // private EmadIHTTP http;

    private boolean finale = false;

    @Setter
    private Game game;

    @Scheduled(fixedRate = 20000)
    @Transactional
    public void sync() {

        if (game == null) {
            log.info("syncer with no game!");
            return;
        }

        // TODO: Re-implement HTTP sync with RestTemplate/WebClient
        // The old code used EmadIHTTP to fetch backup data from a remote URL
        // and XstreamUtil to deserialize the response
        log.debug("BackupSyncer.sync() - TODO: re-implement with RestTemplate");
    }

    private void signal(BackupSyncModel event) {

        SpielRepository repo = game.getSpielRepository();

        long id = Long.parseLong(event.getId());

        Spiel spiel = repo.findById(id).get();

        if (!finale & spiel.getTyp() != SpielEnum.GRUPPE) {
            try {
                Thread.sleep(40000);
            } catch (InterruptedException e) {
                log.info("warte 40 Sekunden, finale...");
            }
            finale = true;
        }

        spiel.setToreABestaetigt(event.getToreABestaetigt());
        spiel.setToreBBestaetigt(event.getToreBBestaetigt());

        spiel.setAmSpielen(false);
        spiel.setFertigGespielt(true);
        spiel.setFertigEingetragen(true);
        spiel.setFertigBestaetigt(true);
        spiel.setSpielZeilenPhase(SpielZeilenPhaseEnum.E_BEENDET);

        repo.save(spiel);

        game.getResultate().signalFertigesSpiel(id);

        game.getDurchfuehrung().reset();

    }

}
