package ch.plaintext.schuetu.model.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SpielZeilenPhaseEnumTest {

    @Test
    void testGetText() {
        assertEquals("A_ANSTEHEND", SpielZeilenPhaseEnum.A_ANSTEHEND.getText());
        assertEquals("B_ZUR_VORBEREITUNG", SpielZeilenPhaseEnum.B_ZUR_VORBEREITUNG.getText());
        assertEquals("C_VORBEREITET", SpielZeilenPhaseEnum.C_VORBEREITET.getText());
        assertEquals("D_SPIELEND", SpielZeilenPhaseEnum.D_SPIELEND.getText());
        assertEquals("E_BEENDET", SpielZeilenPhaseEnum.E_BEENDET.getText());
    }

    @Test
    void testFromString_Valid() {
        assertEquals(SpielZeilenPhaseEnum.A_ANSTEHEND, SpielZeilenPhaseEnum.fromString("A_ANSTEHEND"));
        assertEquals(SpielZeilenPhaseEnum.B_ZUR_VORBEREITUNG, SpielZeilenPhaseEnum.fromString("B_ZUR_VORBEREITUNG"));
        assertEquals(SpielZeilenPhaseEnum.C_VORBEREITET, SpielZeilenPhaseEnum.fromString("C_VORBEREITET"));
        assertEquals(SpielZeilenPhaseEnum.D_SPIELEND, SpielZeilenPhaseEnum.fromString("D_SPIELEND"));
        assertEquals(SpielZeilenPhaseEnum.E_BEENDET, SpielZeilenPhaseEnum.fromString("E_BEENDET"));
    }

    @Test
    void testFromString_CaseInsensitive() {
        assertEquals(SpielZeilenPhaseEnum.A_ANSTEHEND, SpielZeilenPhaseEnum.fromString("a_anstehend"));
        assertEquals(SpielZeilenPhaseEnum.E_BEENDET, SpielZeilenPhaseEnum.fromString("e_beendet"));
    }

    @Test
    void testFromString_Null() {
        assertEquals(SpielZeilenPhaseEnum.A_ANSTEHEND, SpielZeilenPhaseEnum.fromString(null));
    }

    @Test
    void testFromString_Unknown() {
        assertEquals(SpielZeilenPhaseEnum.A_ANSTEHEND, SpielZeilenPhaseEnum.fromString("UNKNOWN"));
    }

    @Test
    void testEnumValues() {
        assertEquals(5, SpielZeilenPhaseEnum.values().length);
    }
}
