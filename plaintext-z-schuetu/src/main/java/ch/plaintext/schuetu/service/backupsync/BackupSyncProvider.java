package ch.plaintext.schuetu.service.backupsync;

import ch.plaintext.schuetu.service.utils.XstreamUtil;
import ch.plaintext.schuetu.entity.Spiel;
import ch.plaintext.schuetu.repository.SpielRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller providing backup sync endpoints
 */
@RestController
@Slf4j
@RequestMapping(value = "/nosec")
public class BackupSyncProvider {

    @Autowired
    private SpielRepository repo;

    private Map<String, List<BackupSyncModel>> map = new HashMap<>();

    public String getOutcount(String game) {

        if (map.containsKey(game)) {
            return "" + map.get(game).size();
        }
        return "";
    }

    public String getOwnSync(String game) {
        return "/nosec/dumpXX/" + game + "/" + getOutcount(game);
    }

    @RequestMapping(value = "/dumpXX/{spielid}/{from:.+}", method = RequestMethod.GET)
    public String sync(@PathVariable("spielid") String spielId, @PathVariable("from") int from, HttpServletResponse response) {
        List<BackupSyncModel> spiele = map.get(spielId);
        if (spiele == null) {
            return XstreamUtil.serializeToString(new ArrayList<BackupSyncModel>());
        }
        List<BackupSyncModel> ret = spiele.subList(from, spiele.size());
        return XstreamUtil.serializeToString(new ArrayList<>(ret));
    }

    @RequestMapping(value = "/dumpXX/{spielid}", method = RequestMethod.GET)
    public String syncO(@PathVariable("spielid") String spielId, HttpServletResponse response) {
        return sync(spielId, 0, response);
    }

    public void signalSpiel(Long id, String game) {

        Spiel spiel = repo.findById(id).get();

        if (map.get(game) == null) {
            map.put(game, new ArrayList<>());
        }

        BackupSyncModel model = new BackupSyncModel();
        model.setId("" + id);
        model.setToreABestaetigt(spiel.getToreABestaetigt());
        model.setToreBBestaetigt(spiel.getToreBBestaetigt());
        map.get(game).add(model);

    }

}
