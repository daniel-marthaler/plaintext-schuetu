package ch.plaintext.schuetu.web.controllers;

import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import java.util.Map;

@Component
public class SchiriMobileViewRenderer {

    private String csrfToken;
    private String csrfParameterName;

    public void setCsrf(String parameterName, String token) {
        this.csrfParameterName = parameterName;
        this.csrfToken = token;
    }

    private String csrfField() {
        if (csrfToken != null && csrfParameterName != null) {
            return "<input type=\"hidden\" name=\"" + esc(csrfParameterName) + "\" value=\"" + esc(csrfToken) + "\"/>";
        }
        return "";
    }

    public String render(Model model) {
        Map<String, Object> m = model.asMap();
        String status = str(m, "status");
        StringBuilder body = new StringBuilder();

        switch (status) {
            case "error" -> body.append(renderError(m));
            case "already_taken" -> body.append(renderAlreadyTaken());
            case "register" -> body.append(renderRegister(m));
            case "waiting" -> body.append(renderWaiting(m));
            case "approved" -> body.append(renderApproved(m));
            case "eingetragen" -> body.append(renderEingetragen(m));
            case "kontrolle" -> body.append(renderKontrolle(m));
            case "kontrolle_done" -> body.append(renderKontrolleDone(m));
            case "kontrolle_rejected" -> body.append(renderKontrolleRejected(m));
            default -> body.append("<div class=\"status-badge status-error\">Unbekannter Status</div>");
        }

        return wrapHtml(body.toString());
    }

    private String renderError(Map<String, Object> m) {
        return "<div class=\"icon\">&#10060;</div>" +
                "<div class=\"status-badge status-error\">" + esc(str(m, "error")) + "</div>";
    }

    private String renderAlreadyTaken() {
        return "<div class=\"icon\">&#128274;</div>" +
                "<div class=\"status-badge status-error\">Dieser QR-Code wurde bereits verwendet</div>";
    }

    private String renderRegister(Map<String, Object> m) {
        String token = str(m, "token");
        String spielInfo = str(m, "spielInfo");
        StringBuilder sb = new StringBuilder();
        sb.append("<h2>Registrierung als Schiedsrichter</h2>");
        if (!spielInfo.isEmpty()) {
            sb.append("<div class=\"spiel-info-text\">").append(esc(spielInfo)).append("</div>");
        }
        sb.append("<form method=\"post\" action=\"/nosec/schiri-mobile/").append(esc(token)).append("/register\">");
        sb.append(csrfField());
        sb.append("<div class=\"form-group\"><label for=\"name\">Dein Name</label>");
        sb.append("<input type=\"text\" id=\"name\" name=\"name\" placeholder=\"Vor- und Nachname\" required autocomplete=\"name\"/></div>");
        sb.append("<div class=\"form-group\"><label for=\"loginName\">Login-Name (optional)</label>");
        sb.append("<input type=\"text\" id=\"loginName\" name=\"loginName\" placeholder=\"Wunsch-Login\" autocomplete=\"username\"/></div>");
        sb.append("<div class=\"form-group\"><label for=\"telefon\">Telefonnummer</label>");
        sb.append("<input type=\"tel\" id=\"telefon\" name=\"telefon\" placeholder=\"+41 79 123 45 67\" autocomplete=\"tel\"/></div>");
        sb.append("<div class=\"form-group\"><label for=\"password\">Passwort</label>");
        sb.append("<input type=\"password\" id=\"password\" name=\"password\" placeholder=\"Passwort setzen\" autocomplete=\"new-password\"/></div>");
        sb.append("<button type=\"submit\" class=\"btn btn-primary\">Registrieren</button></form>");
        return sb.toString();
    }

    private String renderWaiting(Map<String, Object> m) {
        String token = str(m, "token");
        return "<div class=\"icon pulse\">&#9203;</div>" +
                "<div class=\"status-badge status-waiting\">Warte auf Freigabe</div>" +
                "<div style=\"text-align:center;color:#aaa;margin-bottom:12px;\">Registriert als: <strong style=\"color:#fff;\">" + esc(str(m, "schiriName")) + "</strong></div>" +
                "<script>setInterval(function(){fetch('/nosec/schiri-mobile/" + esc(token) + "/status').then(function(r){return r.json();}).then(function(d){if(d.status==='approved')location.reload();});},5000);</script>";
    }

    private String renderApproved(Map<String, Object> m) {
        String token = str(m, "token");
        StringBuilder sb = new StringBuilder();
        sb.append("<div style=\"text-align:center;color:#aaa;margin-bottom:8px;font-size:0.9em;\">Schiri: <strong style=\"color:#fff;\">")
                .append(esc(str(m, "schiriName"))).append("</strong>");
        sb.append("<span style=\"margin-left:12px;\">Platz ").append(esc(str(m, "spielPlatz"))).append(" | ").append(esc(str(m, "spielZeit"))).append("</span></div>");

        if (!str(m, "spielId").isEmpty()) {
            sb.append("<form method=\"post\" action=\"/nosec/schiri-mobile/").append(esc(token)).append("/eintragen\">");
            sb.append(csrfField());
            sb.append("<div class=\"score-display\" style=\"padding:10px 0;\">");
            sb.append(renderScoreTeamInput(str(m, "spielTeamA"), "scoreA", "toreA"));
            sb.append("<div class=\"score-separator\">:</div>");
            sb.append(renderScoreTeamInput(str(m, "spielTeamB"), "scoreB", "toreB"));
            sb.append("</div>");
            sb.append("<button type=\"submit\" class=\"btn btn-primary\">Ergebnis eintragen</button></form>");
            sb.append("<script>function changeScore(id,d){var e=document.getElementById(id);var v=parseInt(e.value)+d;if(v<0)v=0;if(v>30)v=30;e.value=v;}</script>");
        }
        return sb.toString();
    }

