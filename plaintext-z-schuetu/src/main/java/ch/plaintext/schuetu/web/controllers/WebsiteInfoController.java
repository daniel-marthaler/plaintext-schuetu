package ch.plaintext.schuetu.web.controllers;

import ch.plaintext.schuetu.service.Game;
import ch.plaintext.schuetu.service.GameRoot;
import ch.plaintext.schuetu.service.websiteinfo.WebsiteInfoService;
import ch.plaintext.schuetu.service.websiteinfo.model.TeamGruppen;
import ch.plaintext.schuetu.entity.GameModel;
import ch.plaintext.schuetu.model.enums.SpielPhasenEnum;
import ch.plaintext.schuetu.model.ranglistensortierung.RanglisteneintragHistorie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Controller fuer die Webseiteninformationen
 *
 * @author info@emad.ch
 * @since 1.2.8
 */
@Component
@RequestMapping(value = "/nosec")
public class WebsiteInfoController {

    @Autowired
    private WebsiteInfoService service;

    @Autowired
    private GameRoot root;

    @RequestMapping(value = "/info/{jahr:.+}", method = RequestMethod.GET)
    public String getWebsiteinfo(@PathVariable("jahr") String jahr, Model model, HttpServletRequest request) {

        GameModel gameModel = null;

        for (GameModel mdl : root.displayGames()) {
            if (mdl.getGameName().equals(jahr)) {
                gameModel = mdl;
            }
        }

        boolean ganzeListe;
        boolean anmeldung;

        if (gameModel != null) {
            ganzeListe = gameModel.isWebsiteInMannschaftslistenmode();
            anmeldung = gameModel.getPhase() == SpielPhasenEnum.A_ANMELDEPHASE;
        } else {
            ganzeListe = true;
            anmeldung = false;
            gameModel = new GameModel();
        }

        boolean liste = ganzeListe || anmeldung;

        List<TeamGruppen> maedchen = service.getMaedchenMannschaften(jahr, liste);
        List<TeamGruppen> knaben = service.getKnabenMannschaften(jahr, liste);


        model.addAttribute("maedchen", maedchen);
        model.addAttribute("knaben", knaben);

        model.addAttribute("gruppenspiele", service.getGruppenspiele(jahr));
        model.addAttribute("finalspiele", service.getFinalspiele(jahr));

        model.addAttribute("rangliste", service.getRangliste(jahr));

        if (service.getEinstellungen(jahr) != null) {
            model.addAttribute("einstellungen", service.getEinstellungen(jahr));
        } else {
            model.addAttribute("einstellungen", gameModel);
        }

        return "info.htm";

    }

    @RequestMapping(value = "/tabelle/{jahr:.+}", method = RequestMethod.GET)
    public String getWebsiteinfo(@PathVariable("jahr") String jahr, @RequestParam("kategorie") String kategorie, Model model) {

        Game game = root.selectGame(jahr);
        RanglisteneintragHistorie historie = game.getResultate().getHistorie(kategorie);

        model.addAttribute("historie", historie);

        return "tabelle.htm";

    }

}
