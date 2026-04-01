package ch.plaintext.schuetu.menu;

import ch.plaintext.boot.menu.MenuAnnotation;

@MenuAnnotation(
        title = "Sprache",
        link = "sprache.htm",
        parent = "",
        order = 200,
        icon = "pi pi-globe",
        roles = {"user", "admin", "exportierer", "speaker", "eintragen", "beobachter", "planer", "kontrollierer", "eintrager"}
)
public class SpracheRootMenu {
}
