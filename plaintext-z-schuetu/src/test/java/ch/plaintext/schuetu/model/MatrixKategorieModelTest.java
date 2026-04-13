package ch.plaintext.schuetu.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class MatrixKategorieModelTest {

    @Test
    void testDefaultValues() {
        MatrixKategorieModel m = new MatrixKategorieModel();
        assertNotNull(m.getMannschaften());
        assertNotNull(m.getZeilen());
        assertTrue(m.getMannschaften().isEmpty());
        assertTrue(m.getZeilen().isEmpty());
    }

    @Test
    void testSetAndGetFields() {
        MatrixKategorieModel m = new MatrixKategorieModel();
        m.setKategorieName("MKl5");
        m.setGruppenName("MGr5");
        m.setVorUndRueckrunde(true);
        m.setZweiGruppen(false);
        m.setLatestSpielZeit("14:30");
        m.setAnzahlMannschaften(6);

        assertEquals("MKl5", m.getKategorieName());
        assertEquals("MGr5", m.getGruppenName());
        assertTrue(m.isVorUndRueckrunde());
        assertFalse(m.isZweiGruppen());
        assertEquals("14:30", m.getLatestSpielZeit());
        assertEquals(6, m.getAnzahlMannschaften());
    }

    @Test
    void testMatrixZeileModel() {
        MatrixKategorieModel.MatrixZeileModel zeile = new MatrixKategorieModel.MatrixZeileModel();
        zeile.setMannschaftName("M501");
        zeile.setPunkte(9);
        zeile.setTore(12);
        zeile.setGegentore(3);
        zeile.setTorDifferenz(9);
        zeile.setSpieleAbgeschlossen(3);

        assertEquals("M501", zeile.getMannschaftName());
        assertEquals(9, zeile.getPunkte());
        assertEquals(12, zeile.getTore());
        assertEquals(3, zeile.getGegentore());
        assertEquals(9, zeile.getTorDifferenz());
        assertEquals(3, zeile.getSpieleAbgeschlossen());
        assertNotNull(zeile.getZellen());
    }
}
