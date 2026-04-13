package ch.plaintext.schuetu.service;

import ch.plaintext.schuetu.entity.Spiel;
import ch.plaintext.schuetu.entity.SpielZeile;
import ch.plaintext.schuetu.model.enums.PlatzEnum;
import ch.plaintext.schuetu.repository.SpielRepository;
import ch.plaintext.schuetu.repository.SpielZeilenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpielzeilenServiceTest {

    @InjectMocks
    private SpielzeilenService service;

    @Mock
    private GameSelectionHolder gameHolder;

    @Mock
    private SpielRepository spielRepository;

    @Mock
    private SpielZeilenRepository spielZeilenRepository;

    @Test
    void testSpielZeitenAnpassen_EmptyList() {
        when(gameHolder.getGameName()).thenReturn("TestGame");
        when(spielZeilenRepository.findByGame("TestGame")).thenReturn(List.of());

        service.spielZeitenAnpassen();

        verify(spielRepository, never()).save(any());
    }

    @Test
    void testSpielZeitenAnpassen_WithSpiele() {
        when(gameHolder.getGameName()).thenReturn("TestGame");

        Date start = new Date();
        Spiel spielA = new Spiel();
        spielA.setId(1L);

        SpielZeile zeile = new SpielZeile();
        zeile.setStart(start);
        zeile.setA(spielA);

        when(spielZeilenRepository.findByGame("TestGame")).thenReturn(List.of(zeile));

        service.spielZeitenAnpassen();

        verify(spielRepository).save(argThat(s ->
                s.getStart().equals(start) && s.getPlatz() == PlatzEnum.A));
    }

    @Test
    void testSpielZeitenAnpassen_SkipsPlatzhalter() {
        when(gameHolder.getGameName()).thenReturn("TestGame");

        SpielZeile zeile = new SpielZeile();
        zeile.setStart(new Date());
        // a, b, c are null, getA() returns platzhalter

        when(spielZeilenRepository.findByGame("TestGame")).thenReturn(List.of(zeile));

        service.spielZeitenAnpassen();

        verify(spielRepository, never()).save(any());
    }

    @Test
    void testSpielZeitenAnpassen_AllPlatzTypes() {
        when(gameHolder.getGameName()).thenReturn("TestGame");

        Date start = new Date();
        Spiel spielA = new Spiel();
        spielA.setId(1L);
        Spiel spielB = new Spiel();
        spielB.setId(2L);
        Spiel spielC = new Spiel();
        spielC.setId(3L);
        Spiel spielD = new Spiel();
        spielD.setId(4L);

        SpielZeile zeile = new SpielZeile();
        zeile.setStart(start);
        zeile.setA(spielA);
        zeile.setB(spielB);
        zeile.setC(spielC);
        zeile.setD(spielD);

        when(spielZeilenRepository.findByGame("TestGame")).thenReturn(List.of(zeile));

        service.spielZeitenAnpassen();

        verify(spielRepository, times(4)).save(any(Spiel.class));
    }
}
