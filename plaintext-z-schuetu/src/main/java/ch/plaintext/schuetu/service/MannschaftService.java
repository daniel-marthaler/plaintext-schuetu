package ch.plaintext.schuetu.service;

import ch.plaintext.schuetu.service.websiteinfo.VelocityReplacer;
import ch.plaintext.schuetu.service.websiteinfo.WebsiteInfoService;
import ch.plaintext.schuetu.entity.Mannschaft;
import ch.plaintext.schuetu.repository.GameRepository;
import ch.plaintext.schuetu.repository.MannschaftRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Vorwiegend zum Kopieren von Mannschaften
 */
@Service
@Slf4j
public class MannschaftService {

    @Autowired
    private MannschaftRepository repo;

    @Autowired
    private WebsiteInfoService infoService;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    VelocityReplacer website;

    public List<String> getJahre() {
        return new ArrayList<>(infoService.getOldJahre());
    }

    public void copyFromOldJahr(String jahr, String game) {

        for (Mannschaft man : getMannschaften(jahr, game)) {

            Mannschaft n = new Mannschaft();
            try {
                BeanUtils.copyProperties(n, man);
            } catch (IllegalAccessException | InvocationTargetException e) {
                log.error(e.getMessage(), e);
            }
            n.setId(null);
            n.setGame(game);
            n.setGruppe(null);
            n.setGruppe(null);
            n.setGruppeB(null);
            n.setTeamNummer(-1);
            repo.save(n);
        }
    }

    public void copyFromOldGame(String oldGame, String newGame) {

        List<Mannschaft> games = repo.findByGame(oldGame);

        for (Mannschaft man : games) {

            Mannschaft n = new Mannschaft();
            try {
                BeanUtils.copyProperties(n, man);
            } catch (IllegalAccessException | InvocationTargetException e) {
                log.error(e.getMessage(), e);
            }
            n.setId(null);
            n.setGame(newGame);
            n.setGruppe(null);
            n.setGruppe(null);
            n.setGruppeB(null);
            n.setTeamNummer(0);
            repo.save(n);
        }

    }

    public List<Mannschaft> getMannschaften(String jahr, String game) {
        return repo.findByGame(game);
    }

    public Mannschaft save(Mannschaft mannschaft) {
        Mannschaft ret = repo.save(mannschaft);
        website.dumpWebsite();
        return ret;
    }

}
