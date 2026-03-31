package ch.plaintext.schuetu.service.spielkorrekturen;

import ch.plaintext.schuetu.service.GameSelectionHolder;
import ch.plaintext.schuetu.repository.SpielRepository;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * LazySpielkorrekturenRowModel
 */
@Component
@Scope("session")
@Slf4j
public class LazySpielkorrekturenRowModel extends LazyDataModel<SpielKorrektur> {

    // TODO: Replace with plaintext-root security wrapper if available
    // @Autowired
    // private EmadSecWrapper sec;

    @Autowired
    private SpielRepository repo;

    @Autowired
    private GameSelectionHolder holder;

    @Getter
    @Setter
    private SpielKorrektur selected;

    @Autowired
    private KorrekturWrapper wrapper;

    @PostConstruct
    private void init() {
        wrapper.setGame(holder.getGameName());
        wrapper.initOrReload();
    }

    public void kontr() {
        // TODO: sec.getUser() replaced - wire up plaintext-root security
        selected.getSpiel().setKontrolle("TODO");
        repo.save(selected.getSpiel());

        wrapper.initOrReload();

        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect("spielekorrekturen-liste.htm");
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        this.selected = null;

    }

    public void save() {
        // TODO: sec.getUser() replaced - wire up plaintext-root security
        selected.getSpiel().setEintrager("TODO");
        repo.save(selected.getSpiel());

        if (holder.hasGame()) {
            holder.getGame().getResultate().neuberechnenDerKategorie(selected.getSpiel().getMannschaftA().getKategorie(), holder.getGameName());
        }

        wrapper.initOrReload();

        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect("spielekorrekturen-liste.htm");
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        this.selected = null;

    }

    public int count(Map<String, FilterMeta> map) {
        return 0;
    }

    @Override
    public SpielKorrektur getRowData(String rowKey) {
        for (SpielKorrektur spielKorrektur : wrapper.getDatasource()) {
            if (spielKorrektur.getId().equals(rowKey))
                return spielKorrektur;
        }

        return null;
    }

    @Override
    public String getRowKey(SpielKorrektur SpielKorrektur) {
        return SpielKorrektur.getId();
    }

    @Override
    public List<SpielKorrektur> load(int first, int pageSize, Map<String, SortMeta> map, Map<String, FilterMeta> filters) {
        List<SpielKorrektur> data = new ArrayList<>();

        for (SpielKorrektur row : wrapper.getDatasource()) {
            boolean match = true;

            if (filters != null) {
                for (String s : filters.keySet()) {
                    try {
                        Object filterValue = filters.get(s);

                        Field field = SpielKorrektur.class.getDeclaredField(s);
                        field.setAccessible(true);
                        String fieldValue = String.valueOf(field.get(row));

                        if (filterValue == null || fieldValue.contains(filterValue.toString())) {
                            match = true;
                        } else {
                            match = false;
                            break;
                        }
                    } catch (Exception e) {
                        match = false;
                    }
                }
            }

            if (match) {
                data.add(row);
            }
        }

        int dataSize = data.size();
        this.setRowCount(dataSize);

        if (dataSize > pageSize) {
            try {
                return data.subList(first, first + pageSize);
            } catch (IndexOutOfBoundsException e) {
                return data.subList(first, first + (dataSize % pageSize));
            }
        } else {
            return data;
        }
    }
}
