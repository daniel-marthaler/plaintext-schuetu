package ch.plaintext.schuetu.service.spieldurchfuehrung;

import ch.plaintext.schuetu.entity.GameModel;
import ch.plaintext.schuetu.entity.Spiel;
import ch.plaintext.schuetu.repository.SpielRepository;
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
class EintragerServiceTest {

    @InjectMocks
    private EintragerService service;

    @Mock
    private SpielRepository spielRepository;

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
    void testFindAllEinzutragende() {
        List<Spiel> expected = List.of(new Spiel(), new Spiel());
        when(spielRepository.findAllEinzutragende("TestTurnier")).thenReturn(expected);

        List<Spiel> result = service.findAllEinzutragende();
        assertEquals(2, result.size());
        verify(spielRepository).findAllEinzutragende("TestTurnier");
    }

    @Test
    void testFindAllZuBestaetigen() {
        List<Spiel> expected = List.of(new Spiel());
        when(spielRepository.findAllZuBestaetigen("TestTurnier")).thenReturn(expected);

        List<Spiel> result = service.findAllZuBestaetigen();
        assertEquals(1, result.size());
    }

    @Test
    void testEintragen_ValidSpiel() {
        Spiel spiel = new Spiel();
        spiel.setId(42L);
        spiel.setToreA(3);
        spiel.setToreB(1);
        List<Spiel> spiele = new ArrayList<>();
        spiele.add(spiel);

        service.eintragen(spiele, "42", "TestUser");

        assertTrue(spiel.isFertigEingetragen());
        assertEquals("TestUser", spiel.getEintrager());
        verify(spielRepository).save(spiel);
    }

    @Test
    void testEintragen_InvalidId() {
        Spiel spiel = new Spiel();
        spiel.setId(42L);
        spiel.setToreA(3);
        spiel.setToreB(1);
        List<Spiel> spiele = new ArrayList<>();
        spiele.add(spiel);

        service.eintragen(spiele, "99", "TestUser");

        assertFalse(spiel.isFertigEingetragen());
        verify(spielRepository, never()).save(any());
    }

    @Test
    void testEintragen_NegativeToreA() {
        Spiel spiel = new Spiel();
        spiel.setId(42L);
        spiel.setToreA(-1);
        spiel.setToreB(1);
        List<Spiel> spiele = new ArrayList<>();
        spiele.add(spiel);

        service.eintragen(spiele, "42", "TestUser");

        assertFalse(spiel.isFertigEingetragen());
        verify(spielRepository, never()).save(any());
    }

    @Test
    void testEintragen_NegativeToreB() {
        Spiel spiel = new Spiel();
        spiel.setId(42L);
        spiel.setToreA(1);
        spiel.setToreB(-1);
        List<Spiel> spiele = new ArrayList<>();
        spiele.add(spiel);

        service.eintragen(spiele, "42", "TestUser");

        assertFalse(spiel.isFertigEingetragen());
        verify(spielRepository, never()).save(any());
    }

    @Test
    void testBestaetigen_Ok() {
        Spiel spiel = new Spiel();
        spiel.setId(42L);
        spiel.setToreA(3);
        spiel.setToreB(1);
        List<Spiel> spiele = new ArrayList<>();
        spiele.add(spiel);

        ResultateVerarbeiter resultate = mock(ResultateVerarbeiter.class);
        when(game.getResultate()).thenReturn(resultate);

        service.bestaetigen(spiele, "42", "ok");

        assertTrue(spiel.isFertigBestaetigt());
        assertEquals(3, spiel.getToreABestaetigt());
        assertEquals(1, spiel.getToreBBestaetigt());
        verify(resultate).signalFertigesSpiel(42L);
    }

    @Test
    void testBestaetigen_NotOk() {
        Spiel spiel = new Spiel();
        spiel.setId(42L);
        spiel.setToreA(3);
        spiel.setToreB(1);
        spiel.setFertigEingetragen(true);
        List<Spiel> spiele = new ArrayList<>();
        spiele.add(spiel);

        service.bestaetigen(spiele, "42", "nok");

        assertFalse(spiel.isFertigBestaetigt());
        assertTrue(spiel.isZurueckgewiesen());
        assertFalse(spiel.isFertigEingetragen());
    }

    @Test
    void testBestaetigen_WrongId() {
        Spiel spiel = new Spiel();
        spiel.setId(42L);
        List<Spiel> spiele = new ArrayList<>();
        spiele.add(spiel);

        service.bestaetigen(spiele, "99", "ok");

        assertFalse(spiel.isFertigBestaetigt());
        verify(spielRepository, never()).save(any());
    }
}
