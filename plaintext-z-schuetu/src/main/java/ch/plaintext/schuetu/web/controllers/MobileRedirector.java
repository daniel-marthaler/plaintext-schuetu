package ch.plaintext.schuetu.web.controllers;

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
public class MobileRedirector {

    @RequestMapping(value = "/mobileredirect/{game:.+}", method = RequestMethod.GET)
    public String getWebsiteinfo(@PathVariable("game") String game, HttpServletResponse response) {
        setGameAsCookie(game, response);
        return "redirect:../mobile.htm";
    }

    public void setGameAsCookie(String game, HttpServletResponse response) {
        Cookie cookie = new Cookie("SchuetuMobileGame", "" + game);
        cookie.setMaxAge(60 * 60 * 24 * 30 * 2);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

}
