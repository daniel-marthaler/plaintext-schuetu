package ch.plaintext.schuetu.service;

import ch.plaintext.MenuVisibilityProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@Slf4j
public class GameMenuVisibilityProvider implements MenuVisibilityProvider {

    private static final Set<String> ALWAYS_VISIBLE = Set.of("Start", "Root", "Admin", "Home");

    @Autowired
    private GameSelectionHolder gameSelectionHolder;

    @Override
    public boolean isMenuVisible(String menuTitle) {
        if (ALWAYS_VISIBLE.contains(menuTitle)) {
            return true;
        }
        try {
            boolean hasGame = Boolean.TRUE.equals(gameSelectionHolder.hasGame());
            if (!hasGame) {
                log.debug("Menu '{}' hidden - no game selected", menuTitle);
            }
            return hasGame;
        } catch (Exception e) {
            log.debug("Menu '{}' visibility check failed, showing menu: {}", menuTitle, e.getMessage());
            return true;
        }
    }

    @Override
    public boolean isMenuVisibleForMandate(String menuTitle, String mandate) {
        return isMenuVisible(menuTitle);
    }
}
