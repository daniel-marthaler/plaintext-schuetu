package ch.plaintext.schuetu.web.controllers;

import ch.plaintext.schuetu.entity.Spiel;
import ch.plaintext.schuetu.repository.SpielRepository;
import ch.plaintext.schuetu.service.qrcode.SchiriMobileService;
import ch.plaintext.schuetu.service.qrcode.SchiriMobileService.SchiriRegistration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;

/**
 * Controller fuer die mobile Schiri-Authentifizierung via QR-Code.
 * Alle Endpoints unter /nosec/ sind oeffentlich zugaenglich (kein Login noetig).
 */
@Controller
@RequestMapping("/nosec/schiri-mobile")
@Slf4j
public class SchiriMobileController {

    @Autowired
    private SchiriMobileService schiriMobileService;

    @Autowired
    private SpielRepository spielRepository;

    /**
     * Zeigt die mobile Schiri-Seite an.
     * Je nach Status: Registrierungsformular, Wartestatus, Spieldetails oder Kontrollierer-Ansicht.
     *
     * Wenn das Spiel bereits eingetragen ist (fertigEingetragen=true), wird die
     * Kontrollierer-Ansicht angezeigt, damit ein zweiter Scan das Ergebnis bestaetigen kann.
     */
    @GetMapping("/{token}")
    public String showMobilePage(@PathVariable String token, HttpServletRequest request, Model model) {

        SchiriRegistration reg = schiriMobileService.getRegistration(token);
        if (reg == null) {
            model.addAttribute("error", "Ungueltiger QR-Code / Token");
            model.addAttribute("status", "error");
            return "forward:/nosec/schiri-mobile.xhtml";
        }

        // Kontrollierer-Workflow: Wenn das Spiel bereits eingetragen ist,
        // zeige die Kontrollierer-Ansicht statt der Schiri-Ansicht
        if (schiriMobileService.isSpielEingetragen(token)) {
            Spiel spiel = schiriMobileService.getSpielForToken(token);
            if (spiel != null) {
                if (spiel.isFertigBestaetigt()) {
                    model.addAttribute("status", "kontrolle_done");
                } else {
                    model.addAttribute("status", "kontrolle");
                }
                addSpielDetailsForKontrolle(model, spiel);
            }
            model.addAttribute("token", token);
            model.addAttribute("spielInfo", reg.getSpielInfo());
            return "forward:/nosec/schiri-mobile.xhtml";
        }

        // Check if already registered via cookie
        String cookieToken = getSchiriTokenFromCookie(request);
        if (cookieToken != null && cookieToken.equals(token) && reg.isRegistered()) {
            // Already registered
            if (reg.isApproved()) {
                model.addAttribute("status", "approved");
                addSpielDetails(model, reg);
            } else {
                model.addAttribute("status", "waiting");
            }
            model.addAttribute("schiriName", reg.getSchiriName());
        } else if (reg.isRegistered()) {
            // Someone else registered with this token already
            model.addAttribute("status", "already_taken");
        } else {
            model.addAttribute("status", "register");
        }

        model.addAttribute("token", token);
        model.addAttribute("spielInfo", reg.getSpielInfo());
        return "forward:/nosec/schiri-mobile.xhtml";
    }

    /**
     * Registriert einen Schiri-Namen fuer den Token
     */
    @PostMapping("/{token}/register")
    public String registerSchiri(@PathVariable String token,
                                 @RequestParam String name,
                                 HttpServletResponse response,
                                 Model model) {

        boolean success = schiriMobileService.registerSchiri(token, name);

        if (success) {
            // Set cookie so the user stays "logged in"
            Cookie cookie = new Cookie("SchiriToken", token);
            cookie.setMaxAge(60 * 60 * 24); // 24 Stunden
            cookie.setPath("/");
            response.addCookie(cookie);

            model.addAttribute("status", "waiting");
            model.addAttribute("schiriName", name);
            model.addAttribute("token", token);

            SchiriRegistration reg = schiriMobileService.getRegistration(token);
            if (reg != null) {
                model.addAttribute("spielInfo", reg.getSpielInfo());
            }
        } else {
            model.addAttribute("status", "error");
            model.addAttribute("error", "Registrierung fehlgeschlagen. Token ungueltig.");
            model.addAttribute("token", token);
        }

        return "forward:/nosec/schiri-mobile.xhtml";
    }

