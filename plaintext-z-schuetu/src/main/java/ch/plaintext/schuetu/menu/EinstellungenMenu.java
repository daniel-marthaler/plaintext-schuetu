package ch.plaintext.schuetu.menu;

import ch.plaintext.boot.menu.MenuAnnotation;

/**
 * Programmatic Menu
 *
 * @author Author: info@emad.ch
 * @since 0.0.1
 */
@MenuAnnotation(
        title = "Einstellungen",
        link = "einstellungen.htm",
        parent = "",
        order = 12,
        icon = "pi pi-briefcase",
        roles = {"admin"}
)
public class EinstellungenMenu {
}
