package ch.plaintext.schuetu.service;

import ch.plaintext.MenuVisibilityProvider;
import ch.plaintext.boot.menu.MenuItemImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * After ALL beans are initialized, inject our GameMenuVisibilityProvider
 * into every MenuItemImpl. This ensures the provider is set before any
 * menu rendering happens.
 */
@Component
@Slf4j
public class MenuVisibilityInitializer implements SmartInitializingSingleton {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private GameMenuVisibilityProvider gameMenuVisibilityProvider;

    @Override
    public void afterSingletonsInstantiated() {
        Map<String, MenuItemImpl> menuItems = applicationContext.getBeansOfType(MenuItemImpl.class);
        int count = 0;
        for (MenuItemImpl item : menuItems.values()) {
            item.setMenuVisibilityProvider(gameMenuVisibilityProvider);
            count++;
        }
        log.info(">>> Injected GameMenuVisibilityProvider into {} menu items <<<", count);
    }
}
