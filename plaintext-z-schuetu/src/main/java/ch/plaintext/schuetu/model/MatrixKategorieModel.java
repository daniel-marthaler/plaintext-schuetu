package ch.plaintext.schuetu.model;

import ch.plaintext.schuetu.entity.Kategorie;
import ch.plaintext.schuetu.entity.Mannschaft;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Holds the structured matrix data for a single Kategorie.
 * Contains the list of teams and their cross-reference game cells.
 */
@Data
public class MatrixKategorieModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private String kategorieName;
    private String gruppenName;
    private boolean vorUndRueckrunde;
    private boolean zweiGruppen;
    private String latestSpielZeit;
    private int anzahlMannschaften;

    private List<Mannschaft> mannschaften = new ArrayList<>();
    private List<MatrixZeileModel> zeilen = new ArrayList<>();

    /**
     * A single row in the matrix: one team vs all other teams.
     */
    @Data
    public static class MatrixZeileModel implements Serializable {

        private static final long serialVersionUID = 1L;

        private String mannschaftName;
        private int punkte;
        private int tore;
        private int gegentore;
        private int torDifferenz;
        private int spieleAbgeschlossen;
        private List<MatrixZelleModel> zellen = new ArrayList<>();
    }
}
