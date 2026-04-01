package ch.plaintext.schuetu.service;

import ch.plaintext.schuetu.entity.GameModel;
import ch.plaintext.schuetu.repository.GameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameBackingBeanTest {

    @InjectMocks
    private GameBackingBean bean;

    @Mock
    private GameRoot root;

    @Mock
    private GameRepository repo;

    // === displayGames ===

    @Test
    void testDisplayGames_Empty() {
        when(root.displayGames()).thenReturn(Collections.emptyList());
        List<GameModel> result = bean.displayGames();
        assertTrue(result.isEmpty());
    }

    @Test
    void testDisplayGames_WithGames() {
        GameModel g1 = new GameModel();
        g1.setGameName("Turnier2025");
        GameModel g2 = new GameModel();
        g2.setGameName("Turnier2026");
        when(root.displayGames()).thenReturn(Arrays.asList(g1, g2));

        List<GameModel> result = bean.displayGames();
        assertEquals(2, result.size());
    }

    @Test
    void testDisplayGames_DelegatesToRoot() {
        when(root.displayGames()).thenReturn(Collections.emptyList());
        bean.displayGames();
        verify(root, times(1)).displayGames();
    }

    // === save ===

    @Test
    void testSave_SavesSelected() {
        GameModel model = new GameModel();
        model.setGameName("Test");
        bean.setSelected(model);

        bean.save();
        verify(repo, times(1)).save(model);
    }

    @Test
    void testSave_ResetsSelected() {
        GameModel model = new GameModel();
        model.setGameName("Test");
        bean.setSelected(model);

        bean.save();
        // After save, selected is reset to a new GameModel
        assertNotSame(model, bean.getSelected());
        assertNotNull(bean.getSelected());
    }

    // === selected ===

    @Test
    void testDefaultSelected() {
        assertNotNull(bean.getSelected());
    }

    @Test
    void testSetSelected() {
        GameModel model = new GameModel();
        model.setGameName("Custom");
        bean.setSelected(model);
        assertEquals("Custom", bean.getSelected().getGameName());
    }
}
