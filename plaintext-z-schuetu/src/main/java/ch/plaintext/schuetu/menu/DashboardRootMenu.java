package ch.plaintext.schuetu.menu;

import ch.plaintext.boot.menu.MenuAnnotation;

/**
 * Programmatic Menu
 *
 * @author Author: info@emad.ch
 * @since 0.0.1
 */
@MenuAnnotation(
        title = "Start",
        link = "dashboard.htm",
        parent = "",
        order = 0,
        icon = "pi pi-arrow-right",
        roles = {"user", "exportierer", "admin"}
)
public class DashboardRootMenu {
}