    private String renderScoreTeamInput(String teamName, String inputId, String fieldName) {
        return "<div class=\"score-team\"><div class=\"team-name\">" + esc(teamName) + "</div>" +
                "<button type=\"button\" class=\"btn-counter\" onclick=\"changeScore('" + inputId + "',1)\">+</button>" +
                "<input type=\"number\" name=\"" + fieldName + "\" id=\"" + inputId + "\" min=\"0\" max=\"30\" value=\"0\" readonly class=\"score-input\"/>" +
                "<button type=\"button\" class=\"btn-counter btn-counter-minus\" onclick=\"changeScore('" + inputId + "',-1)\">-</button></div>";
    }

    private String renderEingetragen(Map<String, Object> m) {
        return "<div class=\"icon\">&#9989;</div>" +
                "<div class=\"status-badge status-approved\">Ergebnis eingetragen!</div>" +
                "<div style=\"text-align:center;color:#aaa;margin-bottom:4px;\">Schiri: <strong style=\"color:#fff;\">" + esc(str(m, "schiriName")) + "</strong></div>" +
                renderScoreDisplay(m);
    }

    private String renderKontrolle(Map<String, Object> m) {
        String token = str(m, "token");
        return "<h2>Kontrolle / Ergebnis pruefen</h2>" +
                "<div class=\"icon\">&#128270;</div>" +
                "<div class=\"game-info\"><div class=\"row\"><span class=\"label\">Zeit</span><span class=\"value\">" + esc(str(m, "spielZeit")) + "</span></div>" +
                "<div class=\"row\"><span class=\"label\">Platz</span><span class=\"value\">" + esc(str(m, "spielPlatz")) + "</span></div>" +
                "<div class=\"row\"><span class=\"label\">Schiri</span><span class=\"value\">" + esc(str(m, "spielSchiri")) + "</span></div></div>" +
                renderScoreDisplay(m) +
                "<div class=\"btn-group\">" +
                "<form method=\"post\" action=\"/nosec/schiri-mobile/" + esc(token) + "/kontrolle\" style=\"flex:1;display:flex;\">" + csrfField() + "<input type=\"hidden\" name=\"aktion\" value=\"bestaetigen\"/><button type=\"submit\" class=\"btn btn-confirm\" style=\"flex:1;\">Bestaetigen</button></form>" +
                "<form method=\"post\" action=\"/nosec/schiri-mobile/" + esc(token) + "/kontrolle\" style=\"flex:1;display:flex;\">" + csrfField() + "<input type=\"hidden\" name=\"aktion\" value=\"zurueckweisen\"/><button type=\"submit\" class=\"btn btn-reject\" style=\"flex:1;\">Zurueckweisen</button></form></div>";
    }

    private String renderKontrolleDone(Map<String, Object> m) {
        String msg = str(m, "kontrolleMessage");
        return "<div class=\"icon\">&#9989;</div>" +
                "<div class=\"status-badge status-approved\">" + (msg.isEmpty() ? "Ergebnis bestaetigt" : esc(msg)) + "</div>" +
                "<div class=\"game-info\"><div class=\"row\"><span class=\"label\">Zeit</span><span class=\"value\">" + esc(str(m, "spielZeit")) + "</span></div>" +
                "<div class=\"row\"><span class=\"label\">Platz</span><span class=\"value\">" + esc(str(m, "spielPlatz")) + "</span></div>" +
                "<div class=\"row\"><span class=\"label\">Schiri</span><span class=\"value\">" + esc(str(m, "spielSchiri")) + "</span></div>" +
                "<div class=\"row\"><span class=\"label\">Kontrolleur</span><span class=\"value\">" + esc(str(m, "spielKontrolle")) + "</span></div></div>" +
                renderScoreDisplay(m);
    }

    private String renderKontrolleRejected(Map<String, Object> m) {
        return "<div class=\"icon\">&#10060;</div>" +
                "<div class=\"status-badge status-rejected\">" + esc(str(m, "kontrolleMessage")) + "</div>" +
                "<div class=\"game-info\"><div class=\"row\"><span class=\"label\">" + esc(str(m, "spielTeamA")) + "</span><span class=\"value\">" + m.getOrDefault("spielToreA", "") + "</span></div>" +
                "<div class=\"row\"><span class=\"label\">" + esc(str(m, "spielTeamB")) + "</span><span class=\"value\">" + m.getOrDefault("spielToreB", "") + "</span></div></div>" +
                "<div style=\"text-align:center;color:#aaa;font-size:0.85em;margin-top:16px;\">Das Ergebnis muss am Eintrage-Posten neu eingegeben werden.</div>";
    }

