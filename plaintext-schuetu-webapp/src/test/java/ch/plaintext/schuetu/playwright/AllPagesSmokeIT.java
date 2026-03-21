package ch.plaintext.schuetu.playwright;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Smoke-Test: Ruft alle Views der Applikation auf und prüft,
 * dass keine HTTP-Fehler (4xx/5xx) oder JavaScript-Errors auftreten.
 */
class AllPagesSmokeIT extends BasePlaywrightIT {

    private final List<String> jsErrors = new ArrayList<>();

    @BeforeEach
    void loginAndSetupErrorTracking() {
        login("admin", "admin");

        jsErrors.clear();
        page.onConsoleMessage(msg -> {
            if ("error".equals(msg.type())) {
                jsErrors.add(msg.text());
            }
        });
    }

    static Stream<String> allPages() {
        return Stream.of(
                // Core
                "/index.xhtml",
                "/dashboard.xhtml",

                // Mannschaften
                "/mannschaft-liste.xhtml",

                // Kategorien
                "/kategorie-liste.xhtml",

                // Spieltage
                "/spieltage-liste.xhtml",

                // Spiele
                "/spielekorrekturen-liste.xhtml",
                "/finalekorrekturen-liste.xhtml",
                "/penaltykorrekturen-liste.xhtml",

                // Eintragen
                "/eintragen-liste.xhtml",

                // Schiri
                "/schiri-liste.xhtml",
                "/schirizettel.xhtml",

                // Rangliste / Matrix
                "/rangliste.xhtml",
                "/matrix.xhtml",

                // Speaker
                "/speaker.xhtml",

                // Historie
                "/historie.xhtml",

                // Einstellungen
                "/einstellungen.xhtml",

                // Exports
                "/export-kategorien.xhtml",
                "/export-teams.xhtml",
                "/export-allemannschaften.xhtml",
                "/export-spiele.xhtml",
                "/export-finale.xhtml",
                "/export-begleitpersonen.xhtml",
                "/export-email-betreuer.xhtml",
                "/export-clubdesk.xhtml",

                // Admin
                "/admin-crecord-liste.xhtml"
        );
    }

    @ParameterizedTest(name = "Seite {0} lädt ohne Fehler")
    @MethodSource("allPages")
    void pageShouldLoadWithoutErrors(String path) {
        jsErrors.clear();

        Response response = page.navigate(baseUrl + path,
                new Page.NavigateOptions().setTimeout(30000));

        assertNotNull(response, "Response für " + path + " sollte nicht null sein");

        int status = response.status();
        assertTrue(status >= 200 && status < 400,
                "Seite " + path + " liefert HTTP " + status + " (erwartet: 2xx/3xx)");

        page.waitForLoadState();

        assertFalse(page.url().contains("login.xhtml"),
                "Seite " + path + " hat auf Login umgeleitet - Session verloren?");

        assertTrue(jsErrors.isEmpty(),
                "JavaScript-Fehler auf " + path + ": " + String.join("; ", jsErrors));
    }

    @Test
    void allPagesShouldBeReachableInSequence() {
        List<String> failedPages = new ArrayList<>();

        allPages().forEach(path -> {
            try {
                Response response = page.navigate(baseUrl + path,
                        new Page.NavigateOptions().setTimeout(15000));
                page.waitForLoadState();

                if (response == null || response.status() >= 400) {
                    failedPages.add(path + " (HTTP " + (response != null ? response.status() : "null") + ")");
                } else if (page.url().contains("login.xhtml")) {
                    failedPages.add(path + " (redirect to login)");
                    login("admin", "admin");
                }
            } catch (Exception e) {
                failedPages.add(path + " (" + e.getMessage() + ")");
            }
        });

        assertTrue(failedPages.isEmpty(),
                "Folgende Seiten haben Fehler:\n" + String.join("\n", failedPages));
    }
}
