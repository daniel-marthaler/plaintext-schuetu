package ch.plaintext.schuetu.web;

import ch.plaintext.schuetu.entity.Spiel;
import ch.plaintext.schuetu.repository.SpielRepository;
import ch.plaintext.schuetu.service.GameSelectionHolder;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Backing-Bean fuer die Live-Hallen-Display-Ansicht
 * (Vollbild, ohne Login, fuer TV/Beamer in der Halle).
 */
@Named
@ViewScoped
@Slf4j
public class LiveDisplayBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final int MAX_NAECHSTE_SPIELE = 4;

    @Autowired
    private transient SpielRepository spielRepository;

    @Autowired
    private transient GameSelectionHolder holder;

    @PostConstruct
    public void init() {
        log.debug("LiveDisplayBean initialisiert");
    }

    /**
     * Alle Spiele die gerade laufen (amSpielen=true) im aktuellen Game.
     */
    public List<Spiel> getSpielendeSpiele() {
        if (holder == null || !holder.hasGame()) {
            return Collections.emptyList();
        }
        return spielRepository.findByGame(holder.getGameName()).stream()
                .filter(Spiel::isAmSpielen)
                .sorted(Comparator.comparing(Spiel::getPlatz,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }

    /**
     * Die naechsten wartenden Spiele (Top {@value #MAX_NAECHSTE_SPIELE}),
     * sortiert nach Startzeit aufsteigend, nur Spiele mit start > now().
     */
    public List<Spiel> getNaechsteSpiele() {
        if (holder == null || !holder.hasGame()) {
            return Collections.emptyList();
        }
        Date now = new Date();
        return spielRepository.findByGame(holder.getGameName()).stream()
                .filter(s -> !s.isAmSpielen())
                .filter(s -> s.getStart() != null && s.getStart().after(now))
                .sorted(Comparator.comparing(Spiel::getStart))
                .limit(MAX_NAECHSTE_SPIELE)
                .collect(Collectors.toList());
    }

    /**
     * Aktuelle Uhrzeit im Format HH:mm.
     */
    public String getUhrzeit() {
        return new SimpleDateFormat("HH:mm").format(new Date());
    }

    /**
     * Maximale Anzahl angezeigter naechster Spiele.
     */
    public int getMaxNaechsteSpiele() {
        return MAX_NAECHSTE_SPIELE;
    }
}
