package ch.plaintext.schuetu.web;

import ch.plaintext.schuetu.service.GameSelectionHolder;
import ch.plaintext.schuetu.entity.Kategorie;
import ch.plaintext.schuetu.entity.Mannschaft;
import ch.plaintext.schuetu.model.comperators.KategorieNameComperator;
import ch.plaintext.schuetu.model.enums.SpielTageszeit;
import ch.plaintext.schuetu.repository.KategorieRepository;
import ch.plaintext.schuetu.repository.MannschaftRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * @author Author: info@emad.ch
 * @since 10
 */
@Controller
@Scope("session")
@Slf4j
public class KategorieBackingBean {

    @Autowired
    private KategorieRepository repo;

    @Autowired
    private MannschaftRepository mannschaftRepo;

    @Autowired
    private GameSelectionHolder holder;

    public List<Kategorie> getKategorien() {
        List<Kategorie> ret = repo.findByGame(holder.getGame().getModel().getGameName());
        ret.get(0).setFirst(Boolean.TRUE);
        ret.get(ret.size() - 1).setLast(Boolean.TRUE);
        return ret;
    }

    public void spielwunschEintragen(String id) {
        log.info("spielwunschEintragen(String id)" + id);

        final Kategorie k = repo.findById(Long.parseLong(id)).get();
        final SpielTageszeit wunsch = k.getSpielwunsch();
        String spielwunsch = null;
        if (wunsch.equals(SpielTageszeit.EGAL)) {
            k.setSpielwunsch(SpielTageszeit.SAMSTAGMORGEN);
            spielwunsch = "morgen";
        } else if (wunsch.equals(SpielTageszeit.SAMSTAGMORGEN)) {
            k.setSpielwunsch(SpielTageszeit.SAMSTAGNACHMITTAG);
            spielwunsch = "nachmittag";
        } else if (wunsch.equals(SpielTageszeit.SAMSTAGNACHMITTAG)) {
            k.setSpielwunsch(SpielTageszeit.SONNTAGMORGEN);
            spielwunsch = "sonntag";
        }
        if (wunsch.equals(SpielTageszeit.SONNTAGMORGEN)) {
            k.setSpielwunsch(SpielTageszeit.EGAL);
            spielwunsch = "";
        }

        // nachfuehren der spielwunschhints auf den mannschaften der kategorie
        for (Mannschaft m : k.getMannschaften()) {
            m.setSpielWunschHint(spielwunsch);
            mannschaftRepo.save(m);
        }
        repo.save(k);
    }

    public void manuelleZuordnungDurchziehen(String mannschaftName, String zielKategorieKey) {

        List<Kategorie> katListe;

        if (mannschaftName.toLowerCase().contains("m")) {
            katListe = repo.getKategorienMList(holder.getGame().getModel().getGameName());
        } else {
            katListe = repo.getKategorienKList(holder.getGame().getModel().getGameName());
        }

        Kategorie quelle = null;
        Kategorie ziel = null;

        katListe.sort(new KategorieNameComperator());

        Mannschaft verschieben = null;

        for (int i = 0; i < katListe.size(); i++) {
            final List<Mannschaft> mannschaften = katListe.get(i).getMannschaften();
            for (final Mannschaft mannschaft : mannschaften) {
                if (mannschaft.getName().equals(mannschaftName)) {
                    verschieben = mannschaft;
                    quelle = katListe.get(i);

                    if (zielKategorieKey.equals("+")) {
                        ziel = katListe.get(i + 1);
                    } else {
                        ziel = katListe.get(i - 1);
                    }
                }
            }
        }

        if (quelle == null) {
            log.error("!!! bei mannschaftszuordnungs korrektur quelle nicht gefunden: " + mannschaftName);
        }

        quelle.getGruppeA().getMannschaften().remove(verschieben);
        if (verschieben != null) {
            verschieben.setGruppe(ziel.getGruppeA());
        }
        verschieben = this.mannschaftRepo.save(verschieben);
        if (ziel != null) {
            ziel.getGruppeA().getMannschaften().add(verschieben);
        }

        repo.save(quelle);
        repo.save(ziel);

    }

}