    /**
     * AJAX-Endpoint: Gibt den aktuellen Status zurueck (polling)
     */
    @GetMapping("/{token}/status")
    @ResponseBody
    public String getStatus(@PathVariable String token) {
        SchiriRegistration reg = schiriMobileService.getRegistration(token);
        if (reg == null) {
            return "{\"status\":\"error\"}";
        }
        if (reg.isApproved()) {
            String spielInfo = reg.getSpielInfo() != null ? reg.getSpielInfo() : "";
            return "{\"status\":\"approved\",\"spielInfo\":\"" + spielInfo.replace("\"", "\\\"") + "\"}";
        }
        if (reg.isRegistered()) {
            return "{\"status\":\"waiting\"}";
        }
        return "{\"status\":\"register\"}";
    }

    /**
     * POST Kontrolle: Bestaetigt oder weist das Ergebnis zurueck.
     * Parameter 'aktion' = "bestaetigen" oder "zurueckweisen"
     */
    @PostMapping("/{token}/kontrolle")
    public String kontrolle(@PathVariable String token,
                             @RequestParam String aktion,
                             Model model) {

        if ("bestaetigen".equals(aktion)) {
            boolean success = schiriMobileService.bestaetigeSpiel(token);
            if (success) {
                model.addAttribute("status", "kontrolle_done");
                model.addAttribute("kontrolleMessage", "Ergebnis bestaetigt!");
            } else {
                model.addAttribute("status", "error");
                model.addAttribute("error", "Bestaetigung fehlgeschlagen.");
            }
        } else if ("zurueckweisen".equals(aktion)) {
            boolean success = schiriMobileService.weiseSpielZurueck(token);
            if (success) {
                model.addAttribute("status", "kontrolle_rejected");
                model.addAttribute("kontrolleMessage", "Ergebnis zurueckgewiesen. Das Spiel muss neu eingetragen werden.");
            } else {
                model.addAttribute("status", "error");
                model.addAttribute("error", "Zurueckweisung fehlgeschlagen.");
            }
        } else {
            model.addAttribute("status", "error");
            model.addAttribute("error", "Unbekannte Aktion.");
        }

        // Reload Spiel details for display
        Spiel spiel = schiriMobileService.getSpielForToken(token);
        if (spiel != null) {
            addSpielDetailsForKontrolle(model, spiel);
        }

        model.addAttribute("token", token);
        SchiriRegistration reg = schiriMobileService.getRegistration(token);
        if (reg != null) {
            model.addAttribute("spielInfo", reg.getSpielInfo());
        }

        return "forward:/nosec/schiri-mobile.xhtml";
    }

    private void addSpielDetailsForKontrolle(Model model, Spiel spiel) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        model.addAttribute("spielId", spiel.getIdString());
        model.addAttribute("spielZeit", sdf.format(spiel.getStart()));
        model.addAttribute("spielPlatz", spiel.getPlatz() != null ? spiel.getPlatz().toString() : "?");
        model.addAttribute("spielTeamA", spiel.getMannschaftAName());
        model.addAttribute("spielTeamB", spiel.getMannschaftBName());
        model.addAttribute("spielToreA", spiel.getToreA());
        model.addAttribute("spielToreB", spiel.getToreB());
        model.addAttribute("spielSchiri", spiel.getSchiriName() != null ? spiel.getSchiriName() : "-");
        model.addAttribute("spielFertigBestaetigt", spiel.isFertigBestaetigt());
    }

    private void addSpielDetails(Model model, SchiriRegistration reg) {
        if (reg.getSpielId() != null) {
            try {
                Spiel spiel = spielRepository.findById(reg.getSpielId()).orElse(null);
                if (spiel != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                    model.addAttribute("spielZeit", sdf.format(spiel.getStart()));
                    model.addAttribute("spielPlatz", spiel.getPlatz() != null ? spiel.getPlatz().toString() : "?");
                    model.addAttribute("spielTeamA", spiel.getMannschaftAName());
                    model.addAttribute("spielTeamB", spiel.getMannschaftBName());
                    model.addAttribute("spielId", spiel.getIdString());
                }
            } catch (Exception e) {
                log.error("Fehler beim Laden der Spieldetails: {}", e.getMessage());
            }
        }
    }

    private String getSchiriTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("SchiriToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
