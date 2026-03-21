package ch.plaintext.schuetu.menu;

import ch.plaintext.boot.menu.MenuAnnotation;

/**
 * Programmatic Menu
 * NOTE: In the old code, this menu was only visible during SPIELEN or ABGESCHLOSSEN phase.
 * TODO: If phase-dependent visibility is needed, implement a custom menu visibility check.
 *
 * @author Author: info@emad.ch
 * @since 0.0.1
 */
@MenuAnnotation(
        title = "Speaker",
        link = "speaker.htm",
        parent = "",
        order = 50,
        icon = "pi pi-volume-up",
        roles = {"admin", "speaker"}
)
public class SpeakerRootMenu {
}
