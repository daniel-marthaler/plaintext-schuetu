package ch.plaintext.schuetu.web;

import ch.plaintext.schuetu.service.GameSelectionHolder;
import ch.plaintext.schuetu.service.PenaltyLoaderFactory;
import ch.plaintext.schuetu.entity.Mannschaft;
import ch.plaintext.schuetu.entity.Penalty;
import ch.plaintext.schuetu.entity.Spiel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.event.ReorderEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;

/**
 * Schiri BackingBean
 *
 * @author info@emad.ch
 * @since 1.2.8
 */
@Controller
@Scope("session")
@Slf4j
@Data
public class EintragenBackingBean {

    @Autowired
    private GameSelectionHolder holder;

    @Autowired
    private PenaltyLoaderFactory penaltyLoader;

    private Spiel selected;

    private String selectedSchiri = "";

    private Penalty penalty;

    private String pen;

    public void penaltyEintragen() {
        if (!holder.hasGame()) return;
        Penalty temp = penaltyLoader.penaltyEingetragen("" + penalty.getId(), penalty.getReihenfolge());
        holder.getGame().getResultate().signalPenalty(temp);
        penalty = null;
    }

    public Penalty getPenalty() {
        if (this.penalty != null) {
            return penalty;
        }
        penalty = penaltyLoader.loadPenaltyGespielt(holder.getGameName());
        if (penalty == null) {
            return null;
        }
        pen = penalty.getReihenfolgeOrig();
        return penalty;
    }

    public void onRowReorder(ReorderEvent event) {
        List<Mannschaft> liste = penalty.getFinalList();
        Mannschaft von = penalty.getFinalList().get(event.getFromIndex());
        Mannschaft zu = penalty.getFinalList().get(event.getToIndex());

        liste.set(event.getFromIndex(), zu);
        liste.set(event.getToIndex(), von);

        penalty.setFinalList(liste);
    }

    public List<Spiel> getEinzutragende() {
        if (!holder.hasGame()) {
            return new ArrayList<>();
        }
        return holder.getGame().getEintragen().findAllEinzutragende();
    }

    public void eintragen() {
        if (!holder.hasGame() || selected == null) return;
        List<Spiel> spiele = new ArrayList<>();
        spiele.add(selected);
        holder.getGame().getEintragen().eintragen(spiele, "" + selected.getId(), selectedSchiri);
        holder.getGame().getEintragen().bestaetigen(spiele, "" + selected.getId(), "ok");
        selected = null;
        selectedSchiri = "";
    }

    public Boolean eintragOK() {

        if (selected != null && selected.getToreA() > -1 && selected.getToreB() > -1 && !selectedSchiri.isEmpty()) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }


}
