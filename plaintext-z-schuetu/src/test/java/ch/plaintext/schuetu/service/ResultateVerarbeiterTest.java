package ch.plaintext.schuetu.service;

import ch.plaintext.schuetu.entity.GameModel;
import ch.plaintext.schuetu.entity.Kategorie;
import ch.plaintext.schuetu.entity.Penalty;
import ch.plaintext.schuetu.model.ranglistensortierung.RanglisteneintragHistorie;
import ch.plaintext.schuetu.repository.KategorieRepository;
import ch.plaintext.schuetu.repository.SpielRepository;
import ch.plaintext.schuetu.service.backupsync.BackupSyncProvider;
import ch.plaintext.schuetu.service.html.HTMLOutConverter;
import ch.plaintext.schuetu.service.html.HTMLSpielMatrixConverter;
import ch.plaintext.schuetu.service.html.ModelConverterRangliste;
import ch.plaintext.schuetu.service.websiteinfo.VelocityReplacer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResultateVerarbeiterTest {

    @InjectMocks
    private ResultateVerarbeiter verarbeiter;

    @Mock
    private HTMLSpielMatrixConverter matrix;
    @Mock
    private VelocityReplacer web;
    @Mock
    private HTMLOutConverter historieGenerator;
    @Mock
    private SpielRepository spielRepo;
    @Mock
    private KategorieRepository katRepo;
    @Mock
    private ModelConverterRangliste ranglisteConverter;
    @Mock
    private BackupSyncProvider syncProvider;
    @Mock
    private PenaltyLoaderFactory penaltyLoaderFactory;
    @Mock
    private Game game;

    @BeforeEach
    void setUp() {
        GameModel model = new GameModel();
        model.setGameName("TestTurnier");
        lenient().when(game.getModel()).thenReturn(model);
        verarbeiter.setGame(game);
    }

    @Test
    void testSignalFertigesSpiel() {
        verarbeiter.signalFertigesSpiel(42L);
        verify(syncProvider).signalSpiel(42L, "TestTurnier");
        assertEquals(1, verarbeiter.getQueueSize());
    }

    @Test
    void testSignalPenalty() {
        Penalty p = new Penalty();
        verarbeiter.signalPenalty(p);
        assertEquals(1, verarbeiter.getQueueSize());
    }

    @Test
    void testGetQueueSize_EmptyQueues() {
        assertEquals(0, verarbeiter.getQueueSize());
    }

    @Test
    void testGetQueueSize_CombinedQueues() {
        verarbeiter.signalFertigesSpiel(1L);
        verarbeiter.signalFertigesSpiel(2L);
        Penalty p = new Penalty();
        verarbeiter.signalPenalty(p);

        assertEquals(3, verarbeiter.getQueueSize());
    }

    @Test
    void testIsFertig_EmptyBeendet() {
        assertFalse(verarbeiter.isFertig());
    }

    @Test
    void testIsFertig_WithPendingSpiel() {
        verarbeiter.signalFertigesSpiel(1L);
        assertFalse(verarbeiter.isFertig());
    }

    @Test
    void testIsFertig_WithPendingPenalty() {
        verarbeiter.signalPenalty(new Penalty());
        assertFalse(verarbeiter.isFertig());
    }

    @Test
    void testGetHistorie_NotExisting() {
        assertNull(verarbeiter.getHistorie("nonexistent"));
    }

    @Test
    void testGetKeys_Empty() {
        assertTrue(verarbeiter.getKeys().isEmpty());
    }

    @Test
    void testInitFertigMap() {
        Kategorie kat1 = mock(Kategorie.class);
        when(kat1.getName()).thenReturn("MKl5");
        Kategorie kat2 = mock(Kategorie.class);
        when(kat2.getName()).thenReturn("KKl4");

        when(katRepo.findByGame("TestTurnier")).thenReturn(List.of(kat1, kat2));

        verarbeiter.initFertigMap();

        // Should not throw and should populate the beendet map
        // (internal state, tested indirectly via isFertig)
        assertFalse(verarbeiter.isFertig());
    }

    @Test
    void testInitFertigMap_CalledTwice_SecondCallSkipped() {
        Kategorie kat = mock(Kategorie.class);
        when(kat.getName()).thenReturn("MKl5");
        when(katRepo.findByGame("TestTurnier")).thenReturn(List.of(kat));

        verarbeiter.initFertigMap();
        verarbeiter.initFertigMap();

        // only called once because map already populated
        verify(katRepo, times(1)).findByGame("TestTurnier");
    }

    @Test
    void testGenerateSpieleMatrix() {
        when(katRepo.findByGame("TestTurnier")).thenReturn(Collections.emptyList());
        when(matrix.generateSpieleTable(anyList())).thenReturn("<table></table>");

        String result = verarbeiter.generateSpieleMatrix();
        assertEquals("<table></table>", result);
    }

    @Test
    void testGenerateRanglistenHistorie_Null() {
        when(historieGenerator.getRangliste(null)).thenReturn("");
        String result = verarbeiter.generateRanglistenHistorieForKategorieName("nonexistent");
        assertEquals("", result);
    }

    @Test
    void testGetRanglisteModel_EmptyMap() {
        when(ranglisteConverter.convertKlassenrangZeile(anyList())).thenReturn(Collections.emptyList());
        assertTrue(verarbeiter.getRanglisteModel().isEmpty());
    }

    @Test
    void testGameConnectable() {
        assertSame(game, verarbeiter.getGame());
        Game newGame = mock(Game.class);
        verarbeiter.setGame(newGame);
        assertSame(newGame, verarbeiter.getGame());
    }
}
