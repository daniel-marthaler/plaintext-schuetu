package ch.plaintext.schuetu.model.ranglistensortierung;

import ch.plaintext.schuetu.entity.Mannschaft;
import ch.plaintext.schuetu.model.enums.GeschlechtEnum;
import ch.plaintext.schuetu.model.enums.RangierungsgrundEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RanglisteneintragZeileTest {

    private RanglisteneintragZeile zeile;
    private Mannschaft mannschaft;

    @BeforeEach
    void setUp() {
        zeile = new RanglisteneintragZeile();
        mannschaft = new Mannschaft();
        mannschaft.setTeamNummer(1);
        mannschaft.setKlasse(5);
        mannschaft.setGeschlecht(GeschlechtEnum.M);
        zeile.setMannschaft(mannschaft);
    }

    @Test
    void testDefaultRangierungsgrund() {
        assertEquals(RangierungsgrundEnum.KEINSPIEL, zeile.getRangierungsgrund());
    }

    @Test
    void testGetTordifferenz() {
        zeile.setToreErziehlt(10);
        zeile.setToreKassiert(3);

        assertEquals(7, zeile.getTordifferenz());
    }

    @Test
    void testGetTordifferenz_Negative() {
        zeile.setToreErziehlt(2);
        zeile.setToreKassiert(5);

        assertEquals(-3, zeile.getTordifferenz());
    }

    @Test
    void testGetTordifferenz_Zero() {
        zeile.setToreErziehlt(3);
        zeile.setToreKassiert(3);

        assertEquals(0, zeile.getTordifferenz());
    }

    @Test
    void testSettersGetters() {
        zeile.setPunkte(9);
        zeile.setToreErziehlt(15);
        zeile.setToreKassiert(5);
        zeile.setSpieleVorbei(3);
        zeile.setSpieleAnstehend(1);
        zeile.setRangierungsgrund(RangierungsgrundEnum.PUNKTE);

        assertEquals(9, zeile.getPunkte());
        assertEquals(15, zeile.getToreErziehlt());
        assertEquals(5, zeile.getToreKassiert());
        assertEquals(3, zeile.getSpieleVorbei());
        assertEquals(1, zeile.getSpieleAnstehend());
        assertEquals(RangierungsgrundEnum.PUNKTE, zeile.getRangierungsgrund());
    }

    @Test
    void testPrint() {
        zeile.setPunkte(9);
        zeile.setToreErziehlt(15);
        zeile.setToreKassiert(5);
        zeile.setSpieleVorbei(3);
        zeile.setSpieleAnstehend(1);
        zeile.setRangierungsgrund(RangierungsgrundEnum.PUNKTE);

        String printed = zeile.print();
        assertTrue(printed.contains("M501"));
        assertTrue(printed.contains("P:09"));
        assertTrue(printed.contains("TV:10"));
        assertTrue(printed.contains("Te:15"));
        assertTrue(printed.contains("PUNKTE"));
    }

    @Test
    void testToString() {
        zeile.setPunkte(6);
        zeile.setToreErziehlt(8);
        zeile.setToreKassiert(4);
        zeile.setRangierungsgrund(RangierungsgrundEnum.TORDIFFERENZ);

        String str = zeile.toString();
        assertTrue(str.contains("M501"));
        assertTrue(str.contains("p:6"));
        assertTrue(str.contains("tv:4"));
        assertTrue(str.contains("TORDIFFERENZ"));
    }
}
