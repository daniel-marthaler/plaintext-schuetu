package ch.plaintext.schuetu.service.spieldurchfuehrung;

import ch.plaintext.schuetu.entity.GameModel;
import ch.plaintext.schuetu.entity.Penalty;
import ch.plaintext.schuetu.entity.SpielZeile;
import ch.plaintext.schuetu.repository.SpielRepository;
import ch.plaintext.schuetu.repository.SpielZeilenRepository;
import ch.plaintext.schuetu.service.Game;
import ch.plaintext.schuetu.service.PenaltyLoaderFactory;
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
class SpielDurchfuehrungTest {

    @InjectMocks
    private SpielDurchfuehrung durchfuehrung;

    @Mock
    private SpielDurchfuehrungTrigger trigger;

    @Mock
    private ResultateVerarbeiter verarbeiter;

    @Mock
    private SpielZeilenRepository spielzeilenRepo;

    @Mock
    private SpielRepository spielRepo;

    @Mock
    private DurchfuehrungDataDatabase durchfuehrungData;

    @Mock
    private PenaltyLoaderFactory penalty;

    @Mock
    private Game game;

    @BeforeEach
    void setUp() {
        GameModel model = new GameModel();
        model.setGameName("TestTurnier");
        lenient().when(game.getModel()).thenReturn(model);
        durchfuehrung.setGame(game);
    }

    @Test
    void testGetPenaltyAnstehend_WithPenalty() {
        Penalty p = new Penalty();
        when(penalty.loadPenaltyAnstehend("TestTurnier")).thenReturn(p);

        List<Penalty> result = durchfuehrung.getPenaltyAnstehend();
        assertEquals(1, result.size());
        assertSame(p, result.get(0));
    }

    @Test
    void testGetPenaltyAnstehend_NoPenalty() {
        when(penalty.loadPenaltyAnstehend("TestTurnier")).thenReturn(null);

        List<Penalty> result = durchfuehrung.getPenaltyAnstehend();
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetPenaltyGespielt_WithPenalty() {
        Penalty p = new Penalty();
        when(penalty.loadPenaltyGespielt("TestTurnier")).thenReturn(p);

        List<Penalty> result = durchfuehrung.getPenaltyGespielt();
        assertEquals(1, result.size());
    }

    @Test
    void testGetPenaltyGespielt_NoPenalty() {
        when(penalty.loadPenaltyGespielt("TestTurnier")).thenReturn(null);

        List<Penalty> result = durchfuehrung.getPenaltyGespielt();
        assertTrue(result.isEmpty());
    }

    @Test
    void testSetPenaltyGespielt() {
        durchfuehrung.setPenaltyGespielt("42");
        verify(penalty).penaltyGespielt("42");
    }

    @Test
    void testGetReadyToVorbereiten_EmptyList() {
        when(durchfuehrungData.getList3Vorbereitet()).thenReturn(new ArrayList<>());
        assertTrue(durchfuehrung.getReadyToVorbereiten());
    }

    @Test
    void testGetReadyToVorbereiten_NonEmptyList() {
        List<SpielZeile> list = new ArrayList<>();
        list.add(new SpielZeile());
        when(durchfuehrungData.getList3Vorbereitet()).thenReturn(list);
        assertFalse(durchfuehrung.getReadyToVorbereiten());
    }

    @Test
    void testGetReadyToSpielen_EmptyList() {
        when(durchfuehrungData.getList4Spielend()).thenReturn(new ArrayList<>());
        assertTrue(durchfuehrung.getReadyToSpielen());
    }

    @Test
    void testGetReadyToSpielen_NonEmptyList() {
        List<SpielZeile> list = new ArrayList<>();
        list.add(new SpielZeile());
        when(durchfuehrungData.getList4Spielend()).thenReturn(list);
        assertFalse(durchfuehrung.getReadyToSpielen());
    }

    @Test
    void testDefaultValues() {
        assertEquals(3, durchfuehrung.getWartendSize());
        assertEquals(3, durchfuehrung.getMinutenZumVorbereiten());
        assertEquals("/", durchfuehrung.getDelim());
        assertFalse(durchfuehrung.isInit());
        assertEquals(0, durchfuehrung.getCount());
    }

    @Test
    void testFertigesSpiel() {
        ch.plaintext.schuetu.entity.Spiel spiel = new ch.plaintext.schuetu.entity.Spiel();
        spiel.setId(42L);

        durchfuehrung.fertigesSpiel(spiel);
        verify(verarbeiter).signalFertigesSpiel(42L);
    }

    @Test
    void testGetWait_NoCountdowns() {
        durchfuehrung.setCountdown(null);
        String wait = durchfuehrung.getWait();
        assertEquals("15000", wait);
    }

    @Test
    void testGetList2ZumVorbereiten() {
        List<SpielZeile> list = new ArrayList<>();
        when(durchfuehrungData.getList2ZumVorbereiten()).thenReturn(list);
        assertSame(list, durchfuehrung.getList2ZumVorbereiten());
    }

    @Test
    void testGetList3Vorbereitet() {
        List<SpielZeile> list = new ArrayList<>();
        when(durchfuehrungData.getList3Vorbereitet()).thenReturn(list);
        assertSame(list, durchfuehrung.getList3Vorbereitet());
    }

    @Test
    void testGetList4Spielend() {
        List<SpielZeile> list = new ArrayList<>();
        when(durchfuehrungData.getList4Spielend()).thenReturn(list);
        assertSame(list, durchfuehrung.getList4Spielend());
    }

    @Test
    void testGetList5Beendet() {
        List<SpielZeile> list = new ArrayList<>();
        when(durchfuehrungData.getList5Beendet()).thenReturn(list);
        assertSame(list, durchfuehrung.getList5Beendet());
    }
}
