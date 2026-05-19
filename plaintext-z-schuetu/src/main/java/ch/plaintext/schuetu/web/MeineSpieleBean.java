package ch.plaintext.schuetu.web;

import ch.plaintext.PlaintextSecurity;
import ch.plaintext.schuetu.entity.Schiri;
import ch.plaintext.schuetu.entity.Spiel;
import ch.plaintext.schuetu.repository.SchiriRepository;
import ch.plaintext.schuetu.repository.SpielRepository;
import ch.plaintext.schuetu.service.GameSelectionHolder;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Schiri-Dashboard: zeigt dem eingeloggten Schiri die naechsten eigenen Einsaetze.
 */
@Named
@ViewScoped
@Slf4j
public class MeineSpieleBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final long TWO_HOURS_MS = 2L * 60 * 60 * 1000;
    private static final long TEN_MIN_MS = 10L * 60 * 1000;
    private static final int MAX_EINSAETZE = 10;

    @Autowired
    private transient SchiriRepository schiriRepository;

    @Autowired
    private transient SpielRepository spielRepository;

    @Autowired
    private transient GameSelectionHolder holder;

    @Autowired
    private transient PlaintextSecurity plaintextSecurity;

    @Getter
    private Schiri eigenerSchiri;

    @PostConstruct
    public void init() {
        eigenerSchiri = ermittleEigenenSchiri();
    }

    Schiri ermittleEigenenSchiri() {
        if (holder == null || !holder.hasGame()) {
            return null;
        }
        String login;
        try {
            login = plaintextSecurity.getUser();
        } catch (Exception e) {
            log.debug("Konnte User nicht ermitteln: {}", e.getMessage());
            return null;
        }
        if (login == null || login.isEmpty()) {
            return null;
        }
        Optional<Schiri> match = schiriRepository.findByGame(holder.getGameName()).stream()
                .filter(s -> login.equals(s.getLoginName()))
                .findFirst();
        return match.orElse(null);
    }

    public boolean isSchiriBekannt() {
        return eigenerSchiri != null;
    }

    public String getSchiriVorname() {
        return eigenerSchiri == null ? "" : eigenerSchiri.getVorname();
    }

    public String getSchiriName() {
        return eigenerSchiri == null ? "" : eigenerSchiri.getShName();
    }

    /**
     * Eigene Spiele: alle Spiele wo eigeneSchiriId == spiel.schiri.id,
     * sortiert nach start aufsteigend, gefiltert auf start > now()-2h,
     * max {@value #MAX_EINSAETZE} Eintraege.
     */
    public List<Spiel> getNaechsteEinsaetze() {
        if (eigenerSchiri == null) {
            return Collections.emptyList();
        }
        Long meineId = eigenerSchiri.getId();
        Date grenze = new Date(System.currentTimeMillis() - TWO_HOURS_MS);
        return spielRepository.findByGame(holder.getGameName()).stream()
                .filter(s -> s.getSchiri() != null && meineId.equals(s.getSchiri().getId()))
                .filter(s -> s.getStart() != null && s.getStart().after(grenze))
                .sorted(Comparator.comparing(Spiel::getStart))
                .limit(MAX_EINSAETZE)
                .collect(Collectors.toList());
    }

    public String countdownText(Spiel s) {
        if (s == null) return "";
        if (s.isAmSpielen()) return "läuft";
        if (s.getStart() == null) return "";
        long diff = s.getStart().getTime() - System.currentTimeMillis();
        if (diff <= 0) {
            if (s.getFertiggespielt()) return "fertig";
            return "läuft gleich";
        }
        long minutes = diff / 60_000;
        long hours = minutes / 60;
        long rem = minutes % 60;
        if (hours > 0) return "in " + hours + "h " + rem + "m";
        return "in " + rem + "m";
    }

    public String statusBadge(Spiel s) {
        if (s == null) return "";
        if (s.isAmSpielen()) return "LIVE";
        if (s.getFertiggespielt()) return "FERTIG";
        if (s.getStart() == null) return "GEPLANT";
        long diff = s.getStart().getTime() - System.currentTimeMillis();
        if (diff <= TEN_MIN_MS && diff > 0) return "GLEICH";
        if (diff <= 0) return "ÜBERFÄLLIG";
        return "GEPLANT";
    }

    /**
     * Schon erledigte (fertiggespielte) Einsaetze heute.
     */
    public int getErledigteHeute() {
        return (int) getMeineHeute().stream().filter(Spiel::getFertiggespielt).count();
    }

    /**
     * Total geplante Einsaetze heute.
     */
    public int getTotalHeute() {
        return getMeineHeute().size();
    }

    private List<Spiel> getMeineHeute() {
        if (eigenerSchiri == null) {
            return Collections.emptyList();
        }
        Long meineId = eigenerSchiri.getId();
        Date now = new Date();
        Date startHeute = startOfDay(now);
        Date endeHeute = endOfDay(now);
        return spielRepository.findByGame(holder.getGameName()).stream()
                .filter(s -> s.getSchiri() != null && meineId.equals(s.getSchiri().getId()))
                .filter(s -> s.getStart() != null
                        && !s.getStart().before(startHeute)
                        && !s.getStart().after(endeHeute))
                .collect(Collectors.toList());
    }

    private Date startOfDay(Date d) {
        java.util.Calendar c = java.util.Calendar.getInstance();
        c.setTime(d);
        c.set(java.util.Calendar.HOUR_OF_DAY, 0);
        c.set(java.util.Calendar.MINUTE, 0);
        c.set(java.util.Calendar.SECOND, 0);
        c.set(java.util.Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    private Date endOfDay(Date d) {
        java.util.Calendar c = java.util.Calendar.getInstance();
        c.setTime(d);
        c.set(java.util.Calendar.HOUR_OF_DAY, 23);
        c.set(java.util.Calendar.MINUTE, 59);
        c.set(java.util.Calendar.SECOND, 59);
        c.set(java.util.Calendar.MILLISECOND, 999);
        return c.getTime();
    }

    /**
     * Liefert die Sekunden bis zum naechsten Einsatz (oder -1 falls keiner ansteht).
     * Wird im JSF fuer JS-Notification verwendet.
     */
    public long getSekundenBisNaechstesSpiel() {
        return getNaechsteEinsaetze().stream()
                .filter(s -> !s.isAmSpielen() && s.getStart() != null
                        && s.getStart().getTime() > System.currentTimeMillis())
                .findFirst()
                .map(s -> (s.getStart().getTime() - System.currentTimeMillis()) / 1000)
                .orElse(-1L);
    }
}
