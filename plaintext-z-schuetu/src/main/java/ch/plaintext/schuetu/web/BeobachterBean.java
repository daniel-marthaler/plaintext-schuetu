package ch.plaintext.schuetu.web;

import ch.plaintext.schuetu.service.GameSelectionHolder;
import ch.plaintext.schuetu.service.html.HTMLSchiriConverter;
import ch.plaintext.schuetu.entity.Kategorie;
import ch.plaintext.schuetu.entity.Spiel;
import ch.plaintext.schuetu.repository.KategorieRepository;
import ch.plaintext.schuetu.repository.SpielRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author $Author: info@emad.ch $
 * @since 0.7
 */
@Component
@Scope("session")
@Slf4j
public class BeobachterBean {

    private String auswahl = "";

    private String schiriAuswahl = "";

    @Autowired
    private GameSelectionHolder holder;

    @Autowired
    private KategorieRepository repo;

    @Autowired
    private SpielRepository spielRepo;

    @Autowired
    private HTMLSchiriConverter schiri;

    public List<String> getKategorien() {
        if (!holder.hasGame()) return new ArrayList<>();

        Set<String> result = new HashSet<>();
        for (Kategorie kat : repo.findByGame(holder.getGameName())) {
            if (kat.getGruppeA() != null) {
                result.add(kat.getName());
            } else {
                log.warn("achtung, kategorie ohne gruppe a: " + kat);
            }
        }

        result.addAll(this.holder.getGame().getResultate().getKeys());

        List<String> list = new ArrayList<>();
        list.addAll(result);
        Collections.sort(list);

        return list;
    }

    public String getMatrix() {
        if (!holder.hasGame()) return "";
        return this.holder.getGame().getResultate().generateSpieleMatrix();
    }

    public String getHistorie() {
        if (!holder.hasGame()) return "";
        return this.holder.getGame().getResultate().generateRanglistenHistorieForKategorieName(this.auswahl);
    }

    public String getSchiriZettel() {

        if (schiriAuswahl.contains("finale")) {

            List<Spiel> finale = this.spielRepo.findFinalSpielAsc(holder.getGameName());
            if (finale != null && finale.size() > 0) {
                return this.schiri.getTable(finale);
            }
            return "keine Finale";

        }

        return this.schiri.getTable(this.spielRepo.findGruppenSpielAsc(holder.getGameName()));

    }

    public String getAuswahl() {
        return auswahl;
    }

    public void setAuswahl(String auswahl) {
        this.auswahl = auswahl;
    }

    public String getSchiriAuswahl() {
        return schiriAuswahl;
    }

    public void setSchiriAuswahl(String schiriAuswahl) {
        this.schiriAuswahl = schiriAuswahl;
    }

    public void neuBerechnen() {
        if (!holder.hasGame()) return;
        holder.getGame().recalculateResultate();
    }
}
