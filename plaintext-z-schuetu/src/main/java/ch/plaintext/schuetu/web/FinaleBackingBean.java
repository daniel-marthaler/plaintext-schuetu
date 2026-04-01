package ch.plaintext.schuetu.web;

import ch.plaintext.PlaintextSecurity;
import ch.plaintext.schuetu.service.GameSelectionHolder;
import ch.plaintext.schuetu.entity.Kategorie;
import ch.plaintext.schuetu.repository.KategorieRepository;
import ch.plaintext.schuetu.repository.MannschaftRepository;
import ch.plaintext.schuetu.repository.SpielRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jakarta.faces.context.FacesContext;
import java.io.IOException;
import java.util.List;

/**
 * Korrigiert Finalzuteilungen der Gruppe von Hand !
 *
 * @author info@emad.ch
 * @since 1.2.8
 */
@Component
@Scope("session")
@Slf4j
@Data
public class FinaleBackingBean {

    @Autowired
    private PlaintextSecurity plaintextSecurity;

    @Autowired
    private KategorieRepository repo;

    @Autowired
    private SpielRepository repoSpiel;

    @Autowired
    private MannschaftRepository repoMannschaft;

    @Autowired
    private GameSelectionHolder gameHolder;

    private Kategorie selected;

    private String klA = "";
    private String klB = "";

    private String grA = "";
    private String grB = "";

    private String grA2 = "";
    private String grB2 = "";

    public void save() {

        selected = repo.save(selected);

        if (!klA.isEmpty()) {
            selected.getKleineFinal().setMannschaftA(repoMannschaft.findById(Long.parseLong(klA)).get());
            selected.getKleineFinal().setMannschaftAId(Integer.parseInt(klA));
            repoSpiel.save(selected.getKleineFinal());
        }

        if (!klB.isEmpty()) {
            selected.getKleineFinal().setMannschaftB(repoMannschaft.findById(Long.parseLong(klB)).get());
            selected.getKleineFinal().setMannschaftBId(Integer.parseInt(klB));
            repoSpiel.save(selected.getKleineFinal());
        }

        if (!grA.isEmpty()) {
            selected.getGrosserFinal().setMannschaftA(repoMannschaft.findById(Long.parseLong(grA)).get());
            selected.getGrosserFinal().setMannschaftAId(Integer.parseInt(grA));
            repoSpiel.save(selected.getGrosserFinal());
        }

        if (!grB.isEmpty()) {
            selected.getGrosserFinal().setMannschaftB(repoMannschaft.findById(Long.parseLong(grB)).get());
            selected.getGrosserFinal().setMannschaftBId(Integer.parseInt(grB));
            repoSpiel.save(selected.getGrosserFinal());
        }

        if (!grA2.isEmpty()) {
            selected.getGrosserfinal2().setMannschaftA(repoMannschaft.findById(Long.parseLong(grA2)).get());
            selected.getGrosserfinal2().setMannschaftAId(Integer.parseInt(grA2));
            repoSpiel.save(selected.getGrosserfinal2());
        }

        if (!grB2.isEmpty()) {
            selected.getGrosserfinal2().setMannschaftB(repoMannschaft.findById(Long.parseLong(grB2)).get());
            selected.getGrosserfinal2().setMannschaftBId(Integer.parseInt(grB2));
            repoSpiel.save(selected.getGrosserfinal2());
        }

        selected = repo.findById(selected.getId()).get();
        selected.setEintrager(plaintextSecurity.getUser());
        repo.save(selected);

        // neuberechnen
        if (gameHolder.hasGame()) {
            gameHolder.getGame().getResultate().neuberechnenDerKategorie(selected, gameHolder.getGameName());
        }
        selected = null;

        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect("finalekorrekturen-liste.htm");
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }

    }

    public void select() {

        klA = "";
        grA = "";

        klB = "";
        grB = "";

        grA2 = "";
        grB2 = "";

        if (selected.getKleineFinal() != null) {
            try {
                klA = "" + selected.getKleineFinal().getMannschaftA().getId();
                klB = "" + selected.getKleineFinal().getMannschaftB().getId();
            } catch (Exception e) {
                // noop
            }
        }

        if (selected.getGrosserFinal() != null) {
            try {
                grA = "" + selected.getGrosserFinal().getMannschaftA().getId();
                grB = "" + selected.getGrosserFinal().getMannschaftB().getId();
            } catch (Exception e) {
                // noop
            }
        }

        if (selected.getGrosserfinal2() != null) {
            try {
                grA2 = "" + selected.getGrosserfinal2().getMannschaftA().getId();
                grB2 = "" + selected.getGrosserfinal2().getMannschaftB().getId();
            } catch (Exception e) {
                // noop
            }
        }

        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect("finalekorrekturen-details.htm");
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    public List<Kategorie> getKategorien() {
        return repo.findByGame(gameHolder.getGameName());
    }

}
