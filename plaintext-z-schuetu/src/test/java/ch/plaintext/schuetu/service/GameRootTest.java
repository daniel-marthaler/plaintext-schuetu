package ch.plaintext.schuetu.service;

import ch.plaintext.schuetu.entity.GameModel;
import ch.plaintext.schuetu.repository.GameRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameRootTest {

    @InjectMocks
    private GameRoot gameRoot;

    @Mock
    private GameRepository repo;

    @Mock
    private ApplicationContext ctx;

    @Test
    void testDisplayGames() {
        GameModel g1 = new GameModel();
        g1.setGameName("T1");
        GameModel g2 = new GameModel();
        g2.setGameName("T2");

        when(repo.findAll()).thenReturn(List.of(g1, g2));

        List<GameModel> result = gameRoot.displayGames();
        assertEquals(2, result.size());
    }

    @Test
    void testDisplayGames_Empty() {
        when(repo.findAll()).thenReturn(List.of());
        assertTrue(gameRoot.displayGames().isEmpty());
    }

    @Test
    void testSelectGame_NotFound() {
        when(repo.findByGameName("unknown")).thenReturn(null);

        Game result = gameRoot.selectGame("unknown");
        assertNull(result);
    }

    @Test
    void testSelectGame_Found() {
        GameModel model = new GameModel();
        model.setGameName("Test");
        when(repo.findByGameName("Test")).thenReturn(model);

        Game game = mock(Game.class);
        when(ctx.getBean(Game.class)).thenReturn(game);
        when(repo.save(model)).thenReturn(model);

        Game result = gameRoot.selectGame("Test");
        assertNotNull(result);
        verify(game).setModel(model);
        verify(game).init();
    }

    @Test
    void testSelectGame_Cached() {
        GameModel model = new GameModel();
        model.setGameName("Cached");
        when(repo.findByGameName("Cached")).thenReturn(model);

        Game game = mock(Game.class);
        when(ctx.getBean(Game.class)).thenReturn(game);
        when(repo.save(model)).thenReturn(model);

        // First call
        gameRoot.selectGame("Cached");
        // Second call - should use cache
        Game result = gameRoot.selectGame("Cached");

        assertNotNull(result);
        // Game.class bean should only be requested once
        verify(ctx, times(1)).getBean(Game.class);
    }

    @Test
    void testClearCache() {
        gameRoot.clearCache();
        assertTrue(gameRoot.getGameCache().isEmpty());
    }
}
