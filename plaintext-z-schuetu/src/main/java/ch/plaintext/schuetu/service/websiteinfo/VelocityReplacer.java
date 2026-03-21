package ch.plaintext.schuetu.service.websiteinfo;

import ch.plaintext.schuetu.service.Game;
import ch.plaintext.schuetu.service.GameRoot;
import ch.plaintext.schuetu.service.GameSelectionHolder;
import ch.plaintext.schuetu.entity.Einstellungen;
import lombok.extern.slf4j.Slf4j;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Scope("prototype")
@Slf4j
public class VelocityReplacer {

    private Map<String, String> content = new ConcurrentHashMap<>();

    @Autowired private GameRoot root;
    @Autowired private WebsiteInfoService info;

    // TODO: EmadIHTTP replaced - use RestTemplate/WebClient for website upload
    // @Autowired private EmadIHTTP website;

    @Autowired private GameRoot gameRoot;
    @Autowired private GameSelectionHolder gameHolder;
    @Autowired private JdbcUpdate replacer;

    private static Map<String, String> pages = new ConcurrentHashMap<>();
    private static Map<String, String> urls = new ConcurrentHashMap<>();

    public void write() {
        for (String key : pages.keySet()) {
            String text = replace(key);
            text = text.replace("ae", "&auml;").replace("oe", "&ouml;").replace("ue", "&uuml;");
            replacer.update(text, pages.get(key), "page_id");
            // TODO: website.postWB(...) - re-implement with RestTemplate
        }
        pages.clear();
    }

    public void dumpWebsite() { dump(gameHolder.getGameName()); }

    public void dump(String game) {
        Game gm = root.selectGame(game);
        String id = gm.getModel().getWebsiteId();
        String url = gm.getModel().getWebsiteUrl();
        dump(game, id, url);
        write();
    }

    public void dump(String game, String id, String url) {
        Game gm = root.getGameCache().get(game);
        if (!gm.getModel().isUploadOn()) { return; }
        pages.put(game, id);
        if (!url.endsWith("/")) { url = url + "/"; }
        urls.put(game, url);
    }

    @RequestMapping(value = "/nosec/result/{game:.+}", method = RequestMethod.GET)
    @ResponseBody
    public String website(@PathVariable("game") String game) { return cache(game); }

    public String cache(String game) {
        if (content.containsKey(game)) { return content.get(game); }
        gameRoot.selectGame(game);
        String result = replace(game);
        content.put(game, result);
        return result;
    }

    public String replace(String game) {
        StringWriter sw = new StringWriter();
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        ve.init();

        final String templatePath = "websitetemplate.vm";
        InputStream input = VelocityReplacer.class.getClassLoader().getResourceAsStream(templatePath);
        if (input == null) { log.error("template: websitetemplate.vm not available"); return ""; }

        Einstellungen einstellungen = info.getEinstellungen(game);
        InputStreamReader reader = new InputStreamReader(input);
        VelocityContext context = new VelocityContext();

        if (einstellungen.isMobileLinkOn()) { context.put("moblink", "<h4>Mobiles Teamprogramm: <a href=\"" + einstellungen.getMobileLink() + "\">hier</a></h4>"); } else { context.put("moblink", ""); }
        if (einstellungen.isWebsiteEnableProgrammDownloadLink()) { context.put("programm", "<h4>Programmheft herunterladen: <a href=\"" + einstellungen.getWebsiteProgrammDownloadLink() + "\">hier</a></h4>"); } else { context.put("programm", ""); }
        if (einstellungen.isWebsiteEnableDownloadLink()) { context.put("anmeldung", "<h4>Anmeldung herunterladen: <a href=\"" + einstellungen.getWebsiteDownloadLink() + "\">hier</a></h4>"); } else { context.put("anmeldung", ""); }

        context.put("titel", einstellungen.getWebsiteTurnierTitel());
        context.put("grspiele", info.getGruppenspiele(game));
        context.put("finspiele", info.getFinalspiele(game));
        context.put("knaben", info.getKnabenMannschaften2(game));
        context.put("madchen", info.getMaedchenMannschaften2(game));
        context.put("rangliste", info.getRangliste(game));

        BufferedWriter writer = new BufferedWriter(sw);
        if (!ve.evaluate(context, writer, templatePath, reader)) { log.error("failed to generate html"); }
        try { writer.flush(); writer.close(); } catch (Exception e) { log.error("failed to generate html", e.getMessage()); }

        String temp = sw.toString();
        temp = temp.replace("$spiel.mannschaftA.name", "-").replace("$spiel.mannschaftB.name", "-").replace("[- -] - -", "-");
        return temp;
    }
}
