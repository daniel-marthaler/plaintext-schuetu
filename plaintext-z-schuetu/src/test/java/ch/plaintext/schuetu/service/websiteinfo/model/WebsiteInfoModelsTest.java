package ch.plaintext.schuetu.service.websiteinfo.model;

import ch.plaintext.schuetu.model.enums.GeschlechtEnum;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WebsiteInfoModelsTest {

    @Test
    void testMannschaft() {
        Mannschaft m = new Mannschaft();
        m.setGruppe("MGr5");
        m.setNummer("01");
        m.setKlasse("5");
        m.setKlassenname("5a");
        m.setSchulhaus("Schulhaus A");
        m.setCaptain("Max Muster");
        m.setBegleitperson("Peter Paan");
        m.setSpieler(7);

        assertEquals("MGr5", m.getGruppe());
        assertEquals("01", m.getNummer());
        assertEquals("5", m.getKlasse());
        assertEquals("5a", m.getKlassenname());
        assertEquals("Schulhaus A", m.getSchulhaus());
        assertEquals("Max Muster", m.getCaptain());
        assertEquals("Peter Paan", m.getBegleitperson());
        assertEquals(7, m.getSpieler());
    }

    @Test
    void testMannschaftEintrag_NullMannschaft() {
        MannschaftEintrag e = new MannschaftEintrag();
        assertFalse(e.hasMannschaft());
        assertEquals("", e.getName());
        assertEquals("", e.getSchulhaus());
    }

    @Test
    void testMannschaftEintrag_WithMannschaft() {
        ch.plaintext.schuetu.entity.Mannschaft m = new ch.plaintext.schuetu.entity.Mannschaft();
        m.setGeschlecht(ch.plaintext.schuetu.model.enums.GeschlechtEnum.M);
        m.setKlasse(5);
        m.setTeamNummer(1);
        m.setSchulhaus("Schulhaus A");
        m.setFarbe("");

        MannschaftEintrag e = new MannschaftEintrag();
        e.setMannschaft(m);

        assertTrue(e.hasMannschaft());
        assertTrue(e.getName().contains("M5"));
        assertEquals("Schulhaus A", e.getSchulhaus());
    }

    @Test
    void testKlassenrangZeile_DefaultMannschaften() {
        KlassenrangZeile k = new KlassenrangZeile();
        assertEquals(4, k.getMannschaften().size());
        for (MannschaftEintrag me : k.getMannschaften()) {
            assertFalse(me.hasMannschaft());
        }
    }

    @Test
    void testKlassenrangZeile_Name_Knaben() {
        KlassenrangZeile k = new KlassenrangZeile();
        k.setKlasse(5);
        k.setGeschlecht(GeschlechtEnum.K);

        assertEquals("Knaben 5. Klasse", k.getName());
    }

    @Test
    void testKlassenrangZeile_Name_Maedchen() {
        KlassenrangZeile k = new KlassenrangZeile();
        k.setKlasse(4);
        k.setGeschlecht(GeschlechtEnum.M);

        assertEquals("Maedchen 4. Klasse", k.getName());
    }

    @Test
    void testKlassenrangZeile_NameInverse() {
        KlassenrangZeile k = new KlassenrangZeile();
        k.setKlasse(5);
        k.setGeschlecht(GeschlechtEnum.K);

        assertEquals("5. Klasse Knaben", k.getNameInverse());
    }

    @Test
    void testKlassenrangZeile_AddNext() {
        KlassenrangZeile k = new KlassenrangZeile();
        ch.plaintext.schuetu.entity.Mannschaft m = new ch.plaintext.schuetu.entity.Mannschaft();
        m.setGeschlecht(ch.plaintext.schuetu.model.enums.GeschlechtEnum.M);
        m.setKlasse(5);
        m.setTeamNummer(1);
        m.setFarbe("");
        m.setSchulhaus("Test");

        k.addNext(m);

        assertTrue(k.getMannschaften().get(0).hasMannschaft());
        assertFalse(k.getMannschaften().get(1).hasMannschaft());
    }

    @Test
    void testKlassenrangZeile_GetAsZeilen() {
        KlassenrangZeile k = new KlassenrangZeile();
        k.setKlasse(5);
        k.setGeschlecht(GeschlechtEnum.M);

        List<List<String>> zeilen = k.getAsZeilen();
        assertEquals(4, zeilen.size());
        assertEquals(5, zeilen.get(0).size()); // header row
    }

    @Test
    void testTeamGruppen() {
        TeamGruppen tg = new TeamGruppen();
        tg.setName("MGr5");
        tg.setTotal(5);

        assertEquals("MGr5", tg.getName());
        assertEquals(5, tg.getTotal());
        assertTrue(tg.getMannschaften().isEmpty());
    }

    @Test
    void testTeamGruppen_AddMannschaft() {
        TeamGruppen tg = new TeamGruppen();
        Mannschaft m = new Mannschaft();
        m.setKlasse("5");
        tg.addMannschaft(m);

        assertEquals(1, tg.getMannschaften().size());
    }
}
