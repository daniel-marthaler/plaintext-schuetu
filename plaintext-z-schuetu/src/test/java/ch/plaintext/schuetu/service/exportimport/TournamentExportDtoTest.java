package ch.plaintext.schuetu.service.exportimport;

import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class TournamentExportDtoTest {

    @Test
    void testDefaultValues() {
        TournamentExportDto dto = new TournamentExportDto();
        assertEquals("1.0", dto.getExportVersion());
        assertNotNull(dto.getMannschaften());
        assertNotNull(dto.getSchiris());
        assertNotNull(dto.getKategorien());
        assertNotNull(dto.getGruppen());
        assertNotNull(dto.getSpiele());
        assertNotNull(dto.getSpielzeilen());
        assertNotNull(dto.getPenalties());
        assertNotNull(dto.getKorrekturen());
        assertTrue(dto.getMannschaften().isEmpty());
    }

    @Test
    void testGameModelDto() {
        TournamentExportDto.GameModelDto gm = new TournamentExportDto.GameModelDto();
        gm.setOriginalId(1L);
        gm.setGameName("Turnier2026");
        gm.setSpielPhase("spielen");
        gm.setSpiellaenge(10);
        gm.setSpiellaengefinale(13);
        gm.setPause(2);
        gm.setAutomatischesAufholen(true);

        assertEquals(1L, gm.getOriginalId());
        assertEquals("Turnier2026", gm.getGameName());
        assertEquals("spielen", gm.getSpielPhase());
        assertEquals(10, gm.getSpiellaenge());
        assertEquals(13, gm.getSpiellaengefinale());
        assertEquals(2, gm.getPause());
        assertTrue(gm.isAutomatischesAufholen());
    }

    @Test
    void testMannschaftDto() {
        TournamentExportDto.MannschaftDto m = new TournamentExportDto.MannschaftDto();
        m.setOriginalId(10L);
        m.setNickname("Eagles");
        m.setKlasse(6);
        m.setGeschlecht("K");
        m.setSchulhaus("Schulhaus B");
        m.setCaptainName("Max Muster");

        assertEquals(10L, m.getOriginalId());
        assertEquals("Eagles", m.getNickname());
        assertEquals(6, m.getKlasse());
        assertEquals("K", m.getGeschlecht());
    }

    @Test
    void testSpielDto() {
        TournamentExportDto.SpielDto s = new TournamentExportDto.SpielDto();
        s.setOriginalId(30L);
        s.setTyp("GRUPPE");
        s.setMannschaftAId(10L);
        s.setMannschaftBId(11L);
        s.setToreA(3);
        s.setToreB(1);
        s.setFertigGespielt(true);
        s.setFertigBestaetigt(true);

        assertEquals(30L, s.getOriginalId());
        assertEquals("GRUPPE", s.getTyp());
        assertEquals(3, s.getToreA());
        assertEquals(1, s.getToreB());
        assertTrue(s.isFertigGespielt());
    }

    @Test
    void testKorrekturDto() {
        TournamentExportDto.KorrekturDto k = new TournamentExportDto.KorrekturDto();
        k.setOriginalId(40L);
        k.setTyp("spielzeile");
        k.setWert("123");
        k.setReihenfolge(1);

        assertEquals(40L, k.getOriginalId());
        assertEquals("spielzeile", k.getTyp());
        assertEquals("123", k.getWert());
        assertEquals(1, k.getReihenfolge());
    }

    @Test
    void testGruppeDto() {
        TournamentExportDto.GruppeDto g = new TournamentExportDto.GruppeDto();
        g.setOriginalId(50L);
        g.setKategorieId(60L);
        g.setGeschlecht("M");

        assertEquals(50L, g.getOriginalId());
        assertEquals(60L, g.getKategorieId());
        assertNotNull(g.getMannschaftIds());
        assertNotNull(g.getSpielIds());
    }

    @Test
    void testPenaltyDto() {
        TournamentExportDto.PenaltyDto p = new TournamentExportDto.PenaltyDto();
        p.setOriginalId(70L);
        p.setGruppeId(50L);
        p.setGespielt(true);
        p.setBestaetigt(true);

        assertEquals(70L, p.getOriginalId());
        assertTrue(p.isGespielt());
        assertTrue(p.isBestaetigt());
        assertNotNull(p.getFinalListIds());
    }

    @Test
    void testSchiriDto() {
        TournamentExportDto.SchiriDto s = new TournamentExportDto.SchiriDto();
        s.setOriginalId(80L);
        s.setVorname("Max");
        s.setNachname("Muster");
        s.setAktiviert(true);
        s.setMatchcount(5);

        assertEquals(80L, s.getOriginalId());
        assertEquals("Max", s.getVorname());
        assertTrue(s.isAktiviert());
        assertEquals(5, s.getMatchcount());
    }

    @Test
    void testSpielZeileDto() {
        TournamentExportDto.SpielZeileDto sz = new TournamentExportDto.SpielZeileDto();
        sz.setOriginalId(90L);
        sz.setAId(30L);
        sz.setBId(31L);
        sz.setSonntag(true);
        sz.setFinale(true);
        sz.setPhase("A_ANSTEHEND");

        assertEquals(90L, sz.getOriginalId());
        assertEquals(30L, sz.getAId());
        assertTrue(sz.isSonntag());
        assertTrue(sz.isFinale());
    }
}
