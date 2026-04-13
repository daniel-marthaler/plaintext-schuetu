package ch.plaintext.schuetu.service.spieldurchfuehrung;

import ch.plaintext.schuetu.entity.GameModel;
import ch.plaintext.schuetu.entity.Penalty;
import ch.plaintext.schuetu.repository.PenaltyRepository;
import ch.plaintext.schuetu.service.Game;
import ch.plaintext.schuetu.service.ResultateVerarbeiter;
import org.junit.jupiter.api.BeforeEach;
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
class PenaltyServiceTest {

    @InjectMocks
    private PenaltyService service;

    @Mock
    private PenaltyRepository penaltyRepo;

    @Mock
    private Game game;

    @BeforeEach
    void setUp() {
        GameModel model = new GameModel();
        model.setGameName("TestTurnier");
        lenient().when(game.getModel()).thenReturn(model);
        service.setGame(game);
    }

    @Test
    void testAnstehendePenalty_ReturnsNotBestaetigtAndNotGespielt() {
        Penalty p1 = new Penalty();
        p1.setBestaetigt(false);
        p1.setGespielt(false);

        Penalty p2 = new Penalty();
        p2.setBestaetigt(true);
        p2.setGespielt(true);

        Penalty p3 = new Penalty();
        p3.setBestaetigt(false);
        p3.setGespielt(true);

        when(penaltyRepo.findByGame("TestTurnier")).thenReturn(List.of(p1, p2, p3));

        List<Penalty> result = service.anstehendePenalty();
        assertEquals(1, result.size());
        assertSame(p1, result.get(0));
    }

    @Test
    void testAnstehendePenalty_Empty() {
        when(penaltyRepo.findByGame("TestTurnier")).thenReturn(List.of());

        List<Penalty> result = service.anstehendePenalty();
        assertTrue(result.isEmpty());
    }

    @Test
    void testGespieltePenalty_ReturnsFirstGespieltNotBestaetigt() {
        Penalty p1 = new Penalty();
        p1.setBestaetigt(false);
        p1.setGespielt(true);

        Penalty p2 = new Penalty();
        p2.setBestaetigt(false);
        p2.setGespielt(true);

        when(penaltyRepo.findByGame("TestTurnier")).thenReturn(List.of(p1, p2));

        List<Penalty> result = service.gespieltePenalty();
        assertEquals(1, result.size());
        assertSame(p1, result.get(0));
    }

    @Test
    void testGespieltePenalty_NoneGespielt() {
        Penalty p1 = new Penalty();
        p1.setBestaetigt(false);
        p1.setGespielt(false);

        when(penaltyRepo.findByGame("TestTurnier")).thenReturn(List.of(p1));

        List<Penalty> result = service.gespieltePenalty();
        assertTrue(result.isEmpty());
    }

    @Test
    void testGespieltePenalty_AllBestaetigt() {
        Penalty p1 = new Penalty();
        p1.setBestaetigt(true);
        p1.setGespielt(true);

        when(penaltyRepo.findByGame("TestTurnier")).thenReturn(List.of(p1));

        List<Penalty> result = service.gespieltePenalty();
        assertTrue(result.isEmpty());
    }

    @Test
    void testEingetragenePenalty_OnlyBestaetigtAndGespielt() {
        Penalty p1 = new Penalty();
        p1.setBestaetigt(true);
        p1.setGespielt(true);

        Penalty p2 = new Penalty();
        p2.setBestaetigt(false);
        p2.setGespielt(true);

        Penalty p3 = new Penalty();
        p3.setBestaetigt(true);
        p3.setGespielt(false);

        when(penaltyRepo.findByGame("TestTurnier")).thenReturn(List.of(p1, p2, p3));

        List<Penalty> result = service.eingetragenePenalty();
        assertEquals(1, result.size());
        assertSame(p1, result.get(0));
    }

    @Test
    void testPenaltyEintragen_ValidReihenfolge() {
        Penalty p = new Penalty();
        p.setReihenfolge("Team1,Team2");
        p.setBestaetigt(false);
        p.setGespielt(false);

        ResultateVerarbeiter resultate = mock(ResultateVerarbeiter.class);
        when(game.getResultate()).thenReturn(resultate);
        when(penaltyRepo.save(any(Penalty.class))).thenReturn(p);

        service.penaltyEintragen(List.of(p));

        assertTrue(p.isGespielt());
        assertTrue(p.isBestaetigt());
        assertEquals("TestTurnier", p.getGame());
        verify(resultate).signalPenalty(p);
    }

    @Test
    void testPenaltyEintragen_SkipsAlreadyBestaetigt() {
        Penalty p = new Penalty();
        p.setReihenfolge("Team1,Team2");
        p.setBestaetigt(true);
        p.setGespielt(true);

        service.penaltyEintragen(List.of(p));

        verify(penaltyRepo, never()).save(any());
    }

    @Test
    void testPenaltyEintragen_SkipsLeerReihenfolge() {
        Penalty p = new Penalty();
        p.setReihenfolge(Penalty.LEER);
        p.setBestaetigt(false);
        p.setGespielt(false);

        service.penaltyEintragen(List.of(p));

        verify(penaltyRepo, never()).save(any());
    }

    @Test
    void testPenaltyEintragen_SkipsNullReihenfolge() {
        Penalty p = new Penalty();
        p.setReihenfolge(null);

        service.penaltyEintragen(List.of(p));

        verify(penaltyRepo, never()).save(any());
    }

    @Test
    void testPenaltyEintragen_SkipsEmptyReihenfolge() {
        Penalty p = new Penalty();
        p.setReihenfolge("");

        service.penaltyEintragen(List.of(p));

        verify(penaltyRepo, never()).save(any());
    }
}
