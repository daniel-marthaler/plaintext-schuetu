package ch.plaintext.schuetu.web.controllers;

import ch.plaintext.schuetu.service.mobile.MatchInfoService;
import ch.plaintext.schuetu.service.websiteinfo.WebsiteInfoService;
import ch.plaintext.schuetu.entity.Spiel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller fuer die Webseiteninformationen
 *
 * @author info@emad.ch
 * @since 1.2.8
 */
@Component
@Scope("session")
public class InfomonitorController {

    @Autowired
    private WebsiteInfoService service;

    @Autowired
    private MatchInfoService matchinfo;

    @Setter
    @Getter
    private String game;

    private Map<Long, String> bekanntList = new HashMap<>();

    List<Spiel> sp = new ArrayList<>();

    public List<Spiel> getFinalspiele() {
        List<Spiel> spiele = service.getFinalspiele(game);
        if (bekanntList.size() == 0) {
            for (Spiel spiel : spiele) {
                bekanntList.put(spiel.getId(), "Paarung bekannt am: " + matchinfo.evaluateFinalSpielPaarungBekannt(spiel.getKategorieName(), game));
            }
        }

        for (Spiel spiel : spiele) {
            spiel.setFinalspieleBekanntAm(bekanntList.get(spiel.getId()));
        }
        return spiele;
    }

    public String getGameCookie(HttpServletRequest request) {
        for (Cookie s : request.getCookies()) {
            if (s.getName().equals("SchuetuInfomonitorGame")) {
                return s.getValue();
            }
        }

        return "";
    }

}
