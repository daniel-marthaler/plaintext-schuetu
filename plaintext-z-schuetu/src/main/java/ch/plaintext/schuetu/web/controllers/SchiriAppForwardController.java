package ch.plaintext.schuetu.web.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Forwards all non-asset requests under /nosec/speaker-app/ to the SPA index.html.
 * Static assets (js, css, etc.) are served directly by Spring Boot's resource handling.
 */
@Controller
public class SchiriAppForwardController {

    @GetMapping("/nosec/speaker-app")
    public String forwardRoot() {
        return "forward:/nosec/speaker-app/index.html";
    }

    @GetMapping("/nosec/speaker-app/")
    public String forwardRootSlash() {
        return "forward:/nosec/speaker-app/index.html";
    }
}
