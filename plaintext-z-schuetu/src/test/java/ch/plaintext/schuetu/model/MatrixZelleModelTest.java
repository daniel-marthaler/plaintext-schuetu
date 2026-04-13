package ch.plaintext.schuetu.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MatrixZelleModelTest {

    @Test
    void testGetStyleClass_Diagonal() {
        MatrixZelleModel m = new MatrixZelleModel();
        m.setDiagonal(true);
        assertEquals("matrix-diagonal", m.getStyleClass());
    }

    @Test
    void testGetStyleClass_Live() {
        MatrixZelleModel m = new MatrixZelleModel();
        m.setAmSpielen(true);
        assertEquals("matrix-live", m.getStyleClass());
    }

    @Test
    void testGetStyleClass_Pending() {
        MatrixZelleModel m = new MatrixZelleModel();
        m.setFertig(false);
        assertEquals("matrix-pending", m.getStyleClass());
    }

    @Test
    void testGetStyleClass_Win() {
        MatrixZelleModel m = new MatrixZelleModel();
        m.setFertig(true);
        m.setToreEigene(1);
        m.setToreGegner(3);
        assertEquals("matrix-win", m.getStyleClass());
    }

    @Test
    void testGetStyleClass_Loss() {
        MatrixZelleModel m = new MatrixZelleModel();
        m.setFertig(true);
        m.setToreEigene(3);
        m.setToreGegner(1);
        assertEquals("matrix-loss", m.getStyleClass());
    }

    @Test
    void testGetStyleClass_Draw() {
        MatrixZelleModel m = new MatrixZelleModel();
        m.setFertig(true);
        m.setToreEigene(2);
        m.setToreGegner(2);
        assertEquals("matrix-draw", m.getStyleClass());
    }

    @Test
    void testGetResultat_Diagonal() {
        MatrixZelleModel m = new MatrixZelleModel();
        m.setDiagonal(true);
        assertEquals("", m.getResultat());
    }

    @Test
    void testGetResultat_PendingWithZeit() {
        MatrixZelleModel m = new MatrixZelleModel();
        m.setFertig(false);
        m.setAmSpielen(false);
        m.setZeit("10:30");
        assertEquals("10:30", m.getResultat());
    }

    @Test
    void testGetResultat_PendingNoZeit() {
        MatrixZelleModel m = new MatrixZelleModel();
        m.setFertig(false);
        m.setAmSpielen(false);
        m.setZeit(null);
        assertEquals("-", m.getResultat());
    }

    @Test
    void testGetResultat_Live() {
        MatrixZelleModel m = new MatrixZelleModel();
        m.setAmSpielen(true);
        assertEquals("LIVE", m.getResultat());
    }

    @Test
    void testGetResultat_Finished() {
        MatrixZelleModel m = new MatrixZelleModel();
        m.setFertig(true);
        m.setToreEigene(3);
        m.setToreGegner(1);
        assertEquals("3:1", m.getResultat());
    }

    @Test
    void testGetDetail_Diagonal() {
        MatrixZelleModel m = new MatrixZelleModel();
        m.setDiagonal(true);
        assertEquals("", m.getDetail());
    }

    @Test
    void testGetDetail_WithPlatzAndSpielId() {
        MatrixZelleModel m = new MatrixZelleModel();
        m.setPlatz("A");
        m.setSpielId("X123");
        assertEquals("A / X123", m.getDetail());
    }

    @Test
    void testGetDetail_OnlyPlatz() {
        MatrixZelleModel m = new MatrixZelleModel();
        m.setPlatz("A");
        assertEquals("A", m.getDetail());
    }

    @Test
    void testGetDetail_OnlySpielId() {
        MatrixZelleModel m = new MatrixZelleModel();
        m.setSpielId("X123");
        assertEquals("X123", m.getDetail());
    }

    @Test
    void testGetDetail_Empty() {
        MatrixZelleModel m = new MatrixZelleModel();
        assertEquals("", m.getDetail());
    }
}
