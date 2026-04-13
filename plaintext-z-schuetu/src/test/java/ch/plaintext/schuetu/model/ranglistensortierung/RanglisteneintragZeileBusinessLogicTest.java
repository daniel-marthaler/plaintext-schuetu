package ch.plaintext.schuetu.model.ranglistensortierung;

import ch.plaintext.schuetu.entity.Mannschaft;
import ch.plaintext.schuetu.model.enums.GeschlechtEnum;
import ch.plaintext.schuetu.model.enums.RangierungsgrundEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RanglisteneintragZeileBusinessLogicTest {

    private Mannschaft createMannschaft(int teamNr, int klasse) {
        Mannschaft m = new Mannschaft();
        m.setTeamNummer(teamNr);
        m.setKlasse(klasse);
        m.setGeschlecht(GeschlechtEnum.M);
        m.setFarbe("");
        m.setSchulhaus("");
        return m;
    }

    @Test
    void testDefaultValues() {
        RanglisteneintragZeile z = new RanglisteneintragZeile();
        assertNull(z.getMannschaft());
        assertEquals(RangierungsgrundEnum.KEINSPIEL, z.getRangierungsgrund());
        assertEquals(0, z.getToreErziehlt());
        assertEquals(0, z.getToreKassiert());
        assertEquals(0, z.getSpieleVorbei());
        assertEquals(0, z.getSpieleAnstehend());
        assertEquals(0, z.getPunkte());
    }

    @Test
    void testGetTordifferenz() {
        RanglisteneintragZeile z = new RanglisteneintragZeile();
        z.setToreErziehlt(10);
        z.setToreKassiert(3);
        assertEquals(7, z.getTordifferenz());
    }

    @Test
    void testGetTordifferenz_Negative() {
        RanglisteneintragZeile z = new RanglisteneintragZeile();
        z.setToreErziehlt(2);
        z.setToreKassiert(5);
        assertEquals(-3, z.getTordifferenz());
    }

    @Test
    void testGetTordifferenz_Zero() {
        RanglisteneintragZeile z = new RanglisteneintragZeile();
        z.setToreErziehlt(3);
        z.setToreKassiert(3);
        assertEquals(0, z.getTordifferenz());
    }

    @Test
    void testPrint() {
        RanglisteneintragZeile z = new RanglisteneintragZeile();
        Mannschaft m = createMannschaft(1, 5);
        z.setMannschaft(m);
        z.setPunkte(9);
        z.setToreErziehlt(12);
        z.setToreKassiert(3);
        z.setSpieleVorbei(3);
        z.setSpieleAnstehend(0);
        z.setRangierungsgrund(RangierungsgrundEnum.PUNKTE);

        String result = z.print();
        assertNotNull(result);
        assertTrue(result.contains("M501"));
        assertTrue(result.contains("P:09"));
        assertTrue(result.contains("TV:09"));
        assertTrue(result.contains("Te:12"));
    }

    @Test
    void testToString() {
        RanglisteneintragZeile z = new RanglisteneintragZeile();
        Mannschaft m = createMannschaft(1, 5);
        z.setMannschaft(m);
        z.setPunkte(6);
        z.setToreErziehlt(8);
        z.setToreKassiert(4);

        String result = z.toString();
        assertTrue(result.contains("M501"));
        assertTrue(result.contains("p:6"));
        assertTrue(result.contains("tv:4"));
        assertTrue(result.contains("te:8"));
    }

    @Test
    void testSetRangierungsgrund() {
        RanglisteneintragZeile z = new RanglisteneintragZeile();
        z.setRangierungsgrund(RangierungsgrundEnum.TORDIFFERENZ);
        assertEquals(RangierungsgrundEnum.TORDIFFERENZ, z.getRangierungsgrund());
    }
}
