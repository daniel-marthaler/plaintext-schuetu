package ch.plaintext.schuetu.service;

import ch.plaintext.schuetu.entity.GameModel;
import ch.plaintext.schuetu.repository.GameRepository;
import ch.plaintext.schuetu.service.mqtt.MqttEventPublisher;
import ch.plaintext.schuetu.service.websiteinfo.VelocityReplacer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameSelectionHolderTest {

    @InjectMocks
    private GameSelectionHolder holder;

    @Mock
    private GameRoot root;

    @Mock
    private GameRepository repo;

    @Mock
    private VelocityReplacer velocity;

    @Mock
    private MqttEventPublisher mqttEventPublisher;

    @Mock
    private VelocityReplacer website;

    @Mock
    private SpielzeilenService spielZeitenAnpassen;

    @BeforeEach
    void setUp() {
        // game is null by default
    }

    // === hasGame ===

    @Test
    void testHasGame_NoGame() {
        assertFalse(holder.hasGame());
    }

    @Test
    void testHasGame_WithGame() {
        Game game = mock(Game.class);
        holder.setGame(game);
        assertTrue(holder.hasGame());
    }

    // === getGameName ===

    @Test
    void testGetGameName_NoGame() {
        assertEquals("", holder.getGameName());
    }

    @Test
    void testGetGameName_WithGame() {
        Game game = mock(Game.class);
        GameModel model = new GameModel();
        model.setGameName("Turnier2026");
        when(game.getModel()).thenReturn(model);
        holder.setGame(game);

        assertEquals("Turnier2026", holder.getGameName());
    }

    // === isAnmeldung ===

    @Test
    void testIsAnmeldung_NoGame() {
        assertFalse(holder.isAnmeldung());
    }

    @Test
    void testIsAnmeldung_PhaseAnmeldung() {
        Game game = mock(Game.class);
        GameModel model = new GameModel();
        model.setSpielPhase("anmeldung");
        when(game.getModel()).thenReturn(model);
        holder.setGame(game);

        assertTrue(holder.isAnmeldung());
    }

    @Test
    void testIsAnmeldung_PhaseNotAnmeldung() {
        Game game = mock(Game.class);
        GameModel model = new GameModel();
        model.setSpielPhase("spielen");
        when(game.getModel()).thenReturn(model);
        holder.setGame(game);

        assertFalse(holder.isAnmeldung());
    }

    // === selectGame ===

    @Test
    void testSelectGame_GameNotFound() {
        when(root.selectGame("unknown")).thenReturn(null);

        String result = holder.selectGame("unknown");
        assertEquals("dashboard.htm", result);
        assertNull(holder.getGame());
    }

    @Test
    void testSelectGame_GameFound() {
        Game game = mock(Game.class);
        GameModel model = new GameModel();
        model.setGameName("Turnier2026");
        when(game.getModel()).thenReturn(model);
        when(root.selectGame("Turnier2026")).thenReturn(game);

        String result = holder.selectGame("Turnier2026");
        assertEquals("dashboard.htm", result);
        assertNotNull(holder.getGame());
    }
}
