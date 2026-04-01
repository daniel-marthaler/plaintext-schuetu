package ch.plaintext.schuetu.menu;

import ch.plaintext.boot.menu.MenuAnnotation;

/**
 * Menu entry for the modern matrix view under the Experimental menu.
 *
 * @author Author: info@emad.ch
 * @since 1.0.0
 */
@MenuAnnotation(
        title = "Matrix Modern",
        link = "matrix-modern.htm",
        parent = "Experimental",
        order = 10,
        icon = "pi pi-th-large",
        roles = {"admin"}
)
public class MatrixModernMenu {
}
