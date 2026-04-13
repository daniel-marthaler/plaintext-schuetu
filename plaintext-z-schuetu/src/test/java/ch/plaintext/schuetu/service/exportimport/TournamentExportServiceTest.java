package ch.plaintext.schuetu.service.exportimport;

import ch.plaintext.schuetu.entity.*;
import ch.plaintext.schuetu.model.enums.GeschlechtEnum;
import ch.plaintext.schuetu.model.enums.PlatzEnum;
import ch.plaintext.schuetu.model.enums.SpielEnum;
import ch.plaintext.schuetu.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TournamentExportServiceTest {

    @InjectMocks
    private TournamentExportService exportService;

    @Mock
    private GameRepository gameRepository;
    @Mock
    private MannschaftRepository mannschaftRepository;
    @Mock
    private SchiriRepository schiriRepository;
    @Mock
    private KategorieRepository kategorieRepository;
    @Mock
    private SpielRepository spielRepository;
    @Mock
    private SpielZeilenRepository spielZeilenRepository;
    @Mock
    private PenaltyRepository penaltyRepository;
    @Mock
    private KorrekturRepository korrekturRepository;

    @Test
    void testExportTournament_GameNotFound() {
        when(gameRepository.findByGameName("unknown")).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> exportService.exportTournament("unknown"));
    }

    @Test
    void testExportTournament_EmptyTournament() {
        GameModel gm = new GameModel();
        gm.setId(1L);
        gm.setGameName("Test2026");
        gm.setSpielPhase("anmeldung");

        when(gameRepository.findByGameName("Test2026")).thenReturn(gm);
        when(mannschaftRepository.findByGame("Test2026")).thenReturn(List.of());
        when(schiriRepository.findByGame("Test2026")).thenReturn(List.of());
        when(kategorieRepository.findByGame("Test2026")).thenReturn(List.of());
        when(spielRepository.findByGame("Test2026")).thenReturn(List.of());
        when(spielZeilenRepository.findByGame("Test2026")).thenReturn(List.of());
        when(penaltyRepository.findByGame("Test2026")).thenReturn(List.of());
        when(korrekturRepository.findByGame("Test2026")).thenReturn(List.of());

        TournamentExportDto dto = exportService.exportTournament("Test2026");

        assertNotNull(dto);
        assertEquals("1.0", dto.getExportVersion());
        assertEquals("Test2026", dto.getOriginalGameName());
        assertNotNull(dto.getExportDate());
        assertNotNull(dto.getGameModel());
        assertEquals("Test2026", dto.getGameModel().getGameName());
        assertTrue(dto.getMannschaften().isEmpty());
        assertTrue(dto.getSchiris().isEmpty());
        assertTrue(dto.getKategorien().isEmpty());
        assertTrue(dto.getSpiele().isEmpty());
    }

    @Test
    void testExportTournament_WithMannschaften() {
        GameModel gm = new GameModel();
        gm.setId(1L);
        gm.setGameName("Test2026");

        Mannschaft m = new Mannschaft();
        m.setId(10L);
        m.setNickname("Tigers");
        m.setKlasse(5);
        m.setGeschlecht(GeschlechtEnum.M);
        m.setSchulhaus("Schulhaus A");

        when(gameRepository.findByGameName("Test2026")).thenReturn(gm);
        when(mannschaftRepository.findByGame("Test2026")).thenReturn(List.of(m));
        when(schiriRepository.findByGame("Test2026")).thenReturn(List.of());
        when(kategorieRepository.findByGame("Test2026")).thenReturn(List.of());
        when(spielRepository.findByGame("Test2026")).thenReturn(List.of());
        when(spielZeilenRepository.findByGame("Test2026")).thenReturn(List.of());
        when(penaltyRepository.findByGame("Test2026")).thenReturn(List.of());
        when(korrekturRepository.findByGame("Test2026")).thenReturn(List.of());

        TournamentExportDto dto = exportService.exportTournament("Test2026");

        assertEquals(1, dto.getMannschaften().size());
        TournamentExportDto.MannschaftDto mDto = dto.getMannschaften().get(0);
        assertEquals(10L, mDto.getOriginalId());
        assertEquals("Tigers", mDto.getNickname());
        assertEquals(5, mDto.getKlasse());
        assertEquals("M", mDto.getGeschlecht());
        assertEquals("Schulhaus A", mDto.getSchulhaus());
    }

    @Test
    void testExportTournament_WithSchiris() {
        GameModel gm = new GameModel();
        gm.setId(1L);
        gm.setGameName("Test2026");

        Schiri s = new Schiri();
        s.setId(20L);
        s.setVorname("Max");
        s.setNachname("Muster");
        s.setAktiviert(true);

        when(gameRepository.findByGameName("Test2026")).thenReturn(gm);
        when(mannschaftRepository.findByGame("Test2026")).thenReturn(List.of());
        when(schiriRepository.findByGame("Test2026")).thenReturn(List.of(s));
        when(kategorieRepository.findByGame("Test2026")).thenReturn(List.of());
        when(spielRepository.findByGame("Test2026")).thenReturn(List.of());
        when(spielZeilenRepository.findByGame("Test2026")).thenReturn(List.of());
        when(penaltyRepository.findByGame("Test2026")).thenReturn(List.of());
        when(korrekturRepository.findByGame("Test2026")).thenReturn(List.of());

        TournamentExportDto dto = exportService.exportTournament("Test2026");

        assertEquals(1, dto.getSchiris().size());
        TournamentExportDto.SchiriDto sDto = dto.getSchiris().get(0);
        assertEquals(20L, sDto.getOriginalId());
        assertEquals("Max", sDto.getVorname());
        assertEquals("Muster", sDto.getNachname());
        assertTrue(sDto.isAktiviert());
    }

    @Test
    void testExportTournament_WithSpiele() {
        GameModel gm = new GameModel();
        gm.setId(1L);
        gm.setGameName("Test2026");

        Mannschaft mA = new Mannschaft();
        mA.setId(10L);
        Mannschaft mB = new Mannschaft();
        mB.setId(11L);

        Spiel spiel = new Spiel();
        spiel.setId(30L);
        spiel.setTyp(SpielEnum.GRUPPE);
        spiel.setMannschaftA(mA);
        spiel.setMannschaftB(mB);
        spiel.setToreA(3);
        spiel.setToreB(1);
        spiel.setPlatz(PlatzEnum.A);

        when(gameRepository.findByGameName("Test2026")).thenReturn(gm);
        when(mannschaftRepository.findByGame("Test2026")).thenReturn(List.of());
        when(schiriRepository.findByGame("Test2026")).thenReturn(List.of());
        when(kategorieRepository.findByGame("Test2026")).thenReturn(List.of());
        when(spielRepository.findByGame("Test2026")).thenReturn(List.of(spiel));
        when(spielZeilenRepository.findByGame("Test2026")).thenReturn(List.of());
        when(penaltyRepository.findByGame("Test2026")).thenReturn(List.of());
        when(korrekturRepository.findByGame("Test2026")).thenReturn(List.of());

        TournamentExportDto dto = exportService.exportTournament("Test2026");

        assertEquals(1, dto.getSpiele().size());
        TournamentExportDto.SpielDto sDto = dto.getSpiele().get(0);
        assertEquals(30L, sDto.getOriginalId());
        assertEquals("GRUPPE", sDto.getTyp());
        assertEquals(10L, sDto.getMannschaftAId());
        assertEquals(11L, sDto.getMannschaftBId());
        assertEquals(3, sDto.getToreA());
        assertEquals(1, sDto.getToreB());
        assertEquals("A", sDto.getPlatz());
    }

    @Test
    void testExportTournament_WithKorrekturen() {
        GameModel gm = new GameModel();
        gm.setId(1L);
        gm.setGameName("Test2026");

        Korrektur k = new Korrektur();
        k.setId(40L);
        k.setTyp("spielzeile");
        k.setWert("123");
        k.setReihenfolge(1);

        when(gameRepository.findByGameName("Test2026")).thenReturn(gm);
        when(mannschaftRepository.findByGame("Test2026")).thenReturn(List.of());
        when(schiriRepository.findByGame("Test2026")).thenReturn(List.of());
        when(kategorieRepository.findByGame("Test2026")).thenReturn(List.of());
        when(spielRepository.findByGame("Test2026")).thenReturn(List.of());
        when(spielZeilenRepository.findByGame("Test2026")).thenReturn(List.of());
        when(penaltyRepository.findByGame("Test2026")).thenReturn(List.of());
        when(korrekturRepository.findByGame("Test2026")).thenReturn(List.of(k));

        TournamentExportDto dto = exportService.exportTournament("Test2026");

        assertEquals(1, dto.getKorrekturen().size());
        TournamentExportDto.KorrekturDto kDto = dto.getKorrekturen().get(0);
        assertEquals(40L, kDto.getOriginalId());
        assertEquals("spielzeile", kDto.getTyp());
        assertEquals("123", kDto.getWert());
        assertEquals(1, kDto.getReihenfolge());
    }
}
