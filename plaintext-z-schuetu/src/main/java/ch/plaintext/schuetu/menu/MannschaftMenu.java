package ch.plaintext.schuetu.menu;

import ch.plaintext.boot.menu.MenuAnnotation;

/**
 * Programmatic Menu
 *
 * @author Author: info@emad.ch
 * @since 0.0.1
 */
@MenuAnnotation(
        title = "Mannschaften",
        link = "mannschaft-liste.htm",
        parent = "",
        order = 11,
        icon = "pi pi-users",
        roles = {"admin", "planer"}
)
public class MannschaftMenu {
}
