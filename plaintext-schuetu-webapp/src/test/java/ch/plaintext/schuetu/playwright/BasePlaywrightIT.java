package ch.plaintext.schuetu.playwright;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * Basis-Klasse für Playwright Integration Tests.
 * Testet gegen die laufende Applikation.
 */
public abstract class BasePlaywrightIT {

    protected Playwright playwright;
    protected Browser browser;
    protected BrowserContext context;
    protected Page page;

    protected String baseUrl;

    private static final int DEFAULT_TEST_PORT = 9090;

    @BeforeEach
    void setup() {
        String portStr = System.getProperty("test.server.port", String.valueOf(DEFAULT_TEST_PORT));
        int port = Integer.parseInt(portStr);

        String serverUrl = System.getProperty("test.server.url");
        if (serverUrl != null) {
            baseUrl = serverUrl;
        } else {
            baseUrl = "http://localhost:" + port;
        }

        playwright = Playwright.create();

        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(true));

        context = browser.newContext(new Browser.NewContextOptions()
                .setViewportSize(1920, 1080)
                .setLocale("de-CH")
                .setTimezoneId("Europe/Zurich"));

        page = context.newPage();

        page.onConsoleMessage(msg -> System.out.println("Browser Console: " + msg.text()));
    }

    @AfterEach
    void teardown() {
        if (page != null) page.close();
        if (context != null) context.close();
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }

    protected void login(String username, String password) {
        page.navigate(baseUrl + "/login.xhtml");
        page.waitForSelector("#username");
        page.fill("#username", username);
        page.fill("#password", password);
        page.click("button[type='submit']");
        page.waitForURL(baseUrl + "/index.xhtml");
    }

    protected void takeScreenshot(String name) {
        page.screenshot(new Page.ScreenshotOptions()
                .setPath(java.nio.file.Paths.get("target/screenshots/" + name + ".png"))
                .setFullPage(true));
    }
}
