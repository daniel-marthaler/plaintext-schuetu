package ch.plaintext.schuetu.menu;

import ch.plaintext.boot.menu.MenuAnnotation;

/**
 * Programmatic Menu
 *
 * @author Author: info@emad.ch
 * @since 0.0.1
 */
@MenuAnnotation(
        title = "Penaltykorrekturen",
        link = "penaltykorrekturen-liste.htm",
        parent = "",
        order = 80,
        icon = "pi pi-check-circle",
        roles = {"admin"}
)
public class PenaltykorrekturenMenu {
}
