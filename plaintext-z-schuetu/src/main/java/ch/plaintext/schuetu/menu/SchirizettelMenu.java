package ch.plaintext.schuetu.menu;

import ch.plaintext.boot.menu.MenuAnnotation;

/**
 * Programmatic Menu
 *
 * @author Author: info@emad.ch
 * @since 0.0.1
 */
@MenuAnnotation(
        title = "Schirizettel",
        link = "schirizettel.htm",
        parent = "Planung",
        order = 10,
        icon = "pi pi-twitter",
        roles = {"admin"}
)
public class SchirizettelMenu {
}
