package ch.plaintext.schuetu.menu;

import ch.plaintext.boot.menu.MenuAnnotation;

/**
 * Root menu entry for experimental features.
 *
 * @author Author: info@emad.ch
 * @since 1.0.0
 */
@MenuAnnotation(
        title = "Experimental",
        link = "matrix-modern.htm",
        parent = "",
        order = 90,
        icon = "pi pi-bolt",
        roles = {"admin"}
)
public class ExperimentalRootMenu {
}
