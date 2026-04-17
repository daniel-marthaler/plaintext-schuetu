package ch.plaintext.schuetu.web;

import ch.plaintext.PlaintextSecurity;
import ch.plaintext.schuetu.entity.Spiel;
import ch.plaintext.schuetu.repository.SpielRepository;
import ch.plaintext.schuetu.service.GameSelectionHolder;
import ch.plaintext.schuetu.service.mqtt.MqttEventPublisher;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;

/**
 * Backing Bean fuer die Kontrolleur-Sicht.
 * Zeigt eingetragene Spiele zur Kontrolle und ermoeglicht
 * Bestaetigung oder Korrektur mit Bemerkung.
 */
@Controller
@Scope("session")
@Slf4j
@Data
public class KontrolleurBean {

    @Autowired
    private GameSelectionHolder holder;

    @Autowired
    private SpielRepository spielRepository;

    @Autowired
    private PlaintextSecurity plaintextSecurity;

    @Autowired
    private MqttEventPublisher mqttEventPublisher;

    // Korrektur-Formular
    private int korrekturToreA;
    private int korrekturToreB;
    private String korrekturBemerkung = "";

    /**
     * Gibt alle Spiele zurueck die eingetragen aber noch nicht bestaetigt sind.
     */
    public List<Spiel> getZuBestaetigendeSpiele() {
        if (!holder.hasGame()) {
            return new ArrayList<>();
        }
        return holder.getGame().getEintragen().findAllZuBestaetigen();
    }

    /**
     * Bestaetigt ein Spiel - uebernimmt die eingetragenen Tore als korrekt.
     */
    public void bestaetigen(Spiel spiel) {
        if (spiel == null) return;

        String user = getCurrentUser();
        spiel.setFertigBestaetigt(true);
        spiel.setToreABestaetigt(spiel.getToreA());
        spiel.setToreBBestaetigt(spiel.getToreB());
        spiel.setKontrolle(user);
        spielRepository.save(spiel);

        holder.getGame().getResultate().signalFertigesSpiel(spiel.getId());
        mqttEventPublisher.spielKontrolle(spiel);

        log.info("Spiel {} bestaetigt durch {}", spiel.getIdString(), user);
        addMessage("Spiel " + spiel.getIdString() + " bestaetigt.");
    }

    /**
     * Weist ein Spiel zurueck - setzt es in den Status "zurueckgewiesen".
     */
    public void zurueckweisen(Spiel spiel) {
        if (spiel == null) return;

        String user = getCurrentUser();
        spiel.setZurueckgewiesen(true);
        spiel.setFertigEingetragen(false);
        spiel.setKontrolle(user);
        if (!korrekturBemerkung.isBlank()) {
            String existing = spiel.getNotizen() != null ? spiel.getNotizen() : "";
            spiel.setNotizen(existing + (existing.isEmpty() ? "" : "\n") + "Zurueckgewiesen von " + user + ": " + korrekturBemerkung);
        }
        spielRepository.save(spiel);

        log.info("Spiel {} zurueckgewiesen durch {}: {}", spiel.getIdString(), user, korrekturBemerkung);
        addMessage("Spiel " + spiel.getIdString() + " zurueckgewiesen.");
        korrekturBemerkung = "";
    }

    /**
     * Korrigiert ein Spiel - ueberschreibt die Tore und bestaetigt.
     */
    public void korrigieren(Spiel spiel) {
        if (spiel == null) return;

        String user = getCurrentUser();
        spiel.setToreA(korrekturToreA);
        spiel.setToreB(korrekturToreB);
        spiel.setToreABestaetigt(korrekturToreA);
        spiel.setToreBBestaetigt(korrekturToreB);
        spiel.setFertigBestaetigt(true);
        spiel.setKontrolle(user);
        if (!korrekturBemerkung.isBlank()) {
            String existing = spiel.getNotizen() != null ? spiel.getNotizen() : "";
            spiel.setNotizen(existing + (existing.isEmpty() ? "" : "\n") + "Korrektur von " + user + ": " + korrekturBemerkung);
        }
        spielRepository.save(spiel);

        holder.getGame().getResultate().signalFertigesSpiel(spiel.getId());
        mqttEventPublisher.spielKorrektur(spiel);

        log.info("Spiel {} korrigiert durch {} auf {}:{} ({})", spiel.getIdString(), user, korrekturToreA, korrekturToreB, korrekturBemerkung);
        addMessage("Spiel " + spiel.getIdString() + " korrigiert auf " + korrekturToreA + ":" + korrekturToreB);
        korrekturToreA = 0;
        korrekturToreB = 0;
        korrekturBemerkung = "";
    }

    /**
     * Initialisiert das Korrektur-Formular mit den aktuellen Werten.
     */
    public void prepareKorrektur(Spiel spiel) {
        if (spiel != null) {
            korrekturToreA = spiel.getToreA();
            korrekturToreB = spiel.getToreB();
            korrekturBemerkung = "";
        }
    }

    public boolean isGameSelected() {
        return holder.hasGame();
    }

    private String getCurrentUser() {
        try {
            return plaintextSecurity.getUser();
        } catch (Exception e) {
            return "admin";
        }
    }

    private void addMessage(String msg) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, msg, null));
    }
}
