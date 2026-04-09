package ch.plaintext.schuetu.web.controllers;

import ch.plaintext.schuetu.entity.Spiel;
import ch.plaintext.schuetu.repository.SpielRepository;
import ch.plaintext.schuetu.service.qrcode.SchiriMobileService;
import ch.plaintext.schuetu.service.qrcode.SchiriMobileService.SchiriRegistration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
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

    @Autowired
    private SchiriMobileViewRenderer viewRenderer;

    /**
     * Zeigt die mobile Schiri-Seite an.
     * Je nach Status: Registrierungsformular, Wartestatus, Spieldetails oder Kontrollierer-Ansicht.
     *
     * Wenn das Spiel bereits eingetragen ist (fertigEingetragen=true), wird die
     * Kontrollierer-Ansicht angezeigt, damit ein zweiter Scan das Ergebnis bestaetigen kann.
     */
    private void initCsrf(HttpServletRequest request) {
        CsrfToken csrf = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrf != null) {
            viewRenderer.setCsrf(csrf.getParameterName(), csrf.getToken());
        } else {
            viewRenderer.setCsrf(null, null);
        }
    }

    @GetMapping(value = "/{token}", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String showMobilePage(@PathVariable String token, HttpServletRequest request,
                                  HttpServletResponse response, Model model) {
        initCsrf(request);

        SchiriRegistration reg = schiriMobileService.getRegistration(token);
        if (reg == null) {
            model.addAttribute("error", "Ungueltiger QR-Code / Token");
            model.addAttribute("status", "error");
            return viewRenderer.render(model);
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
            return viewRenderer.render(model);
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
            // Not yet registered - check SchiriName cookie for auto-registration
            String savedName = getSchiriNameFromCookie(request);
            String savedTelefon = getCookieValue(request, "SchiriTelefon");
            if (savedName != null && !savedName.isBlank()) {
                // Auto-register and auto-approve with saved name
                boolean success = schiriMobileService.registerSchiri(token, savedName, savedTelefon);
                if (success) {
                    schiriMobileService.approveSchiri(token);

                    Cookie tokenCookie = new Cookie("SchiriToken", token);
                    tokenCookie.setMaxAge(60 * 60 * 24);
                    tokenCookie.setPath("/");
                    response.addCookie(tokenCookie);

                    model.addAttribute("status", "approved");
                    model.addAttribute("schiriName", savedName);
                    addSpielDetails(model, schiriMobileService.getRegistration(token));
                } else {
                    model.addAttribute("status", "register");
                }
            } else {
                model.addAttribute("status", "register");
            }
        }

        model.addAttribute("token", token);
        model.addAttribute("spielInfo", reg.getSpielInfo());
        return viewRenderer.render(model);
    }

    /**
     * Registriert einen Schiri-Namen fuer den Token
     */
    @PostMapping(value = "/{token}/register", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String registerSchiri(@PathVariable String token,
                                 @RequestParam String name,
                                 @RequestParam(required = false) String telefon,
                                 @RequestParam(required = false) String loginName,
                                 @RequestParam(required = false) String password,
                                 HttpServletRequest request,
                                 HttpServletResponse response,
                                 Model model) {
        initCsrf(request);

        boolean success = schiriMobileService.registerSchiri(token, name, telefon, loginName, password);

        if (success) {
            // Auto-approve - no admin approval needed
            schiriMobileService.approveSchiri(token);

            // Set token cookie so the user stays "logged in"
            Cookie cookie = new Cookie("SchiriToken", token);
            cookie.setMaxAge(60 * 60 * 24); // 24 Stunden
            cookie.setPath("/");
            response.addCookie(cookie);

            // Set name cookie for auto-registration on future QR scans
            Cookie nameCookie = new Cookie("SchiriName", java.net.URLEncoder.encode(name, java.nio.charset.StandardCharsets.UTF_8));
            nameCookie.setMaxAge(60 * 60 * 24 * 365); // 1 Jahr
            nameCookie.setPath("/");
            response.addCookie(nameCookie);

            // Set telefon cookie
            if (telefon != null && !telefon.isBlank()) {
                Cookie telCookie = new Cookie("SchiriTelefon", java.net.URLEncoder.encode(telefon, java.nio.charset.StandardCharsets.UTF_8));
                telCookie.setMaxAge(60 * 60 * 24 * 365);
                telCookie.setPath("/");
                response.addCookie(telCookie);
            }

            model.addAttribute("status", "approved");
            model.addAttribute("schiriName", name);
            model.addAttribute("token", token);

            SchiriRegistration reg = schiriMobileService.getRegistration(token);
            if (reg != null) {
                model.addAttribute("spielInfo", reg.getSpielInfo());
                addSpielDetails(model, reg);
            }
        } else {
            model.addAttribute("status", "error");
            model.addAttribute("error", "Registrierung fehlgeschlagen. Token ungueltig.");
            model.addAttribute("token", token);
        }

        return viewRenderer.render(model);
    }

    /**
     * Traegt das Spielergebnis ein (Schiri-Aktion via Mobile).
     */
    @PostMapping(value = "/{token}/eintragen", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String eintragen(@PathVariable String token,
                             @RequestParam int toreA,
                             @RequestParam int toreB,
                             HttpServletRequest request,
                             Model model) {
        initCsrf(request);

        boolean success = schiriMobileService.eintragenSpiel(token, toreA, toreB);

        if (success) {
            model.addAttribute("status", "eingetragen");
            Spiel spiel = schiriMobileService.getSpielForToken(token);
            if (spiel != null) {
                addSpielDetailsForKontrolle(model, spiel);
            }
        } else {
            model.addAttribute("status", "error");
            model.addAttribute("error", "Eintragen fehlgeschlagen. Token ungueltig.");
        }

        model.addAttribute("token", token);
        SchiriRegistration reg = schiriMobileService.getRegistration(token);
        if (reg != null) {
            model.addAttribute("spielInfo", reg.getSpielInfo());
            model.addAttribute("schiriName", reg.getSchiriName());
        }

        return viewRenderer.render(model);
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
    @PostMapping(value = "/{token}/kontrolle", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String kontrolle(@PathVariable String token,
                             @RequestParam String aktion,
                             HttpServletRequest request,
                             Model model) {
        initCsrf(request);

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

        return viewRenderer.render(model);
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

    private String getSchiriNameFromCookie(HttpServletRequest request) {
        return getCookieValue(request, "SchiriName");
    }

    private String getCookieValue(HttpServletRequest request, String name) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (name.equals(cookie.getName())) {
                    return java.net.URLDecoder.decode(cookie.getValue(), java.nio.charset.StandardCharsets.UTF_8);
                }
            }
        }
        return null;
    }
}
