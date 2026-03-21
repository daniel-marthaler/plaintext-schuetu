package ch.plaintext.schuetu.menu;

import ch.plaintext.boot.menu.MenuAnnotation;

/**
 * Programmatic Menu
 *
 * @author Author: info@emad.ch
 * @since 0.0.1
 */
@MenuAnnotation(
        title = "Eintragen",
        link = "eintragen-liste.htm",
        parent = "",
        order = 20,
        icon = "pi pi-check-square",
        roles = {"admin", "eintragen"}
)
public class EintragenMenu {
}
