package ch.plaintext.schuetu.menu;

import ch.plaintext.boot.menu.MenuAnnotation;

/**
 * Programmatic Menu
 *
 * @author Author: info@emad.ch
 * @since 0.0.1
 */
@MenuAnnotation(
        title = "Rangliste",
        link = "rangliste.htm",
        parent = "",
        order = 100,
        icon = "pi pi-bars",
        roles = {"admin", "operator"}
)
public class RanglisteMenu {
}
