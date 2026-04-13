package ch.plaintext.schuetu.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameMenuVisibilityProviderTest {

    @InjectMocks
    private GameMenuVisibilityProvider provider;

    @Mock
    private GameSelectionHolder gameSelectionHolder;

    @Mock
    private ApplicationContext applicationContext;

    @BeforeEach
    void setUp() {
        lenient().when(applicationContext.getBeansOfType(any())).thenReturn(Map.of());
    }

    @Test
    void testAlwaysVisibleMenus() {
        assertTrue(provider.isMenuVisible("Start"));
        assertTrue(provider.isMenuVisible("Root"));
        assertTrue(provider.isMenuVisible("Admin"));
        assertTrue(provider.isMenuVisible("Home"));
        assertTrue(provider.isMenuVisible("Experimental"));
        assertTrue(provider.isMenuVisible("Sprache"));
    }

    @Test
    void testMenuHiddenWhenNoGame() {
        when(gameSelectionHolder.hasGame()).thenReturn(false);

        assertFalse(provider.isMenuVisible("Planung"));
        assertFalse(provider.isMenuVisible("Eintragen"));
    }

    @Test
    void testMenuVisibleWhenGameSelected() {
        when(gameSelectionHolder.hasGame()).thenReturn(true);

        assertTrue(provider.isMenuVisible("Planung"));
        assertTrue(provider.isMenuVisible("Eintragen"));
    }

    @Test
    void testChildMenuExtraction() {
        // Menu with " | " separator - should extract base name
        assertTrue(provider.isMenuVisible("Start | Child"));
    }

    @Test
    void testIsMenuVisibleForMandate() {
        when(gameSelectionHolder.hasGame()).thenReturn(true);
        assertTrue(provider.isMenuVisibleForMandate("Planung", "admin"));
    }
}
