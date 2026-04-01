package ch.plaintext.schuetu.menu;

import ch.plaintext.boot.menu.MenuAnnotation;

/**
 * Menu entry for access statistics under the Experimental menu.
 * Admin-only access.
 *
 * @author info@emad.ch
 * @since 1.60.0
 */
@MenuAnnotation(
        title = "Statistik",
        link = "statistik.htm",
        parent = "Experimental",
        order = 20,
        icon = "pi pi-chart-bar",
        roles = {"admin"}
)
public class StatistikMenu {
}
