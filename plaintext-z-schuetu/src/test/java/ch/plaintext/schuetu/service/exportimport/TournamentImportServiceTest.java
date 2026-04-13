package ch.plaintext.schuetu.service.exportimport;

import ch.plaintext.schuetu.entity.*;
import ch.plaintext.schuetu.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TournamentImportServiceTest {

    @InjectMocks
    private TournamentImportService importService;

    @Mock
    private GameRepository gameRepository;
    @Mock
    private MannschaftRepository mannschaftRepository;
    @Mock
    private SchiriRepository schiriRepository;
    @Mock
    private KategorieRepository kategorieRepository;
    @Mock
    private GruppeRepository gruppeRepository;
    @Mock
    private SpielRepository spielRepository;
    @Mock
    private SpielZeilenRepository spielZeilenRepository;
    @Mock
    private PenaltyRepository penaltyRepository;
    @Mock
    private KorrekturRepository korrekturRepository;

    @Test
    void testImportTournament_GameAlreadyExists() {
        GameModel existing = new GameModel();
        when(gameRepository.findByGameName("Existing")).thenReturn(existing);

        TournamentExportDto dto = new TournamentExportDto();

        assertThrows(IllegalArgumentException.class,
                () -> importService.importTournament(dto, "Existing"));
    }

    @Test
    void testImportTournament_EmptyTournament() {
        when(gameRepository.findByGameName("NewGame")).thenReturn(null);

        TournamentExportDto dto = new TournamentExportDto();
        dto.setOriginalGameName("OldGame");

        TournamentExportDto.GameModelDto gmDto = new TournamentExportDto.GameModelDto();
        gmDto.setOriginalId(1L);
        gmDto.setGameName("OldGame");
        gmDto.setSpielPhase("anmeldung");
        gmDto.setSpiellaenge(10);
        gmDto.setSpiellaengefinale(13);
        gmDto.setPause(2);
        dto.setGameModel(gmDto);
        dto.setMannschaften(new ArrayList<>());
        dto.setSchiris(new ArrayList<>());
        dto.setSpiele(new ArrayList<>());
        dto.setGruppen(new ArrayList<>());
        dto.setPenalties(new ArrayList<>());
        dto.setKategorien(new ArrayList<>());
        dto.setSpielzeilen(new ArrayList<>());
        dto.setKorrekturen(new ArrayList<>());

        GameModel savedGm = new GameModel();
        savedGm.setId(100L);
        when(gameRepository.save(any(GameModel.class))).thenReturn(savedGm);

        importService.importTournament(dto, "NewGame");

        verify(gameRepository).save(any(GameModel.class));
        verify(mannschaftRepository, never()).save(any(Mannschaft.class));
    }

    @Test
    void testImportTournament_WithMannschaften() {
        when(gameRepository.findByGameName("NewGame")).thenReturn(null);

        TournamentExportDto dto = new TournamentExportDto();
        dto.setOriginalGameName("OldGame");

        TournamentExportDto.GameModelDto gmDto = new TournamentExportDto.GameModelDto();
        gmDto.setOriginalId(1L);
        gmDto.setSpielPhase("anmeldung");
        dto.setGameModel(gmDto);

        TournamentExportDto.MannschaftDto mDto = new TournamentExportDto.MannschaftDto();
        mDto.setOriginalId(10L);
        mDto.setNickname("Tigers");
        mDto.setKlasse(5);
        mDto.setGeschlecht("M");
        dto.setMannschaften(List.of(mDto));

        dto.setSchiris(new ArrayList<>());
        dto.setSpiele(new ArrayList<>());
        dto.setGruppen(new ArrayList<>());
        dto.setPenalties(new ArrayList<>());
        dto.setKategorien(new ArrayList<>());
        dto.setSpielzeilen(new ArrayList<>());
        dto.setKorrekturen(new ArrayList<>());

        GameModel savedGm = new GameModel();
        savedGm.setId(100L);
        when(gameRepository.save(any(GameModel.class))).thenReturn(savedGm);

        Mannschaft savedM = new Mannschaft();
        savedM.setId(200L);
        when(mannschaftRepository.save(any(Mannschaft.class))).thenReturn(savedM);

        importService.importTournament(dto, "NewGame");

        verify(mannschaftRepository).save(argThat(m ->
                "NewGame".equals(m.getGame()) && "Tigers".equals(m.getNickname())));
    }

    @Test
    void testImportTournament_WithKorrekturen() {
        when(gameRepository.findByGameName("NewGame")).thenReturn(null);

        TournamentExportDto dto = new TournamentExportDto();
        dto.setOriginalGameName("OldGame");

        TournamentExportDto.GameModelDto gmDto = new TournamentExportDto.GameModelDto();
        gmDto.setOriginalId(1L);
        gmDto.setSpielPhase("anmeldung");
        dto.setGameModel(gmDto);

        TournamentExportDto.KorrekturDto kDto = new TournamentExportDto.KorrekturDto();
        kDto.setOriginalId(40L);
        kDto.setTyp("spielzeile");
        kDto.setWert("123");
        kDto.setReihenfolge(1);
        dto.setKorrekturen(List.of(kDto));

        dto.setMannschaften(new ArrayList<>());
        dto.setSchiris(new ArrayList<>());
        dto.setSpiele(new ArrayList<>());
        dto.setGruppen(new ArrayList<>());
        dto.setPenalties(new ArrayList<>());
        dto.setKategorien(new ArrayList<>());
        dto.setSpielzeilen(new ArrayList<>());

        GameModel savedGm = new GameModel();
        savedGm.setId(100L);
        when(gameRepository.save(any(GameModel.class))).thenReturn(savedGm);

        importService.importTournament(dto, "NewGame");

        verify(korrekturRepository).save(argThat(k ->
                "NewGame".equals(k.getGame()) && "spielzeile".equals(k.getTyp())));
    }
}
