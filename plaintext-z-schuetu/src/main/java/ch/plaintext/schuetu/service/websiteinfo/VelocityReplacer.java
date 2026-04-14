package ch.plaintext.schuetu.service.websiteinfo;

import ch.plaintext.schuetu.service.Game;
import ch.plaintext.schuetu.service.GameRoot;
import ch.plaintext.schuetu.service.GameSelectionHolder;
import ch.plaintext.schuetu.model.Einstellungen;
import lombok.extern.slf4j.Slf4j;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.PostConstruct;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Velocity-basierter Website-Generator
 *
 * TODO: Old framework class EmadSuperCron removed - re-implement cron scheduling if needed
 * TODO: Old framework class EmadIHTTP removed - re-implement HTTP posting if needed
 * TODO: Old framework class ContextUtil removed - re-implement context utilities if needed
 */
@Controller
@Scope("prototype")
@Slf4j
public class VelocityReplacer {

    private Map<String, String> content = new ConcurrentHashMap<>();

    @Autowired
    private GameRoot root;

    @Autowired
    private WebsiteInfoService info;

    // TODO: EmadIHTTP removed - re-implement website posting
    // @Autowired
    // private EmadIHTTP website;

    @Autowired
    private GameRoot gameRoot;

    @Autowired
    private GameSelectionHolder gameHolder;

    @Autowired
    private JdbcUpdate replacer;

    @Autowired
    private ch.plaintext.schuetu.service.SpielzeilenService spielzeilenService;

    private static Map<String, String> pages = new ConcurrentHashMap<>();
    private static Map<String, String> urls = new ConcurrentHashMap<>();

    public void run(String s) {
        write();
    }

    public void write() {

        String mandat = "worb";

        for (String key : pages.keySet()) {

            String text = replace(key);

            text = text.replace("\u00e4", "&auml;");
            text = text.replace("\u00f6", "&ouml;");
            text = text.replace("\u00fc", "&uuml;");
            text = text.replace("\u00e9", "&eacute;");
            text = text.replace("\u00eb", "&euml;");

            Map<String, String> map = new HashMap<>();

            for (String key2 : map.keySet()) {
                text = text.replace(key2, map.get(key2));
            }

            replacer.update(text, pages.get(key), "page_id");
            // TODO: EmadIHTTP removed - re-implement website posting
            // website.postWB(pages.get(key), text, "websiteinfo", urls.get(key), mandat);
        }
        pages.clear();
    }

    public void dumpWebsite() {
        dump(gameHolder.getGameName());
    }

    public void dump(String game) {
        Game gm = root.selectGame(game);

        // Plätze und Zeiten von Spielzeilen auf Spiele synchronisieren
        spielzeilenService.spielZeitenAnpassen();
        log.info("Plätze/Zeiten von Spielzeilen synchronisiert vor Upload für {}", game);

        String id = gm.getModel().getWebsiteId();
        String url = gm.getModel().getWebsiteUrl();

        dump(game, id, url);

        write();
    }

    public void dump(String game, String id, String url) {

        Game gm = root.getGameCache().get(game);

        if (!gm.getModel().isUploadOn()) {
            return;
        }

        pages.put(game, id);
        if (!url.endsWith("/")) {
            url = url + "/";
        }
        urls.put(game, url);
    }

    @RequestMapping(value = "/nosec/result/{game:.+}", method = RequestMethod.GET)
    @ResponseBody
    public String website(@PathVariable("game") String game) {
        return cache(game);
    }

    public String cache(String game) {
        if (content.containsKey(game)) {
            return content.get(game);
        }
        gameRoot.selectGame(game);
        String result = replace(game);
        content.put(game, result);
        return result;
    }

    public String replace(String game) {
        StringWriter sw = new StringWriter();

        VelocityEngine ve = new VelocityEngine();
        ve.setProperty("resource.loaders", "classpath");
        ve.setProperty("resource.loader.classpath.class", ClasspathResourceLoader.class.getName());

        ve.init();

        final String templatePath = "websitetemplate.vm";
        InputStream input = VelocityReplacer.class.getClassLoader().getResourceAsStream(templatePath);
        if (input == null) {
            log.error("template: websitetemplate.vm not available");
            return "<p>Fehler: Website-Template nicht verfügbar</p>";
        }

        Einstellungen einstellungen = info.getEinstellungen(game);

        InputStreamReader reader = new InputStreamReader(input);

        VelocityContext context = new VelocityContext();

        // Mobilelink
        if (einstellungen.isMobileLinkOn()) {
            context.put("moblink", "<h4>Mobiles Teamprogramm: <a href=\"" + einstellungen.getMobileLink() + "\">hier</a></h4>");
        } else {
            context.put("moblink", "");
        }

        // Programmheft
        if (einstellungen.isWebsiteEnableProgrammDownloadLink()) {
            context.put("programm", "<h4>Programmheft herunterladen: <a href=\"" + einstellungen.getWebsiteProgrammDownloadLink() + "\">hier</a></h4>");
        } else {
            context.put("programm", "");
        }

        // Anmeldung Download
        if (einstellungen.isWebsiteEnableDownloadLink()) {
            context.put("anmeldung", "<h4>Anmeldung herunterladen: <a href=\"" + einstellungen.getWebsiteDownloadLink() + "\">hier</a></h4>");
        } else {
            context.put("anmeldung", "");
        }

        context.put("titel", einstellungen.getWebsiteTurnierTitel());

        context.put("grspiele", info.getGruppenspiele(game));
        context.put("finspiele", info.getFinalspiele(game));

        context.put("knaben", info.getKnabenMannschaften2(game));
        context.put("madchen", info.getMaedchenMannschaften2(game));

        context.put("rangliste", info.getRangliste(game));

        BufferedWriter writer = new BufferedWriter(sw);

        if (!ve.evaluate(context, writer, templatePath, reader)) {
            log.error("failed to generate html");
        }

        try {
            writer.flush();
            writer.close();
        } catch (Exception e) {
            log.error("failed to generate html", e.getMessage());
        }

        String temp = sw.toString();
        temp = temp.replace("$spiel.mannschaftA.name", "-");
        temp = temp.replace("$spiel.mannschaftB.name", "-");
        temp = temp.replace("[- -] - -", "-");
        return temp;
    }
}
