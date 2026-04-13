package ch.plaintext.schuetu.model.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SpielPhasenEnumTest {

    @Test
    void testAllValues() {
        SpielPhasenEnum[] values = SpielPhasenEnum.values();
        assertEquals(7, values.length);
    }

    @Test
    void testValueOf() {
        assertEquals(SpielPhasenEnum.A_ANMELDEPHASE, SpielPhasenEnum.valueOf("A_ANMELDEPHASE"));
        assertEquals(SpielPhasenEnum.B_KATEGORIE_ZUORDNUNG, SpielPhasenEnum.valueOf("B_KATEGORIE_ZUORDNUNG"));
        assertEquals(SpielPhasenEnum.C_SPIELTAGE_DEFINIEREN, SpielPhasenEnum.valueOf("C_SPIELTAGE_DEFINIEREN"));
        assertEquals(SpielPhasenEnum.D_SPIELE_ZUORDNUNG, SpielPhasenEnum.valueOf("D_SPIELE_ZUORDNUNG"));
        assertEquals(SpielPhasenEnum.E_SPIELBEREIT, SpielPhasenEnum.valueOf("E_SPIELBEREIT"));
        assertEquals(SpielPhasenEnum.F_SPIELEN, SpielPhasenEnum.valueOf("F_SPIELEN"));
        assertEquals(SpielPhasenEnum.G_ABGESCHLOSSEN, SpielPhasenEnum.valueOf("G_ABGESCHLOSSEN"));
    }

    @Test
    void testOrder() {
        assertTrue(SpielPhasenEnum.A_ANMELDEPHASE.ordinal() < SpielPhasenEnum.G_ABGESCHLOSSEN.ordinal());
        assertTrue(SpielPhasenEnum.F_SPIELEN.ordinal() < SpielPhasenEnum.G_ABGESCHLOSSEN.ordinal());
    }
}
