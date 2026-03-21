package ch.plaintext.schuetu.web;

import ch.plaintext.schuetu.service.GameSelectionHolder;
import ch.plaintext.schuetu.entity.GameModel;
import ch.plaintext.schuetu.entity.Schiri;
import ch.plaintext.schuetu.repository.GameRepository;
import ch.plaintext.schuetu.repository.SchiriRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Schiri BackingBean
 *
 * @author info@emad.ch
 * @since 1.2.8
 */
@Controller
@Scope("session")
@Slf4j
@Data
public class SchirisBackingBean implements Serializable {

    private Schiri selectedSchiri = null;

    private String einteilen = "";

    @Autowired
    private SchiriRepository schiriRepo;

    @Autowired
    private GameSelectionHolder holder;

    @Autowired
    private GameRepository gameRepository;

    private String copyJahr;

    public List<Schiri> getSchiris() {
        return schiriRepo.findByGame(holder.getGameName());
    }

    public List<Schiri> getAktiveSchiris() {
        return schiriRepo.findByGameAndAktiviert(holder.getGameName(), Boolean.TRUE);
    }

    public void setSchiris(List<Schiri> schiris) {
        schiriRepo.saveAll(schiris);
    }

    public String selectSchiri(String id) {
        if (id == null || id.equals("null")) {
            this.selectedSchiri = new Schiri();
            this.selectedSchiri.setGame(holder.getGameName());
            schiriRepo.save(this.selectedSchiri);
        } else {
            this.selectedSchiri = schiriRepo.findById(Long.parseLong(id)).get();
        }
        return "schiri-detail.htm";
    }

    public List<String> getOldJahreGames() {
        List<String> ret = new ArrayList<>();
        for (GameModel game : gameRepository.findAll()) {
            ret.add(game.getGameName());
        }
        return ret;
    }

    public void copy() {

        for (Schiri schiri : schiriRepo.findByGame(copyJahr)) {

            schiri.setId(null);
            schiri.setGame(holder.getGameName());

            schiriRepo.save(schiri);

        }

        copyJahr = "";
    }


    public String save() {
        selectedSchiri.setGame(holder.getGameName());
        selectedSchiri = schiriRepo.save(selectedSchiri);
        this.selectedSchiri = null;
        return "schiri-liste.htm";
    }

    public String einteilen() {

        for (Schiri schiri : getSchiris()) {

            if (schiri.getEinteilung() != null && schiri.getEinteilung().equals(this.einteilen)) {
                schiri.setAktiviert(true);
            } else {
                schiri.setAktiviert(false);
            }

            schiriRepo.save(schiri);

        }

        einteilen = "-";
        this.selectedSchiri = null;

        return "schiri-liste.htm";
    }

    public String delete() {
        schiriRepo.delete(selectedSchiri);
        this.selectedSchiri = null;
        return "schiri-liste.htm";
    }

    public List<String> completeText(String query) {
        return schiriRepo.findAllEinteilungen(query, holder.getGameName());
    }

}
