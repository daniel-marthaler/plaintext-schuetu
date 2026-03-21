package ch.plaintext.schuetu.menu;

import ch.plaintext.boot.menu.MenuAnnotation;

/**
 * Programmatic Menu
 *
 * @author Author: info@emad.ch
 * @since 0.0.1
 */
@MenuAnnotation(
        title = "Schiri",
        link = "schiri-liste.htm",
        parent = "",
        order = 50,
        icon = "pi pi-android",
        roles = {"admin"}
)
public class SchirilisteMenu {
}