    private String renderScoreDisplay(Map<String, Object> m) {
        return "<div class=\"score-display\"><div class=\"score-team\"><div class=\"team-name\">" + esc(str(m, "spielTeamA")) + "</div><div class=\"team-score\">" + m.getOrDefault("spielToreA", "") + "</div></div>" +
                "<div class=\"score-separator\">:</div>" +
                "<div class=\"score-team\"><div class=\"team-name\">" + esc(str(m, "spielTeamB")) + "</div><div class=\"team-score\">" + m.getOrDefault("spielToreB", "") + "</div></div></div>";
    }

    private String str(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v != null ? v.toString() : "";
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private String wrapHtml(String body) {
        return """
                <!DOCTYPE html>
                <html lang="de">
                <head>
                <meta charset="UTF-8"/>
                <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0"/>
                <meta name="apple-mobile-web-app-capable" content="yes"/>
                <title>Schiri Mobile</title>
                <style>
                *{box-sizing:border-box;margin:0;padding:0}
                body{font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;background:#1a1a2e;color:#eee;min-height:100vh;display:flex;flex-direction:column;align-items:center;padding:20px}
                .container{width:100%;max-width:500px;background:#16213e;border-radius:16px;padding:24px;margin-top:20px;box-shadow:0 8px 32px rgba(0,0,0,0.3)}
                h1{text-align:center;font-size:1.5em;margin-bottom:8px;color:#e2b714}
                h2{text-align:center;font-size:1.1em;margin-bottom:20px;color:#aaa;font-weight:normal}
                .status-badge{display:inline-block;padding:6px 16px;border-radius:20px;font-size:0.9em;font-weight:bold;text-align:center;width:100%;margin-bottom:16px}
                .status-waiting{background:#f39c12;color:#000}.status-approved{background:#27ae60;color:#fff}.status-error{background:#e74c3c;color:#fff}
                .form-group{margin-bottom:16px}.form-group label{display:block;margin-bottom:6px;font-size:0.95em;color:#ccc}
                .form-group input[type="text"],.form-group input[type="tel"]{width:100%;padding:14px;border:2px solid #333;border-radius:10px;background:#0f3460;color:#fff;font-size:1.1em;outline:none}
                .form-group input:focus{border-color:#e2b714}
                .btn{display:block;width:100%;padding:16px;border:none;border-radius:10px;font-size:1.1em;font-weight:bold;cursor:pointer;text-align:center;margin-top:12px}
                .btn-primary{background:#e2b714;color:#000}.btn-primary:active{background:#c9a012}
                .game-info{background:#0f3460;border-radius:12px;padding:16px;margin-top:16px}
                .game-info .row{display:flex;justify-content:space-between;padding:8px 0;border-bottom:1px solid #1a1a3e}
                .game-info .row:last-child{border-bottom:none}.game-info .label{color:#888}.game-info .value{font-weight:bold;color:#e2b714}
                .icon{font-size:3em;text-align:center;margin:16px 0}
                .pulse{animation:pulse 2s infinite}@keyframes pulse{0%,100%{opacity:1}50%{opacity:0.5}}
                .spiel-info-text{text-align:center;color:#aaa;font-size:0.9em;margin-bottom:12px}
                .score-display{display:flex;align-items:center;justify-content:center;gap:16px;padding:20px 0}
                .score-team{text-align:center;flex:1}.score-team .team-name{font-size:1.1em;font-weight:bold;color:#e2b714;margin-bottom:8px}
                .score-team .team-score{font-size:3em;font-weight:bold;color:#fff}
                .score-separator{font-size:2.5em;font-weight:bold;color:#888}
                .btn-confirm{background:#27ae60;color:#fff}.btn-confirm:active{background:#1e8449}
                .btn-reject{background:#e67e22;color:#fff}.btn-reject:active{background:#d35400}
                .btn-group{display:flex;gap:12px;margin-top:16px}.btn-group .btn{flex:1}
                .status-rejected{background:#e67e22;color:#fff}
                .score-input{width:90px;font-size:3em;text-align:center;background:#0f3460;color:#fff;border:2px solid #333;border-radius:10px;padding:8px;margin:8px 0;-moz-appearance:textfield}
                .score-input::-webkit-outer-spin-button,.score-input::-webkit-inner-spin-button{-webkit-appearance:none;margin:0}
                .btn-counter{display:block;width:90px;padding:12px 0;border:none;border-radius:10px;font-size:1.8em;font-weight:bold;cursor:pointer;text-align:center;background:#27ae60;color:#fff;margin:0 auto}
                .btn-counter:active{background:#1e8449}.btn-counter-minus{background:#e74c3c}.btn-counter-minus:active{background:#c0392b}
                </style>
                </head>
                <body>
                <div class="container">
                <h1>Schiri Mobile</h1>
                """ + body + """
                </div>
                </body>
                </html>
                """;
    }
}
