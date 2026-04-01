package ch.plaintext.schuetu.web;

import ch.plaintext.schuetu.entity.Spiel;
import ch.plaintext.schuetu.service.GameSelectionHolder;
import ch.plaintext.schuetu.service.mqtt.MqttEventPublisher;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Backing Bean fuer die moderne Eintragen-Ansicht (Card-basiert).
 *
 * @author info@emad.ch
 * @since 1.0.0
 */
@Controller
@Scope("session")
@Slf4j
@Data
public class EintragenModernBean {

    @Autowired
    private GameSelectionHolder holder;

    @Autowired
    private MqttEventPublisher mqttEventPublisher;

    /**
     * Map von SpielId -> ausgewaehlter Schiri-Name.
     * Wird pro Karte im UI gesetzt.
     */
    private Map<Long, String> schiriSelections = new HashMap<>();

    /**
     * Gibt alle einzutragenden Spiele zurueck.
     */
    public List<Spiel> getEinzutragende() {
        if (!holder.hasGame()) {
            return new ArrayList<>();
        }
        return holder.getGame().getEintragen().findAllEinzutragende();
    }

    /**
     * Traegt ein einzelnes Spiel ein und bestaetigt es direkt.
     * Der Schiri wird aus der schiriSelections Map gelesen.
     */
    public void eintragen(Spiel spiel) {
        if (!holder.hasGame() || spiel == null) return;

        String selectedSchiri = schiriSelections.getOrDefault(spiel.getId(), "");

        if (spiel.getToreA() < 0 || spiel.getToreB() < 0 || selectedSchiri.isEmpty()) {
            log.warn("Eintragen nicht moeglich: Tore oder Schiri fehlt fuer Spiel {}", spiel.getIdString());
            return;
        }

        List<Spiel> spiele = new ArrayList<>();
        spiele.add(spiel);

        holder.getGame().getEintragen().eintragen(spiele, "" + spiel.getId(), selectedSchiri);
        holder.getGame().getEintragen().bestaetigen(spiele, "" + spiel.getId(), "ok");
        mqttEventPublisher.spielEingetragen(spiel);

        // Aufraemen
        schiriSelections.remove(spiel.getId());
        log.info("Spiel {} eingetragen via Modern-UI, Schiri: {}", spiel.getIdString(), selectedSchiri);
    }
}
