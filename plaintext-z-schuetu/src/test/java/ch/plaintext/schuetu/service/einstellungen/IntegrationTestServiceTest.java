package ch.plaintext.schuetu.service.einstellungen;

import ch.plaintext.schuetu.entity.Mannschaft;
import ch.plaintext.schuetu.entity.Spiel;
import ch.plaintext.schuetu.repository.SpielRepository;
import ch.plaintext.schuetu.service.ResultateVerarbeiter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IntegrationTestServiceTest {

    @InjectMocks
    private IntegrationTestService service;

    @Mock
    private SpielRepository spielRepository;

    // --- findAllEinzutragende ---

    @Test
    void testFindAllEinzutragende() {
        List<Spiel> expected = List.of(new Spiel(), new Spiel());
        when(spielRepository.findAllEinzutragende("TestTurnier")).thenReturn(expected);

        List<Spiel> result = service.findAllEinzutragende("TestTurnier");

        assertEquals(2, result.size());
        verify(spielRepository).findAllEinzutragende("TestTurnier");
    }

    @Test
    void testFindAllEinzutragende_Empty() {
        when(spielRepository.findAllEinzutragende("Empty")).thenReturn(List.of());

        List<Spiel> result = service.findAllEinzutragende("Empty");

        assertTrue(result.isEmpty());
    }

    // --- findAllZuBestaetigen ---

    @Test
    void testFindAllZuBestaetigen() {
        List<Spiel> expected = List.of(new Spiel());
        when(spielRepository.findAllZuBestaetigen("TestTurnier")).thenReturn(expected);

        List<Spiel> result = service.findAllZuBestaetigen("TestTurnier");

        assertEquals(1, result.size());
        verify(spielRepository).findAllZuBestaetigen("TestTurnier");
    }

    // --- autoEintragen ---

    @Test
    void testAutoEintragen_ValidSpiel() {
        Spiel spiel = new Spiel();
        spiel.setIdString("S01");
        Mannschaft mannA = new Mannschaft();
        mannA.setTeamNummer(13);
        Mannschaft mannB = new Mannschaft();
        mannB.setTeamNummer(27);
        spiel.setMannschaftA(mannA);
        spiel.setMannschaftB(mannB);

        when(spielRepository.findById(1L)).thenReturn(Optional.of(spiel));

        String result = service.autoEintragen(1L);

        assertNotNull(result);
        assertTrue(spiel.isFertigEingetragen());
        assertEquals(3, spiel.getToreA()); // 13 % 10
        assertEquals(7, spiel.getToreB()); // 27 % 10
        assertEquals("Auto-Schiri", spiel.getEintrager());
        assertEquals("Auto-Schiri", spiel.getSchiriName());
        verify(spielRepository).save(spiel);
    }

    @Test
    void testAutoEintragen_SpielNotFound() {
        when(spielRepository.findById(99L)).thenReturn(Optional.empty());

        String result = service.autoEintragen(99L);

        assertNull(result);
        verify(spielRepository, never()).save(any());
    }

    @Test
    void testAutoEintragen_AlreadyEingetragen() {
        Spiel spiel = new Spiel();
        spiel.setFertigEingetragen(true);
        when(spielRepository.findById(1L)).thenReturn(Optional.of(spiel));

        String result = service.autoEintragen(1L);

        assertNull(result);
        verify(spielRepository, never()).save(any());
    }

    @Test
    void testAutoEintragen_MannschaftANull() {
        Spiel spiel = new Spiel();
        spiel.setMannschaftA(null);
        Mannschaft mannB = new Mannschaft();
        mannB.setTeamNummer(5);
        spiel.setMannschaftB(mannB);

        when(spielRepository.findById(1L)).thenReturn(Optional.of(spiel));

        String result = service.autoEintragen(1L);

        assertNull(result);
        verify(spielRepository, never()).save(any());
    }

    @Test
    void testAutoEintragen_MannschaftBNull() {
        Spiel spiel = new Spiel();
        Mannschaft mannA = new Mannschaft();
        mannA.setTeamNummer(5);
        spiel.setMannschaftA(mannA);
        spiel.setMannschaftB(null);

        when(spielRepository.findById(1L)).thenReturn(Optional.of(spiel));

        String result = service.autoEintragen(1L);

        assertNull(result);
        verify(spielRepository, never()).save(any());
    }

    @Test
    void testAutoEintragen_BothMannschaftenNull() {
        Spiel spiel = new Spiel();
        spiel.setMannschaftA(null);
        spiel.setMannschaftB(null);

        when(spielRepository.findById(1L)).thenReturn(Optional.of(spiel));

        String result = service.autoEintragen(1L);

        assertNull(result);
        verify(spielRepository, never()).save(any());
    }

    @Test
    void testAutoEintragen_ResultString() {
        Spiel spiel = new Spiel();
        spiel.setIdString("G05");
        Mannschaft mannA = new Mannschaft();
        mannA.setTeamNummer(10); // 10 % 10 = 0
        Mannschaft mannB = new Mannschaft();
        mannB.setTeamNummer(20); // 20 % 10 = 0
        spiel.setMannschaftA(mannA);
        spiel.setMannschaftB(mannB);

        when(spielRepository.findById(2L)).thenReturn(Optional.of(spiel));

        String result = service.autoEintragen(2L);

        assertNotNull(result);
        assertTrue(result.startsWith("G05"));
        assertTrue(result.contains("0:0"));
        assertEquals(0, spiel.getToreA());
        assertEquals(0, spiel.getToreB());
    }

    // --- autoBestaetigen ---

    @Test
    void testAutoBestaetigen_ValidSpiel() {
        Spiel spiel = new Spiel();
        spiel.setIdString("S01");
        spiel.setToreA(3);
        spiel.setToreB(1);
        Mannschaft mannA = new Mannschaft();
        mannA.setTeamNummer(13);
        Mannschaft mannB = new Mannschaft();
        mannB.setTeamNummer(27);
        spiel.setMannschaftA(mannA);
        spiel.setMannschaftB(mannB);

        ResultateVerarbeiter resultate = mock(ResultateVerarbeiter.class);
        when(spielRepository.findById(1L)).thenReturn(Optional.of(spiel));

        String result = service.autoBestaetigen(1L, resultate);

        assertNotNull(result);
        assertTrue(spiel.isFertigBestaetigt());
        assertEquals(3, spiel.getToreABestaetigt());
        assertEquals(1, spiel.getToreBBestaetigt());
        assertEquals("Auto-Kontrolleur", spiel.getKontrolle());
        verify(spielRepository).save(spiel);
        verify(resultate).signalFertigesSpiel(1L);
    }

    @Test
    void testAutoBestaetigen_SpielNotFound() {
        ResultateVerarbeiter resultate = mock(ResultateVerarbeiter.class);
        when(spielRepository.findById(99L)).thenReturn(Optional.empty());

        String result = service.autoBestaetigen(99L, resultate);

        assertNull(result);
        verify(spielRepository, never()).save(any());
        verify(resultate, never()).signalFertigesSpiel(anyLong());
    }

    @Test
    void testAutoBestaetigen_AlreadyBestaetigt() {
        Spiel spiel = new Spiel();
        spiel.setFertigBestaetigt(true);
        ResultateVerarbeiter resultate = mock(ResultateVerarbeiter.class);
        when(spielRepository.findById(1L)).thenReturn(Optional.of(spiel));

        String result = service.autoBestaetigen(1L, resultate);

        assertNull(result);
        verify(spielRepository, never()).save(any());
        verify(resultate, never()).signalFertigesSpiel(anyLong());
    }

    @Test
    void testAutoBestaetigen_MannschaftenNull_FallbackToQuestionmark() {
        Spiel spiel = new Spiel();
        spiel.setIdString("X01");
        spiel.setToreA(2);
        spiel.setToreB(4);
        spiel.setMannschaftA(null);
        spiel.setMannschaftB(null);

        ResultateVerarbeiter resultate = mock(ResultateVerarbeiter.class);
        when(spielRepository.findById(5L)).thenReturn(Optional.of(spiel));

        String result = service.autoBestaetigen(5L, resultate);

        assertNotNull(result);
        assertTrue(result.contains("?"));
        assertTrue(result.contains("2:4"));
        verify(resultate).signalFertigesSpiel(5L);
    }

    @Test
    void testAutoBestaetigen_ResultString() {
        Spiel spiel = new Spiel();
        spiel.setIdString("F02");
        spiel.setToreA(5);
        spiel.setToreB(0);
        Mannschaft mannA = new Mannschaft();
        mannA.setTeamNummer(11);
        Mannschaft mannB = new Mannschaft();
        mannB.setTeamNummer(22);
        spiel.setMannschaftA(mannA);
        spiel.setMannschaftB(mannB);

        ResultateVerarbeiter resultate = mock(ResultateVerarbeiter.class);
        when(spielRepository.findById(3L)).thenReturn(Optional.of(spiel));

        String result = service.autoBestaetigen(3L, resultate);

        assertNotNull(result);
        assertTrue(result.startsWith("F02"));
        assertTrue(result.contains("5:0"));
    }
}
