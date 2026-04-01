package ch.plaintext.schuetu.menu;

import ch.plaintext.boot.menu.MenuAnnotation;

/**
 * Menu-Eintrag fuer die Schiri-Freigabe (QR-Code basierte Registrierungen)
 *
 * @author info@emad.ch
 * @since 1.61.0
 */
@MenuAnnotation(
        title = "Schiri-Freigabe",
        link = "schiri-freigabe.htm",
        parent = "Experimental",
        order = 20,
        icon = "pi pi-qrcode",
        roles = {"admin"}
)
public class SchiriFreigabeMenu {
}
