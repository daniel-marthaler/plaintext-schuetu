package ch.plaintext.schuetu.model;

import lombok.Data;

import java.io.Serializable;

/**
 * Represents a single cell in the matrix view.
 * Contains the game result between two teams.
 */
@Data
public class MatrixZelleModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private String gegnerName;
    private String platz;
    private String spielId;
    private String zeit;
    private int toreEigene = -1;
    private int toreGegner = -1;
    private boolean fertig;
    private boolean eingetragen;
    private boolean amSpielen;
    private boolean diagonal;

    /**
     * Returns the CSS class for color-coding:
     * green = win, red = loss, yellow = draw, gray = not played, white = diagonal
     */
    public String getStyleClass() {
        if (diagonal) {
            return "matrix-diagonal";
        }
        if (amSpielen) {
            return "matrix-live";
        }
        if (eingetragen && !fertig) {
            return "matrix-eingetragen";
        }
        if (!fertig) {
            return "matrix-pending";
        }
        if (toreEigene < toreGegner) {
            return "matrix-win";
        }
        if (toreEigene > toreGegner) {
            return "matrix-loss";
        }
        return "matrix-draw";
    }

    /**
     * Returns a display string for the result.
     */
    public String getResultat() {
        if (diagonal) {
            return "";
        }
        if (eingetragen && !fertig) {
            return toreEigene + ":" + toreGegner;
        }
        if (!fertig && !amSpielen) {
            return zeit != null ? zeit : "-";
        }
        if (amSpielen) {
            return "LIVE";
        }
        return toreEigene + ":" + toreGegner;
    }

    /**
     * Returns the detail string (Platz and SpielId).
     */
    public String getDetail() {
        if (diagonal) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        if (platz != null && !platz.isEmpty()) {
            sb.append(platz);
        }
        if (spielId != null && !spielId.isEmpty()) {
            if (sb.length() > 0) sb.append(" / ");
            sb.append(spielId);
        }
        return sb.toString();
    }
}
