package ch.plaintext.schuetu.menu;

import ch.plaintext.boot.menu.MenuAnnotation;

/**
 * Programmatic Menu
 *
 * @author Author: info@emad.ch
 * @since 0.0.1
 */
@MenuAnnotation(
        title = "Export",
        link = "speaker.htm",
        parent = "",
        order = 30,
        icon = "pi pi-cloud-download",
        roles = {"admin", "exportierer"}
)
public class ExportierRootMenu {
}
