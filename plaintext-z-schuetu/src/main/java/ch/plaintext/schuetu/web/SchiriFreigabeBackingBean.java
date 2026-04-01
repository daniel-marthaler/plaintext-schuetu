package ch.plaintext.schuetu.web;

import ch.plaintext.schuetu.service.qrcode.SchiriMobileService;
import ch.plaintext.schuetu.service.qrcode.SchiriMobileService.SchiriRegistration;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.io.Serializable;
import java.util.List;

/**
 * Backing Bean fuer die Schiri-Freigabe-Ansicht.
 * Zeigt alle registrierten Schiris an und ermoeglicht Freigabe/Ablehnung.
 *
 * @author info@emad.ch
 * @since 1.61.0
 */
@Controller
@Scope("session")
@Slf4j
@Data
public class SchiriFreigabeBackingBean implements Serializable {

    @Autowired
    private transient SchiriMobileService schiriMobileService;

    private String selectedToken;

    /**
     * Gibt alle registrierten Schiris zurueck (die sich via QR-Code angemeldet haben)
     */
    public List<SchiriRegistration> getRegistrierungen() {
        return schiriMobileService.getRegisteredSchiris();
    }

    /**
     * Gibt alle Tokens zurueck (auch nicht registrierte, fuer Uebersicht)
     */
    public List<SchiriRegistration> getAlleTokens() {
        return schiriMobileService.getAllRegistrations();
    }

    /**
     * Gibt einen Schiri frei
     */
    public void approve(String token) {
        schiriMobileService.approveSchiri(token);
        log.info("Schiri freigegeben: Token {}", token);
    }

    /**
     * Lehnt einen Schiri ab
     */
    public void reject(String token) {
        schiriMobileService.rejectSchiri(token);
        log.info("Schiri abgelehnt: Token {}", token);
    }

    /**
     * Gibt die Anzahl wartender Registrierungen zurueck
     */
    public long getWartendeAnzahl() {
        return schiriMobileService.getRegisteredSchiris().stream()
                .filter(r -> !r.isApproved())
                .count();
    }
}
