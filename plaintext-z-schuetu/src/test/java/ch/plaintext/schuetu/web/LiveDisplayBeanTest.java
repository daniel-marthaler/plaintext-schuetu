package ch.plaintext.schuetu.web;

import ch.plaintext.schuetu.entity.Spiel;
import ch.plaintext.schuetu.model.enums.PlatzEnum;
import ch.plaintext.schuetu.repository.SpielRepository;
import ch.plaintext.schuetu.service.GameSelectionHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LiveDisplayBeanTest {

    @InjectMocks
    private LiveDisplayBean bean;

    @Mock
    private SpielRepository spielRepository;

    @Mock
    private GameSelectionHolder holder;

    private static final String GAME = "TEST";

    @BeforeEach
    void setUp() {
        lenient().when(holder.hasGame()).thenReturn(true);
        lenient().when(holder.getGameName()).thenReturn(GAME);
    }

    private Spiel spiel(boolean amSpielen, boolean fertig, Date start, PlatzEnum platz) {
        Spiel s = new Spiel();
        s.setAmSpielen(amSpielen);
        s.setFertigGespielt(fertig);
        s.setStart(start);
        s.setPlatz(platz);
        return s;
    }

    @Test
    void testSpielendeSpiele_FiltertAmSpielen() {
        Spiel laufend1 = spiel(true, false, new Date(), PlatzEnum.A);
        Spiel laufend2 = spiel(true, false, new Date(), PlatzEnum.B);
        Spiel wartend = spiel(false, false, new Date(), PlatzEnum.C);
        when(spielRepository.findByGame(GAME)).thenReturn(List.of(laufend1, wartend, laufend2));

        List<Spiel> result = bean.getSpielendeSpiele();

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(Spiel::isAmSpielen));
    }

    @Test
    void testSpielendeSpiele_KeinGame() {
        when(holder.hasGame()).thenReturn(false);
        assertTrue(bean.getSpielendeSpiele().isEmpty());
    }

    @Test
    void testNaechsteSpiele_NurZukunft_SortiertNachStart_Limit4() {
        long now = System.currentTimeMillis();
        Spiel inPast = spiel(false, false, new Date(now - 60_000), PlatzEnum.A);
        Spiel in10 = spiel(false, false, new Date(now + 10 * 60_000), PlatzEnum.A);
        Spiel in5 = spiel(false, false, new Date(now + 5 * 60_000), PlatzEnum.B);
        Spiel in20 = spiel(false, false, new Date(now + 20 * 60_000), PlatzEnum.C);
        Spiel in30 = spiel(false, false, new Date(now + 30 * 60_000), PlatzEnum.D);
        Spiel in40 = spiel(false, false, new Date(now + 40 * 60_000), PlatzEnum.A);
        Spiel in50 = spiel(false, false, new Date(now + 50 * 60_000), PlatzEnum.B);
        Spiel laufend = spiel(true, false, new Date(now + 7 * 60_000), PlatzEnum.B);

        when(spielRepository.findByGame(GAME))
                .thenReturn(List.of(inPast, in10, in5, in20, in30, in40, in50, laufend));

        List<Spiel> result = bean.getNaechsteSpiele();

        assertEquals(4, result.size(), "Maximal 4 Spiele");
        // sortiert: in5, in10, in20, in30
        assertEquals(in5, result.get(0));
        assertEquals(in10, result.get(1));
        assertEquals(in20, result.get(2));
        assertEquals(in30, result.get(3));
        assertFalse(result.contains(inPast), "Spiele in der Vergangenheit raus");
        assertFalse(result.contains(laufend), "Laufende Spiele raus");
    }

    @Test
    void testUhrzeit_FormatHHmm() {
        String uhrzeit = bean.getUhrzeit();
        assertNotNull(uhrzeit);
        assertTrue(uhrzeit.matches("\\d{2}:\\d{2}"), "Format HH:mm erwartet, war: " + uhrzeit);
    }

    @Test
    void testMaxNaechsteSpiele_Ist4() {
        assertEquals(4, bean.getMaxNaechsteSpiele());
    }
}
