package ch.plaintext.schuetu.service.spielkorrekturen;

import ch.plaintext.schuetu.entity.Spiel;
import ch.plaintext.schuetu.repository.SpielRepository;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanComparator;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Scope("prototype")
@Slf4j
public class KorrekturWrapper extends LazyDataModel<SpielKorrektur> {

    @Autowired
    private SpielRepository repo;

    public synchronized List<SpielKorrektur> getDatasource() {
        return datasource;
    }

    public List<SpielKorrektur> datasource = new ArrayList<>();

    @Setter
    private String game;

    public synchronized void initOrReload() {

        List<SpielKorrektur> datasourceTemp = new ArrayList<>();

        List<Spiel> spiele = repo.findByGame(game);
        for (Spiel spiel : spiele) {
            SpielKorrektur korrektur = new SpielKorrektur();
            korrektur.setSpiel(spiel);
            datasourceTemp.add(korrektur);
        }

        BeanComparator<SpielKorrektur> comp = new BeanComparator<>("startP");

        try {
            datasourceTemp.sort(comp);
        } catch (Exception e) {
            log.error("achtung fehler beim sortieren der KorekturWrapper ev wegen nicht zugeordneter Spiele !!!!" + e.getMessage());
        }
        datasource = datasourceTemp;

    }

    @Scheduled(fixedDelay = 15000)
    public void go() {
        initOrReload();
    }

    public int count(Map<String, FilterMeta> map) {
        return 0;
    }

    @Override
    public List<SpielKorrektur> load(int i, int i1, Map<String, SortMeta> map, Map<String, FilterMeta> map1) {
        return null;
    }
}
