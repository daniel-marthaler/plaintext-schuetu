package ch.plaintext.schuetu.menu;

import ch.plaintext.boot.menu.MenuAnnotation;

/**
 * Menu entry for the modern Eintragen view under the Experimental menu.
 *
 * @author Author: info@emad.ch
 * @since 1.0.0
 */
@MenuAnnotation(
        title = "Eintragen Modern",
        link = "eintragen-modern.htm",
        parent = "Experimental",
        order = 20,
        icon = "pi pi-pencil",
        roles = {"admin", "eintragen"}
)
public class EintragenModernMenu {
}
