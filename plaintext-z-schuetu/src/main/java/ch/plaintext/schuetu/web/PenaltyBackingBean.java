package ch.plaintext.schuetu.web;

import ch.plaintext.schuetu.service.GameSelectionHolder;
import ch.plaintext.schuetu.service.PenaltyLoaderFactory;
import ch.plaintext.schuetu.entity.Mannschaft;
import ch.plaintext.schuetu.entity.Penalty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.event.ReorderEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Spielzeilen manuell Vertauschen und Vertauschungen recorden
 *
 * @author info@emad.ch
 * @since 1.2.8
 */
@Component
@Scope("session")
@Data
@Slf4j
public class PenaltyBackingBean {

    @Autowired
    private PenaltyLoaderFactory loader;

    @Autowired
    private GameSelectionHolder gameHolder;

    private Penalty selected;

    public String save() {

        loader.save(selected);
        // neuberechnen
        gameHolder.getGame().getResultate().neuberechnenDerKategorie(selected.getFinalList().get(0).getKategorie(), gameHolder.getGameName());
        selected = null;
        return "penaltykorrekturen-liste.htm";

    }

    public void onRowReorder(ReorderEvent event) {
        List<Mannschaft> liste = selected.getFinalList();
        Mannschaft von = selected.getFinalList().get(event.getFromIndex());
        Mannschaft zu = selected.getFinalList().get(event.getToIndex());

        liste.set(event.getFromIndex(), zu);
        liste.set(event.getToIndex(), von);

        selected.setFinalList(liste);
    }

}
