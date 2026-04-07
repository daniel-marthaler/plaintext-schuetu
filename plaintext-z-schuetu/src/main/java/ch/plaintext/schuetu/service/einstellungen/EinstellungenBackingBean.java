package ch.plaintext.schuetu.service.einstellungen;

import ch.plaintext.schuetu.service.GameSelectionHolder;
import ch.plaintext.schuetu.entity.Mannschaft;
import ch.plaintext.schuetu.entity.Schiri;
import ch.plaintext.schuetu.entity.SpielZeile;
import ch.plaintext.schuetu.model.enums.PlatzEnum;
import ch.plaintext.schuetu.repository.MannschaftRepository;
import ch.plaintext.schuetu.repository.SchiriRepository;
import ch.plaintext.schuetu.repository.SpielZeilenRepository;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.Transient;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * IM nachhinein anpassen der Datum der Spieltage (fuer Samstag und Sonntag)
 */
@Component
@Scope("session")
@Data
@Slf4j
public class EinstellungenBackingBean {

    private static final Pattern UNICODE_ESCAPE_PATTERN = Pattern.compile("\\\\u([0-9a-fA-F]{4})");

    @Autowired
    private GameSelectionHolder game;

    private Date samstag;

    private Date sonntag;

    @Autowired
    private SpielZeilenRepository spielZeilenRepository;

    @Autowired
    private MannschaftRepository mannschaftRepository;

    @Autowired
    private SchiriRepository schiriRepository;

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

    public void fixUmlaute() {
        List<Mannschaft> mannschaften = mannschaftRepository.findByGame(game.getGameName());
        int count = 0;
        for (Mannschaft m : mannschaften) {
            boolean changed = false;
            for (Field field : Mannschaft.class.getDeclaredFields()) {
                if (field.getType() == String.class && !field.isAnnotationPresent(Transient.class)) {
                    field.setAccessible(true);
                    try {
                        String value = (String) field.get(m);
                        if (value != null && value.contains("\\u00")) {
                            String fixed = fixUnicodeEscapes(value);
                            if (!fixed.equals(value)) {
                                field.set(m, fixed);
                                changed = true;
                            }
                        }
                    } catch (IllegalAccessException e) {
                        log.error("Fehler beim Zugriff auf Feld {}: {}", field.getName(), e.getMessage());
                    }
                }
            }
            if (changed) {
                mannschaftRepository.save(m);
                count++;
            }
        }
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage("Umlaute bei " + count + " von " + mannschaften.size() + " Mannschaften korrigiert."));
    }

    public void fixSchiris() {
        List<Schiri> schiris = schiriRepository.findByGame(game.getGameName());
        int count = 0;
        for (Schiri s : schiris) {
            boolean changed = false;

            // Vorname/Nachname aus name splitten falls leer
            if (s.getName() != null && !s.getName().isBlank()) {
                if (s.getVorname() == null || s.getVorname().isBlank()) {
                    if (s.getName().contains(" ")) {
                        s.setVorname(s.getName().substring(0, s.getName().indexOf(' ')));
                        s.setNachname(s.getName().substring(s.getName().indexOf(' ') + 1));
                    } else {
                        s.setNachname(s.getName());
                    }
                    changed = true;
                }
            }

            // Unicode-Escapes fixen in allen String-Feldern
            for (Field field : Schiri.class.getDeclaredFields()) {
                if (field.getType() == String.class) {
                    field.setAccessible(true);
                    try {
                        String value = (String) field.get(s);
                        if (value != null && value.contains("\\u00")) {
                            String fixed = fixUnicodeEscapes(value);
                            if (!fixed.equals(value)) {
                                field.set(s, fixed);
                                changed = true;
                            }
                        }
                    } catch (IllegalAccessException e) {
                        log.error("Fehler beim Zugriff auf Feld {}: {}", field.getName(), e.getMessage());
                    }
                }
            }

            if (changed) {
                schiriRepository.save(s);
                count++;
            }
        }
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(count + " von " + schiris.size() + " Schiris korrigiert."));
    }

    private String fixUnicodeEscapes(String input) {
        Matcher matcher = UNICODE_ESCAPE_PATTERN.matcher(input);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            char c = (char) Integer.parseInt(matcher.group(1), 16);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(String.valueOf(c)));
        }
        matcher.appendTail(sb);
        return sb.toString();
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
