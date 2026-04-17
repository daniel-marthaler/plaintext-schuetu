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

    /**
     * Returns all Kategorie models for the current game, structured for JSF rendering.
     */
    public List<MatrixKategorieModel> getKategorieModels() {
        if (!holder.hasGame()) {
            return Collections.emptyList();
        }

        List<Kategorie> kategorien = kategorieRepository.findByGame(holder.getGameName());
        List<MatrixKategorieModel> result = new ArrayList<>();

        for (Kategorie kat : kategorien) {
            if (kat.getGruppeA() == null) {
                log.warn("Kategorie ohne Gruppe A uebersprungen: {}", kat);
                continue;
            }

            if (kat.hasVorUndRueckrunde()) {
                // 3 Mannschaften: Vorrunde und Rueckrunde separat anzeigen
                MatrixKategorieModel modelVorrunde = buildKategorieModel(kat, kat.getGruppeA().getMannschaften(), "Vorrunde", Boolean.TRUE);
                if (modelVorrunde != null) {
                    result.add(modelVorrunde);
                }
                MatrixKategorieModel modelRueckrunde = buildKategorieModel(kat, kat.getGruppeA().getMannschaften(), "Rueckrunde", Boolean.FALSE);
                if (modelRueckrunde != null) {
                    result.add(modelRueckrunde);
                }
            } else {
                // Build model for Gruppe A
                MatrixKategorieModel modelA = buildKategorieModel(kat, kat.getGruppeA().getMannschaften(), "A", null);
                if (modelA != null) {
                    result.add(modelA);
                }

                // Build model for Gruppe B (if separate group with different teams)
                if (kat.getGruppeB() != null
                        && !kat.getGruppeB().getMannschaften().isEmpty()) {
                    MatrixKategorieModel modelB = buildKategorieModel(kat, kat.getGruppeB().getMannschaften(), "B", null);
                    if (modelB != null) {
                        result.add(modelB);
                    }
                }
            }
        }

        result.sort(Comparator.comparing(MatrixKategorieModel::getKategorieName));
        return result;
    }

    /**
     * Builds a MatrixKategorieModel from a list of Mannschaften within a Kategorie.
     */
    private MatrixKategorieModel buildKategorieModel(Kategorie kat, List<Mannschaft> mannschaften, String gruppeLabel, Boolean vorrunde) {
        if (mannschaften == null || mannschaften.isEmpty()) {
            return null;
        }

        MatrixKategorieModel model = new MatrixKategorieModel();
        model.setKategorieName(kat.getName());
        model.setGruppenName(kat.getName() + " " + gruppeLabel);
        model.setVorUndRueckrunde(kat.hasVorUndRueckrunde());
        model.setZweiGruppen(kat.getGruppeB() != null && !kat.getGruppeB().getMannschaften().isEmpty() && !kat.hasVorUndRueckrunde());
        model.setMannschaften(mannschaften);
        model.setAnzahlMannschaften(mannschaften.size());

        // Latest game time
        try {
            Spiel latest = kat.getLatestSpiel();
            if (latest != null && latest.getStart() != null) {
                model.setLatestSpielZeit(sdf.format(latest.getStart()));
            }
        } catch (Exception e) {
            model.setLatestSpielZeit("n/a");
        }

        // Build cross-reference matrix
        for (int i = 0; i < mannschaften.size(); i++) {
            Mannschaft mannschaft = mannschaften.get(i);
            MatrixKategorieModel.MatrixZeileModel zeile = new MatrixKategorieModel.MatrixZeileModel();
            zeile.setMannschaftName(mannschaft.getName());
            zeile.setPunkte(mannschaft.getPunkteTotal());
            zeile.setTore(mannschaft.getGeschosseneTore());
            zeile.setGegentore(mannschaft.getKassierteTore());
            zeile.setTorDifferenz(mannschaft.getTorverhaeltnis());
            zeile.setSpieleAbgeschlossen(mannschaft.getSpieleAbgeschlossen());

            // Get relevant games for this team
            List<Spiel> teamSpiele = getRelevantSpiele(mannschaft, vorrunde);
            teamSpiele.sort(new SpielMannschaftsnamenComparator());

            // Build cells for each opponent
            for (int j = 0; j < mannschaften.size(); j++) {
                MatrixZelleModel zelle = new MatrixZelleModel();

                if (i == j) {
                    // Diagonal cell
                    zelle.setDiagonal(true);
                    zeile.getZellen().add(zelle);
                    continue;
                }

                // Find the game between mannschaft and mannschaften[j]
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

                    if (zelle.isFertig()) {
                        // Show goals as: column team (gegner) against row team (eigene)
                        if (spiel.getMannschaftA() != null && spiel.getMannschaftA().getName().equals(mannschaft.getName())) {
                            zelle.setToreEigene(spiel.getToreBBestaetigt());
                            zelle.setToreGegner(spiel.getToreABestaetigt());
                        } else {
                            zelle.setToreEigene(spiel.getToreABestaetigt());
                            zelle.setToreGegner(spiel.getToreBBestaetigt());
                        }
                    }
                } else {
                    zelle.setGegnerName(gegner.getName());
                }

                zeile.getZellen().add(zelle);
            }

            model.getZeilen().add(zeile);
        }

        return model;
    }

    /**
     * Gets the relevant games for a team, filtered by Vorrunde/Rueckrunde if applicable.
     */
    private List<Spiel> getRelevantSpiele(Mannschaft mannschaft, Boolean vorrunde) {
        List<Spiel> alleSpiele = mannschaft.getSpiele();
        if (vorrunde == null) {
            return new ArrayList<>(alleSpiele);
        }

        List<Spiel> result = new ArrayList<>();
        if (vorrunde && alleSpiele.size() > 0) {
            // Vorrunde: first two games
            result.add(alleSpiele.get(0));
            if (alleSpiele.size() > 1) result.add(alleSpiele.get(1));
        } else if (!vorrunde && alleSpiele.size() > 2) {
            // Rueckrunde: games 3 and 4
            result.add(alleSpiele.get(2));
            if (alleSpiele.size() > 3) result.add(alleSpiele.get(3));
        }
        return result;
    }

    /**
     * Finds the game between two specific teams.
     */
    private Spiel findSpielZwischen(List<Spiel> spiele, Mannschaft a, Mannschaft b) {
        for (Spiel spiel : spiele) {
            if (spiel.getMannschaftA() == null || spiel.getMannschaftB() == null) continue;
            String aName = spiel.getMannschaftAName();
            String bName = spiel.getMannschaftBName();
            if ((aName.equals(a.getName()) && bName.equals(b.getName()))
                    || (aName.equals(b.getName()) && bName.equals(a.getName()))) {
                return spiel;
            }
        }
        return null;
    }

    /**
     * Returns whether a game is currently selected.
     */
    public boolean isGameSelected() {
        return holder.hasGame();
    }

    /**
     * Returns the name of the current game.
     */
    public String getGameName() {
        return holder.getGameName();
    }

    /**
     * Refreshes the matrix data (triggered by button click).
     */
    public void refresh() {
        log.info("Matrix-Modern: Daten aktualisiert");
    }
}
