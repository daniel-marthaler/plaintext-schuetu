package ch.plaintext.schuetu.model.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RangierungsgrundEnumTest {

    @Test
    void testEnumValues() {
        assertEquals(11, RangierungsgrundEnum.values().length);
    }

    @Test
    void testValueOf_Punkte() {
        assertEquals(RangierungsgrundEnum.PUNKTE, RangierungsgrundEnum.valueOf("PUNKTE"));
    }

    @Test
    void testValueOf_Tordifferenz() {
        assertEquals(RangierungsgrundEnum.TORDIFFERENZ, RangierungsgrundEnum.valueOf("TORDIFFERENZ"));
    }

    @Test
    void testValueOf_Mehrtore() {
        assertEquals(RangierungsgrundEnum.MEHRTORE, RangierungsgrundEnum.valueOf("MEHRTORE"));
    }

    @Test
    void testValueOf_Direktbegegnung() {
        assertEquals(RangierungsgrundEnum.DIREKTBEGEGNUNG, RangierungsgrundEnum.valueOf("DIREKTBEGEGNUNG"));
    }

    @Test
    void testValueOf_Penalty() {
        assertEquals(RangierungsgrundEnum.PENALTY, RangierungsgrundEnum.valueOf("PENALTY"));
    }

    @Test
    void testValueOf_FinalKl() {
        assertEquals(RangierungsgrundEnum.FINAL_KL, RangierungsgrundEnum.valueOf("FINAL_KL"));
    }

    @Test
    void testValueOf_FinalGr() {
        assertEquals(RangierungsgrundEnum.FINAL_GR, RangierungsgrundEnum.valueOf("FINAL_GR"));
    }

    @Test
    void testValueOf_Vonhand() {
        assertEquals(RangierungsgrundEnum.VONHAND, RangierungsgrundEnum.valueOf("VONHAND"));
    }
}
