package ch.plaintext.schuetu.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Entwicklungs strategie
 */
@Component
@Profile("devl")
public class DevStrategy {

    public Boolean getLoadOldGames() {
        return Boolean.FALSE;
    }

}
