package ch.plaintext.schuetu.service.werbung;

import lombok.Data;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Stellt die Werbungsseite auf die Website
 *
 * TODO: Old dependencies (EmadIHTTP, Unirest, POI) removed.
 * Re-implement with RestTemplate/WebClient and modern POI if needed.
 */
@Component
@Scope("session")
@Data
public class WerbungBackingBean {

    // TODO: EmadIHTTP removed - use RestTemplate or WebClient
    // @Autowired private EmadIHTTP http;

    public void go() {
        System.out.println("*** Go: WerbungBackingBean - TODO: re-implement");
    }

    public String getLines(String breite) {
        // TODO: re-implement sponsor lines generation
        return "test";
    }

}
