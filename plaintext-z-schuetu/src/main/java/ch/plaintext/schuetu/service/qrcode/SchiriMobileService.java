package ch.plaintext.schuetu.service.qrcode;

import ch.plaintext.schuetu.entity.Schiri;
import ch.plaintext.schuetu.entity.Spiel;
import ch.plaintext.schuetu.repository.SchiriRepository;
import ch.plaintext.schuetu.repository.SpielRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service fuer die Verwaltung von Schiri-Mobile-Registrierungen.
 * Schiris koennen sich per QR-Code auf dem Handy registrieren und
 * werden vom Admin freigegeben.
 */
@Service
@Slf4j
public class SchiriMobileService {

    @Autowired
    private SpielRepository spielRepository;

    @Autowired
    private SchiriRepository schiriRepository;

    private final Map<String, SchiriRegistration> registrations = new ConcurrentHashMap<>();

    /**
     * Erstellt einen neuen Token fuer einen Schirizettel (wird beim Generieren der Schirizettel aufgerufen)
     */
    public String createToken(Long spielId, String spielInfo) {
        String token = UUID.randomUUID().toString().substring(0, 8);
        SchiriRegistration reg = new SchiriRegistration();
        reg.setToken(token);
        reg.setSpielId(spielId);
        reg.setSpielInfo(spielInfo);
        registrations.put(token, reg);
        log.info("Neuer Schiri-Token erstellt: {} fuer Spiel {}", token, spielInfo);
        return token;
    }

    /**
     * Registriert einen Schiri-Namen fuer einen Token
     */
    public boolean registerSchiri(String token, String name) {
        return registerSchiri(token, name, null);
    }

    public boolean registerSchiri(String token, String name, String telefon) {
        SchiriRegistration reg = registrations.get(token);
        if (reg == null) {
            log.warn("Token nicht gefunden: {}", token);
            return false;
        }
        reg.setSchiriName(name);
        reg.setTelefon(telefon);
        reg.setRegistered(true);

        // Schiri in DB speichern und mit dem Spiel verknuepfen
        if (reg.getSpielId() != null) {
            Spiel spiel = spielRepository.findById(reg.getSpielId()).orElse(null);
            if (spiel != null) {
                Schiri schiri = new Schiri();
                schiri.setName(name);
                if (name != null && name.contains(" ")) {
                    schiri.setVorname(name.substring(0, name.indexOf(' ')));
                    schiri.setNachname(name.substring(name.indexOf(' ') + 1));
                } else {
                    schiri.setNachname(name);
                }
                schiri.setTelefon(telefon);
                schiri.setGame(spiel.getGame());
                schiri.setAktiviert(true);
                schiri.setMatchcount(1);
                schiri.setSpielIDs(spiel.getIdString());
                schiriRepository.save(schiri);
                reg.setSchiriId(schiri.getId());

                spiel.setSchiri(schiri);
                spiel.setSchiriName(name);
                spielRepository.save(spiel);
            }
        }

        log.info("Schiri registriert: {} (Tel: {}) fuer Token {}", name, telefon, token);
        return true;
    }

    /**
     * Gibt einen Schiri frei (Admin-Aktion)
     */
    public void approveSchiri(String token) {
        SchiriRegistration reg = registrations.get(token);
        if (reg != null) {
            reg.setApproved(true);
            log.info("Schiri freigegeben: {} (Token {})", reg.getSchiriName(), token);
        }
    }

    /**
     * Lehnt einen Schiri ab und entfernt die Registrierung (Admin-Aktion)
     */
    public void rejectSchiri(String token) {
        SchiriRegistration reg = registrations.get(token);
        if (reg != null) {
            log.info("Schiri abgelehnt: {} (Token {})", reg.getSchiriName(), token);
            reg.setSchiriName(null);
            reg.setRegistered(false);
            reg.setApproved(false);
        }
    }

    /**
     * Pruefen ob ein Schiri freigegeben ist
     */
    public boolean isApproved(String token) {
        SchiriRegistration reg = registrations.get(token);
        return reg != null && reg.isApproved();
    }

