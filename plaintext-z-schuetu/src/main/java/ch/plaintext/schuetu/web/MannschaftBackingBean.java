package ch.plaintext.schuetu.web;

import ch.plaintext.schuetu.service.GameSelectionHolder;
import ch.plaintext.schuetu.service.MannschaftService;
import ch.plaintext.schuetu.entity.GameModel;
import ch.plaintext.schuetu.entity.Mannschaft;
import ch.plaintext.schuetu.repository.GameRepository;
import ch.plaintext.schuetu.repository.MannschaftRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * Schiri BackingBean
 *
 * @author info@emad.ch
 * @since 10
 */
@Controller
@Scope("session")
@Data
@Slf4j
public class MannschaftBackingBean {

    private static final long serialVersionUID = 1L;

    @Autowired
    private MannschaftRepository mannschaftRepo;

    @Autowired
    private GameSelectionHolder holder;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private MannschaftService service;

    private String selectedJahr = "";

    private Mannschaft selected = null;
    private Mannschaft selectedOl = new Mannschaft();

    private String selectOldId = "";

    private String copyJahr = "";

    private String copyMannschaftFrom = "";

    public List<String> getOldJahre() {
        return service.getJahre();
    }

    public List<String> getOldJahreGames() {
        List<String> ret = new ArrayList<>();
        for (GameModel game : gameRepository.findAll()) {
            ret.add(game.getGameName());
        }
        return ret;
    }

    @PostConstruct
    private void init() {
        if (service.getJahre() != null && !service.getJahre().isEmpty()) {
            selectedJahr = service.getJahre().get(0);
        }
    }

    public void copy() {
        service.copyFromOldJahr(copyJahr, holder.getGame().getModel().getGameName());
    }

    public void copyGame() {
        service.copyFromOldGame(copyJahr, holder.getGame().getModel().getGameName());
    }

    public void copyOld() {
        this.selected = selectedOl;
        selected.setTeamNummer(-1);
        selected.setId(null);
    }

    public String save() {
        selected.setGame(holder.getGame().getModel().getGameName());
        selected = service.save(selected);
        return "mannschaft-liste.htm";
    }

    public String neu() {
        selected = new Mannschaft();
        return "mannschaft-details.htm";
    }

    public String delete() {
        mannschaftRepo.delete(selected);
        selected = null;
        return "mannschaft-liste.htm";
    }

    public List<Mannschaft> getOldMannschaften() {
        return service.getMannschaften(copyMannschaftFrom, copyMannschaftFrom);
    }

    public void oldSelect() {
        for (Mannschaft mn : getOldMannschaften()) {
            if (mn.toString().equals(selectOldId)) {
                this.selectedOl = mn;
            }
        }
    }

    public Boolean kopierenEnabled() {

        if (selected.getId() != null && selected.getId() > 0) {
            return false;
        }

        return selected.getCaptainName().isEmpty() && selected.getFarbe().isEmpty() && selected.getBegleitpersonName().isEmpty();

    }

}
