package ch.plaintext.schuetu.web.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Speichert das Game als Cookie und leitet an mobile.htm weiter
 *
 * @author info@emad.ch
 * @since 1.2.8
 */
@Component
@RequestMapping(value = "/nosec")
@Scope("session")
public class InfomonitorRedirector {

    @Autowired
    private InfomonitorController controller;

    @RequestMapping(value = "/infomonitorredirect/{game:.+}", method = RequestMethod.GET)
    public String getWebsiteinfo(@PathVariable("game") String game, HttpServletResponse response) {
        controller.setGame(game);
        setGameAsCookie(game, response);
        return "redirect:../info/infomonitor.htm";
    }

    public void setGameAsCookie(String game, HttpServletResponse response) {
        Cookie cookie = new Cookie("SchuetuInfomonitorGame", "" + game);
        cookie.setMaxAge(60 * 60 * 24 * 30 * 2);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

}
