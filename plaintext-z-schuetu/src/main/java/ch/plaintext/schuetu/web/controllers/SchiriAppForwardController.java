package ch.plaintext.schuetu.web.controllers;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Serves the Speaker SPA index.html directly, bypassing the FacesServlet
 * which intercepts *.html requests.
 */
@RestController
public class SchiriAppForwardController {

    @GetMapping(value = {"/nosec/speaker-app", "/nosec/speaker-app/"},
                produces = MediaType.TEXT_HTML_VALUE)
    public String serveIndex() throws IOException {
        var resource = new ClassPathResource("static/nosec/speaker-app/index.html");
        return resource.getContentAsString(StandardCharsets.UTF_8);
    }
}
