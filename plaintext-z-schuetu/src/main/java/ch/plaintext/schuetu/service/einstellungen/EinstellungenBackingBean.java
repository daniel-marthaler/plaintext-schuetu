package ch.plaintext.schuetu.service.einstellungen;

import ch.plaintext.schuetu.service.GameSelectionHolder;
import ch.plaintext.schuetu.entity.SpielZeile;
import ch.plaintext.schuetu.model.enums.PlatzEnum;
import ch.plaintext.schuetu.repository.SpielZeilenRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * IM nachhinein anpassen der Datum der Spieltage (fuer Samstag und Sonntag)
 */
@Component
@Scope("session")
@Data
public class EinstellungenBackingBean {

    @Autowired
    private GameSelectionHolder game;

    private Date samstag;

    private Date sonntag;

    @Autowired
    private SpielZeilenRepository spielZeilenRepository;

    private List<SpielZeile> spielZeilen = new ArrayList<>();

    @PostConstruct
    private void init() {

        spielZeilen.clear();

        spielZeilen.addAll(spielZeilenRepository.findGruppenSpielZeilen(game.getGameName()));
        spielZeilen.addAll(spielZeilenRepository.findFinalSpielZeilen(game.getGameName()));

        Date low = null;
        Date high = null;

        for (SpielZeile zeile : spielZeilen) {

            if (low == null) {
                low = zeile.getStart();
                high = zeile.getStart();
            } else {

                if (zeile.getStart().before(low)) {
                    low = zeile.getStart();
                }

                if (zeile.getStart().after(high)) {
                    high = zeile.getStart();
                }
            }
        }

        samstag = low;
        sonntag = high;
    }

    public void fixPlaetze() {

        for (SpielZeile zeile : spielZeilen) {

            if (zeile.getA() != null) {
                zeile.getA().setPlatz(PlatzEnum.A);
            }
            if (zeile.getB() != null) {
                zeile.getB().setPlatz(PlatzEnum.B);
            }
            if (zeile.getC() != null) {
                zeile.getC().setPlatz(PlatzEnum.C);
            }

            spielZeilenRepository.save(zeile);
        }

        init();
    }

    public void persistDate() {

        Calendar sam = Calendar.getInstance();
        sam.setTime(samstag);

        Calendar son = Calendar.getInstance();
        son.setTime(sonntag);

        for (SpielZeile zeile : spielZeilen) {
            zeile.getSpieltageszeit();
            Calendar temp = Calendar.getInstance();
            temp.setTime(zeile.getStart());

            if (zeile.isSonntag()) {
                temp.set(son.get(Calendar.YEAR), son.get(Calendar.MONTH), son.get(Calendar.DAY_OF_MONTH));
            } else {
                temp.set(sam.get(Calendar.YEAR), sam.get(Calendar.MONTH), sam.get(Calendar.DAY_OF_MONTH));
            }

            if (zeile.getA() != null) {
                zeile.getA().setStart(temp.getTime());
            }
            if (zeile.getB() != null) {
                zeile.getB().setStart(temp.getTime());
            }
            if (zeile.getC() != null) {
                zeile.getC().setStart(temp.getTime());
            }

            if (zeile.getD() != null) {
                zeile.getD().setStart(temp.getTime());
            }

            zeile.setStart(temp.getTime());

            spielZeilenRepository.save(zeile);
        }

        init();
    }
}