    /**
     * Gibt die Registrierung fuer einen Token zurueck
     */
    public SchiriRegistration getRegistration(String token) {
        return registrations.get(token);
    }

    /**
     * Gibt alle Registrierungen zurueck (fuer Admin-Ansicht)
     */
    public List<SchiriRegistration> getAllRegistrations() {
        return new ArrayList<>(registrations.values());
    }

    /**
     * Gibt alle registrierten (aber nicht zwingend freigegebenen) Schiris zurueck
     */
    public List<SchiriRegistration> getRegisteredSchiris() {
        return registrations.values().stream()
                .filter(SchiriRegistration::isRegistered)
                .toList();
    }

    /**
     * Gibt das Spiel fuer einen Token zurueck
     */
    public Spiel getSpielForToken(String token) {
        SchiriRegistration reg = registrations.get(token);
        if (reg == null || reg.getSpielId() == null) {
            return null;
        }
        return spielRepository.findById(reg.getSpielId()).orElse(null);
    }

    /**
     * Prueft ob das Spiel fuer diesen Token bereits eingetragen wurde
     */
    public boolean isSpielEingetragen(String token) {
        Spiel spiel = getSpielForToken(token);
        return spiel != null && spiel.isFertigEingetragen();
    }

    /**
     * Traegt das Ergebnis eines Spiels ein (Schiri-Aktion via Mobile).
     * Setzt toreA, toreB, fertigEingetragen=true und schiriName.
     */
    public boolean eintragenSpiel(String token, int toreA, int toreB) {
        SchiriRegistration reg = registrations.get(token);
        if (reg == null || reg.getSpielId() == null) {
            return false;
        }
        Spiel spiel = spielRepository.findById(reg.getSpielId()).orElse(null);
        if (spiel == null) {
            return false;
        }
        spiel.setToreA(toreA);
        spiel.setToreB(toreB);
        spiel.setFertigEingetragen(true);
        spiel.setEintrager(reg.getSchiriName());
        spiel.setSchiriName(reg.getSchiriName());
        spielRepository.save(spiel);
        log.info("Spiel {} via Mobile eingetragen: {}:{} von {} (Token {})",
                spiel.getIdString(), toreA, toreB, reg.getSchiriName(), token);
        return true;
    }

    /**
     * Bestaetigt das Ergebnis eines Spiels (Kontrollierer-Aktion).
     * Setzt fertigBestaetigt=true und uebernimmt die Tore als bestaetigte Tore.
     */
    public boolean bestaetigeSpiel(String token) {
        Spiel spiel = getSpielForToken(token);
        if (spiel == null || !spiel.isFertigEingetragen()) {
            return false;
        }
        spiel.setFertigBestaetigt(true);
        spiel.setToreABestaetigt(spiel.getToreA());
        spiel.setToreBBestaetigt(spiel.getToreB());
        spiel.setZurueckgewiesen(false);
        spielRepository.save(spiel);
        log.info("Spiel {} via Kontrollierer bestaetigt (Token {})", spiel.getIdString(), token);
        return true;
    }

    /**
     * Weist das Ergebnis eines Spiels zurueck (Kontrollierer-Aktion).
     * Setzt fertigEingetragen=false und zurueckgewiesen=true.
     */
    public boolean weiseSpielZurueck(String token) {
        Spiel spiel = getSpielForToken(token);
        if (spiel == null || !spiel.isFertigEingetragen()) {
            return false;
        }
        spiel.setZurueckgewiesen(true);
        spiel.setFertigEingetragen(false);
        spielRepository.save(spiel);
        log.info("Spiel {} via Kontrollierer zurueckgewiesen (Token {})", spiel.getIdString(), token);
        return true;
    }

    /**
     * Model-Klasse fuer eine Schiri-Registrierung
     */
    @Data
    public static class SchiriRegistration {
        private String token;
        private String schiriName;
        private String telefon;
        private Long schiriId;
        private boolean registered;
        private boolean approved;
        private Long spielId;
        private String spielInfo;
    }
}
