package ch.plaintext.schuetu.menu;

import ch.plaintext.boot.menu.MenuAnnotation;

/**
 * Programmatic Menu
 *
 * @author Author: info@emad.ch
 * @since 0.0.1
 */
@MenuAnnotation(
        title = "Matrix",
        link = "matrix.htm",
        parent = "Beobachter",
        order = 10,
        icon = "pi pi-list",
        roles = {"admin", "beobachter"}
)
public class MatrixMenu {
}
