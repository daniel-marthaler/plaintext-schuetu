package ch.plaintext.schuetu.menu;

import ch.plaintext.boot.menu.MenuAnnotation;

/**
 * Programmatic Menu
 *
 * @author Author: info@emad.ch
 * @since 0.0.1
 */
@MenuAnnotation(
        title = "Planung",
        link = "dashboard.htm",
        parent = "",
        order = 20,
        icon = "pi pi-align-left",
        roles = {"admin", "planer"}
)
public class PlanungRootMenu {
}
