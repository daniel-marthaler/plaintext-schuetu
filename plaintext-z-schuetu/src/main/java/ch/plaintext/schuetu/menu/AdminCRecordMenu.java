package ch.plaintext.schuetu.menu;

import ch.plaintext.boot.menu.MenuAnnotation;

/**
 * Programmatic Menu
 *
 * @author Author: info@emad.ch
 * @since 0.0.1
 */
@MenuAnnotation(
        title = "Admin-Crecord",
        link = "admin-crecord-liste.htm",
        parent = "Planung",
        order = 100,
        icon = "pi pi-refresh",
        roles = {"admin"}
)
public class AdminCRecordMenu {
}
