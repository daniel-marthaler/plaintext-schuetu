package ch.plaintext.schuetu.service;

import ch.plaintext.MenuVisibilityProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.Set;

/**
 * Primary MenuVisibilityProvider that adds game-selection visibility.
 * Menus are hidden unless a tournament is selected (except ALWAYS_VISIBLE).
 * Delegates to any other MenuVisibilityProvider beans for mandate checks.
 */
@Service
@Primary
@Slf4j
public class GameMenuVisibilityProvider implements MenuVisibilityProvider {

    @PostConstruct
    public void init() {
        log.info(">>> GameMenuVisibilityProvider initialized (@Primary) <<<");
    }

    private static final Set<String> ALWAYS_VISIBLE = Set.of(
            "Start", "Root", "Admin", "Home", "Experimental", "Sprache"
    );

    @Autowired
    @Lazy
    private GameSelectionHolder gameSelectionHolder;

    @Autowired
    private ApplicationContext applicationContext;

    private MenuVisibilityProvider delegateProvider;
    private boolean delegateLookedUp = false;

    private MenuVisibilityProvider getDelegate() {
        if (!delegateLookedUp) {
            delegateLookedUp = true;
            try {
                Map<String, MenuVisibilityProvider> providers = applicationContext.getBeansOfType(MenuVisibilityProvider.class);
                for (Map.Entry<String, MenuVisibilityProvider> entry : providers.entrySet()) {
                    if (entry.getValue() != this) {
                        delegateProvider = entry.getValue();
                        log.info("Found delegate MenuVisibilityProvider: {}", entry.getKey());
                        break;
                    }
                }
            } catch (Exception e) {
                log.debug("No delegate MenuVisibilityProvider found: {}", e.getMessage());
            }
        }
        return delegateProvider;
    }

    @Override
    public boolean isMenuVisible(String menuTitle) {
        // First check delegate (e.g. MandateMenuVisibilityService)
        MenuVisibilityProvider delegate = getDelegate();
        if (delegate != null) {
            try {
                if (!delegate.isMenuVisible(menuTitle)) {
                    return false;
                }
            } catch (Exception e) {
                log.debug("Delegate visibility check failed for '{}': {}", menuTitle, e.getMessage());
            }
        }

        // Extract base menu name (before " | " for child menus)
        String baseMenu = menuTitle.contains(" | ") ? menuTitle.split(" \\| ")[0].trim() : menuTitle.trim();

        if (ALWAYS_VISIBLE.contains(baseMenu)) {
            return true;
        }

        // All other menus require a game to be selected
        try {
            boolean hasGame = Boolean.TRUE.equals(gameSelectionHolder.hasGame());
            if (!hasGame) {
                log.info("Menu '{}' hidden - no game selected", menuTitle);
            }
            return hasGame;
        } catch (Exception e) {
            log.warn("Game check failed for '{}', showing: {}", menuTitle, e.getMessage());
            return true;
        }
    }

    @Override
    public boolean isMenuVisibleForMandate(String menuTitle, String mandate) {
        return isMenuVisible(menuTitle);
    }
}
