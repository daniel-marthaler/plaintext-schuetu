package ch.plaintext.schuetu.web;

import ch.plaintext.schuetu.entity.Kategorie;
import ch.plaintext.schuetu.entity.Mannschaft;
import ch.plaintext.schuetu.entity.Spiel;
import ch.plaintext.schuetu.model.MatrixKategorieModel;
import ch.plaintext.schuetu.model.MatrixZelleModel;
import ch.plaintext.schuetu.model.comparators.SpielMannschaftsnamenComparator;
import ch.plaintext.schuetu.repository.KategorieRepository;
import ch.plaintext.schuetu.service.GameSelectionHolder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Backing bean for the modern matrix view (matrix-modern.xhtml).
 * Provides structured data instead of raw HTML for proper JSF rendering.
 */
@Component
@Scope("session")
@Slf4j
public class MatrixModernBean {

    private final SimpleDateFormat sdf = new SimpleDateFormat("E HH:mm");

    @Autowired
    private GameSelectionHolder holder;

    @Autowired
    private KategorieRepository kategorieRepository;

    @Getter
    @Setter
    private int activeTabIndex = 0;

    private List<MatrixKategorieModel> cachedModels = null;
    private String cachedGameName = null;

    /**
     * Returns all Kategorie models for the current game, structured for JSF rendering.
     */
    @Transactional(readOnly = true)
    public List<MatrixKategorieModel> getKategorieModels() {
        if (!holder.hasGame()) {
            return Collections.emptyList();
        }

        String gameName = holder.getGameName();
        if (cachedModels != null && gameName.equals(cachedGameName)) {
            return cachedModels;
        }

        try {
            List<Kategorie> kategorien = kategorieRepository.findByGame(gameName);
            List<MatrixKategorieModel> result = new ArrayList<>();

            for (Kategorie kat : kategorien) {
                try {
                    if (kat.getGruppeA() == null) {
                        continue;
                    }

                    List<Mannschaft> mannschaftenA = kat.getGruppeA().getMannschaften();
                    if (mannschaftenA == null || mannschaftenA.isEmpty()) {
                        continue;
                    }

                    if (kat.hasVorUndRueckrunde()) {
                        MatrixKategorieModel modelVorrunde = buildKategorieModel(kat, mannschaftenA, "Vorrunde", Boolean.TRUE);
                        if (modelVorrunde != null) result.add(modelVorrunde);

                        MatrixKategorieModel modelRueckrunde = buildKategorieModel(kat, mannschaftenA, "Rueckrunde", Boolean.FALSE);
                        if (modelRueckrunde != null) result.add(modelRueckrunde);
                    } else {
                        MatrixKategorieModel modelA = buildKategorieModel(kat, mannschaftenA, "A", null);
                        if (modelA != null) result.add(modelA);

                        if (kat.getGruppeB() != null) {
                            List<Mannschaft> mannschaftenB = kat.getGruppeB().getMannschaften();
                            if (mannschaftenB != null && !mannschaftenB.isEmpty()) {
                                MatrixKategorieModel modelB = buildKategorieModel(kat, mannschaftenB, "B", null);
                                if (modelB != null) result.add(modelB);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("Fehler beim Verarbeiten der Kategorie {}: {}", kat.getName(), e.getMessage());
                }
            }

            result.sort(Comparator.comparing(MatrixKategorieModel::getKategorieName));
            cachedModels = result;
            cachedGameName = gameName;
            return result;
        } catch (Exception e) {
            log.error("Fehler beim Laden der Matrix-Daten: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private MatrixKategorieModel buildKategorieModel(Kategorie kat, List<Mannschaft> mannschaften, String gruppeLabel, Boolean vorrunde) {
        if (mannschaften == null || mannschaften.isEmpty()) {
            return null;
        }

        MatrixKategorieModel model = new MatrixKategorieModel();
        model.setKategorieName(kat.getName());
        model.setGruppenName(kat.getName() + " " + gruppeLabel);
        model.setVorUndRueckrunde(kat.hasVorUndRueckrunde());
        model.setMannschaften(mannschaften);
        model.setAnzahlMannschaften(mannschaften.size());

        try {
            model.setZweiGruppen(kat.getGruppeB() != null && kat.getGruppeB().getMannschaften() != null
                    && !kat.getGruppeB().getMannschaften().isEmpty() && !kat.hasVorUndRueckrunde());
        } catch (Exception e) {
            model.setZweiGruppen(false);
        }

        try {
            List<Spiel> allSpiele = new ArrayList<>(kat.getSpiele());
            if (!allSpiele.isEmpty()) {
                allSpiele.sort((a, b) -> {
                    if (a.getStart() == null || b.getStart() == null) return 0;
                    return a.getStart().compareTo(b.getStart());
                });
                Spiel latest = allSpiele.get(allSpiele.size() - 1);
                if (latest.getStart() != null) {
                    model.setLatestSpielZeit(sdf.format(latest.getStart()));
                }
            }
        } catch (Exception e) {
            model.setLatestSpielZeit("n/a");
        }

        // Build cross-reference matrix
        for (int i = 0; i < mannschaften.size(); i++) {
            Mannschaft mannschaft = mannschaften.get(i);
            MatrixKategorieModel.MatrixZeileModel zeile = new MatrixKategorieModel.MatrixZeileModel();
            zeile.setMannschaftName(mannschaft.getName());

            try {
                zeile.setPunkte(mannschaft.getPunkteTotal());
                zeile.setTore(mannschaft.getGeschosseneTore());
                zeile.setGegentore(mannschaft.getKassierteTore());
                zeile.setTorDifferenz(mannschaft.getTorverhaeltnis());
                zeile.setSpieleAbgeschlossen(mannschaft.getSpieleAbgeschlossen());
            } catch (Exception e) {
                log.debug("Stats nicht verfuegbar fuer {}: {}", mannschaft.getName(), e.getMessage());
            }

            List<Spiel> teamSpiele;
            try {
                teamSpiele = getRelevantSpiele(mannschaft, vorrunde);
                teamSpiele.sort(new SpielMannschaftsnamenComparator());
            } catch (Exception e) {
                teamSpiele = Collections.emptyList();
                log.debug("Spiele nicht ladbar fuer {}: {}", mannschaft.getName(), e.getMessage());
            }

            for (int j = 0; j < mannschaften.size(); j++) {
                MatrixZelleModel zelle = new MatrixZelleModel();

                if (i == j) {
                    zelle.setDiagonal(true);
                    zeile.getZellen().add(zelle);
                    continue;
                }

                try {
                    Mannschaft gegner = mannschaften.get(j);
                    Spiel spiel = findSpielZwischen(teamSpiele, mannschaft, gegner);

                    if (spiel != null) {
                        zelle.setGegnerName(gegner.getName());
                        zelle.setPlatz(spiel.getPlatz() != null ? spiel.getPlatz().toString() : "");
                        zelle.setSpielId(spiel.getIdString());
                        if (spiel.getStart() != null) {
                            zelle.setZeit(sdf.format(spiel.getStart()));
                        }
                        zelle.setAmSpielen(spiel.isAmSpielen());
                        zelle.setFertig(spiel.getToreABestaetigt() > -1);
                        zelle.setEingetragen(spiel.isFertigEingetragen() && !zelle.isFertig());

                        if (zelle.isFertig()) {
                            if (spiel.getMannschaftA() != null && spiel.getMannschaftA().getName().equals(mannschaft.getName())) {
                                zelle.setToreEigene(spiel.getToreBBestaetigt());
                                zelle.setToreGegner(spiel.getToreABestaetigt());
                            } else {
                                zelle.setToreEigene(spiel.getToreABestaetigt());
                                zelle.setToreGegner(spiel.getToreBBestaetigt());
                            }
                        } else if (zelle.isEingetragen()) {
                            if (spiel.getMannschaftA() != null && spiel.getMannschaftA().getName().equals(mannschaft.getName())) {
                                zelle.setToreEigene(spiel.getToreB());
                                zelle.setToreGegner(spiel.getToreA());
                            } else {
                                zelle.setToreEigene(spiel.getToreA());
                                zelle.setToreGegner(spiel.getToreB());
                            }
                        }
                    } else {
                        zelle.setGegnerName(gegner.getName());
                    }
                } catch (Exception e) {
                    log.debug("Zelle [{},{}] Fehler: {}", i, j, e.getMessage());
                }

                zeile.getZellen().add(zelle);
            }

            model.getZeilen().add(zeile);
        }

        return model;
    }

    private List<Spiel> getRelevantSpiele(Mannschaft mannschaft, Boolean vorrunde) {
        List<Spiel> alleSpiele = mannschaft.getSpiele();
        if (alleSpiele == null) return new ArrayList<>();
        if (vorrunde == null) {
            return new ArrayList<>(alleSpiele);
        }

        List<Spiel> result = new ArrayList<>();
        if (vorrunde && !alleSpiele.isEmpty()) {
            result.add(alleSpiele.get(0));
            if (alleSpiele.size() > 1) result.add(alleSpiele.get(1));
        } else if (!vorrunde && alleSpiele.size() > 2) {
            result.add(alleSpiele.get(2));
            if (alleSpiele.size() > 3) result.add(alleSpiele.get(3));
        }
        return result;
    }

    private Spiel findSpielZwischen(List<Spiel> spiele, Mannschaft a, Mannschaft b) {
        for (Spiel spiel : spiele) {
            try {
                if (spiel.getMannschaftA() == null || spiel.getMannschaftB() == null) continue;
                String aName = spiel.getMannschaftAName();
                String bName = spiel.getMannschaftBName();
                if ((aName.equals(a.getName()) && bName.equals(b.getName()))
                        || (aName.equals(b.getName()) && bName.equals(a.getName()))) {
                    return spiel;
                }
            } catch (Exception e) {
                log.debug("findSpielZwischen Fehler: {}", e.getMessage());
            }
        }
        return null;
    }

    public boolean isGameSelected() {
        return holder.hasGame();
    }

    public String getGameName() {
        return holder.getGameName();
    }

    public void refresh() {
        cachedModels = null;
        cachedGameName = null;
        log.info("Matrix-Modern: Daten aktualisiert");
    }
}
