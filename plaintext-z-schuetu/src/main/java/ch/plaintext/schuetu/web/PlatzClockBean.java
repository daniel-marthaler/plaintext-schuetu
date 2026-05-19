package ch.plaintext.schuetu.web;

import ch.plaintext.schuetu.entity.Spiel;
import ch.plaintext.schuetu.model.enums.PlatzEnum;
import ch.plaintext.schuetu.repository.SpielRepository;
import ch.plaintext.schuetu.service.GameSelectionHolder;
import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.Map;

/**
 * Match-Clock pro Platz: zeigt das aktuelle Spiel des Platzes
 * mit grossem Score und +/- Buttons.
 */
@Named
@ViewScoped
@Slf4j
public class PlatzClockBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final int MAX_TORE = 30;

    @Autowired
    private transient SpielRepository spielRepository;

    @Autowired
    private transient GameSelectionHolder holder;

    @Getter @Setter
    private PlatzEnum platz;

    @PostConstruct
    public void init() {
        String param = readParam("platz");
        platz = PlatzEnum.fromString(param);
        if (platz == null) {
            log.debug("PlatzClockBean ohne gueltigen platz-Parameter (war: {})", param);
        }
    }

    String readParam(String name) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        if (ctx == null) return null;
        Map<String, String> params = ctx.getExternalContext().getRequestParameterMap();
        return params == null ? null : params.get(name);
    }

    /**
     * Aktuelles Spiel auf diesem Platz (amSpielen=true).
     */
    public Spiel getAktuellesSpiel() {
        if (platz == null || holder == null || !holder.hasGame()) {
            return null;
        }
        return spielRepository.findByGame(holder.getGameName()).stream()
                .filter(Spiel::isAmSpielen)
                .filter(s -> platz.equals(s.getPlatz()))
                .findFirst()
                .orElse(null);
    }

    public String getPlatzName() {
        return platz == null ? "?" : platz.getText();
    }

    public String getPhaseText() {
        Spiel s = getAktuellesSpiel();
        if (s == null) return "Warte auf Spielstart";
        if (s.isAmSpielen()) return "Läuft";
        return "Pause";
    }

    public void incTeamA() {
        Spiel s = getAktuellesSpiel();
        if (s == null) return;
        int v = Math.max(s.getToreA(), 0) + 1;
        if (v > MAX_TORE) v = MAX_TORE;
        s.setToreA(v);
        spielRepository.save(s);
    }

    public void decTeamA() {
        Spiel s = getAktuellesSpiel();
        if (s == null) return;
        int v = Math.max(s.getToreA(), 0) - 1;
        if (v < 0) v = 0;
        s.setToreA(v);
        spielRepository.save(s);
    }

    public void incTeamB() {
        Spiel s = getAktuellesSpiel();
        if (s == null) return;
        int v = Math.max(s.getToreB(), 0) + 1;
        if (v > MAX_TORE) v = MAX_TORE;
        s.setToreB(v);
        spielRepository.save(s);
    }

    public void decTeamB() {
        Spiel s = getAktuellesSpiel();
        if (s == null) return;
        int v = Math.max(s.getToreB(), 0) - 1;
        if (v < 0) v = 0;
        s.setToreB(v);
        spielRepository.save(s);
    }

    public void beenden() {
        Spiel s = getAktuellesSpiel();
        if (s == null) return;
        s.setAmSpielen(false);
        s.setFertigGespielt(true);
        spielRepository.save(s);
    }

    public int getDisplayToreA() {
        Spiel s = getAktuellesSpiel();
        if (s == null) return 0;
        return Math.max(s.getToreA(), 0);
    }

    public int getDisplayToreB() {
        Spiel s = getAktuellesSpiel();
        if (s == null) return 0;
        return Math.max(s.getToreB(), 0);
    }
}
