package ch.plaintext.schuetu.web;

import ch.plaintext.schuetu.entity.Spiel;
import ch.plaintext.schuetu.model.enums.PlatzEnum;
import ch.plaintext.schuetu.repository.SpielRepository;
import ch.plaintext.schuetu.service.GameSelectionHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlatzClockBeanTest {

    @Mock
    private SpielRepository spielRepository;

    @Mock
    private GameSelectionHolder holder;

    private static final String GAME = "TEST";

    private PlatzClockBean bean;

    @BeforeEach
    void setUp() {
        lenient().when(holder.hasGame()).thenReturn(true);
        lenient().when(holder.getGameName()).thenReturn(GAME);
        bean = new PlatzClockBean() {
            @Override
            String readParam(String name) {
                return "A";
            }
        };
        injectMocks();
        bean.init();
    }

    private void injectMocks() {
        try {
            java.lang.reflect.Field f1 = PlatzClockBean.class.getDeclaredField("spielRepository");
            f1.setAccessible(true);
            f1.set(bean, spielRepository);
            java.lang.reflect.Field f2 = PlatzClockBean.class.getDeclaredField("holder");
            f2.setAccessible(true);
            f2.set(bean, holder);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Spiel aktivesSpielAufA(int toreA, int toreB) {
        Spiel s = new Spiel();
        s.setPlatz(PlatzEnum.A);
        s.setAmSpielen(true);
        s.setToreA(toreA);
        s.setToreB(toreB);
        return s;
    }

    @Test
    void testInit_LiestPlatzAusParam() {
        assertEquals(PlatzEnum.A, bean.getPlatz());
        assertEquals("A", bean.getPlatzName());
    }

    @Test
    void testGetAktuellesSpiel_NurAmSpielenUndPlatzPasst() {
        Spiel spielA = aktivesSpielAufA(2, 1);
        Spiel spielB = new Spiel();
        spielB.setPlatz(PlatzEnum.B);
        spielB.setAmSpielen(true);
        Spiel ruhigA = new Spiel();
        ruhigA.setPlatz(PlatzEnum.A);
        ruhigA.setAmSpielen(false);

        when(spielRepository.findByGame(GAME)).thenReturn(List.of(spielB, ruhigA, spielA));

        assertEquals(spielA, bean.getAktuellesSpiel());
    }

    @Test
    void testIncTeamA_ErhoehtUndSpeichert() {
        Spiel s = aktivesSpielAufA(2, 1);
        when(spielRepository.findByGame(GAME)).thenReturn(List.of(s));

        bean.incTeamA();

        assertEquals(3, s.getToreA());
        verify(spielRepository).save(s);
    }

    @Test
    void testIncTeamA_CapBeiMaxTore30() {
        Spiel s = aktivesSpielAufA(30, 5);
        when(spielRepository.findByGame(GAME)).thenReturn(List.of(s));

        bean.incTeamA();

        assertEquals(30, s.getToreA());
        verify(spielRepository).save(s);
    }

    @Test
    void testDecTeamA_MinBei0() {
        Spiel s = aktivesSpielAufA(0, 0);
        when(spielRepository.findByGame(GAME)).thenReturn(List.of(s));

        bean.decTeamA();

        assertEquals(0, s.getToreA());
        verify(spielRepository).save(s);
    }

    @Test
    void testIncTeamB_AusNotInitFlagWird1() {
        Spiel s = aktivesSpielAufA(0, -1);
        when(spielRepository.findByGame(GAME)).thenReturn(List.of(s));

        bean.incTeamB();

        assertEquals(1, s.getToreB());
        verify(spielRepository).save(s);
    }

    @Test
    void testBeenden_SetztFlagsUndSpeichert() {
        Spiel s = aktivesSpielAufA(3, 2);
        when(spielRepository.findByGame(GAME)).thenReturn(List.of(s));

        bean.beenden();

        // amSpielen is set to false; spiel is fertig
        verify(spielRepository, atLeastOnce()).save(s);
    }

    @Test
    void testIncTeamA_OhneAktivesSpiel_TutNichts() {
        when(spielRepository.findByGame(GAME)).thenReturn(List.of());

        bean.incTeamA();

        verify(spielRepository, never()).save(any());
    }

    @Test
    void testPhaseText_OhneSpiel() {
        when(spielRepository.findByGame(GAME)).thenReturn(List.of());
        assertEquals("Warte auf Spielstart", bean.getPhaseText());
    }

    @Test
    void testPhaseText_MitLaufendemSpiel() {
        when(spielRepository.findByGame(GAME)).thenReturn(List.of(aktivesSpielAufA(0, 0)));
        assertEquals("Läuft", bean.getPhaseText());
    }

    @Test
    void testDisplayTore_OhneSpiel0() {
        when(spielRepository.findByGame(GAME)).thenReturn(List.of());
        assertEquals(0, bean.getDisplayToreA());
        assertEquals(0, bean.getDisplayToreB());
    }

    @Test
    void testDisplayTore_AusNotInitFlagWird0() {
        when(spielRepository.findByGame(GAME)).thenReturn(List.of(aktivesSpielAufA(-1, -1)));
        assertEquals(0, bean.getDisplayToreA());
        assertEquals(0, bean.getDisplayToreB());
    }
}
