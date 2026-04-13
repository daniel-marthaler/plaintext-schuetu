package ch.plaintext.schuetu.service;

import ch.plaintext.schuetu.entity.Penalty;
import ch.plaintext.schuetu.repository.PenaltyRepository;
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
class PenaltyLoaderFactoryTest {

    @InjectMocks
    private PenaltyLoaderFactory factory;

    @Mock
    private PenaltyRepository penaltyRepo;

    @Test
    void testLoadPenaltyAnstehend_Found() {
        Penalty p1 = new Penalty();
        p1.setGespielt(true);
        Penalty p2 = new Penalty();
        p2.setGespielt(false);

        when(penaltyRepo.findByGame("TestGame")).thenReturn(List.of(p1, p2));

        Penalty result = factory.loadPenaltyAnstehend("TestGame");
        assertSame(p2, result);
    }

    @Test
    void testLoadPenaltyAnstehend_AllGespielt() {
        Penalty p1 = new Penalty();
        p1.setGespielt(true);

        when(penaltyRepo.findByGame("TestGame")).thenReturn(List.of(p1));

        Penalty result = factory.loadPenaltyAnstehend("TestGame");
        assertNull(result);
    }

    @Test
    void testLoadPenaltyAnstehend_Empty() {
        when(penaltyRepo.findByGame("TestGame")).thenReturn(List.of());

        Penalty result = factory.loadPenaltyAnstehend("TestGame");
        assertNull(result);
    }

    @Test
    void testLoadPenaltyGespielt_Found() {
        Penalty p = new Penalty();
        p.setGespielt(true);
        p.setBestaetigt(false);

        when(penaltyRepo.findByGame("TestGame")).thenReturn(List.of(p));

        Penalty result = factory.loadPenaltyGespielt("TestGame");
        assertSame(p, result);
    }

    @Test
    void testLoadPenaltyGespielt_AllBestaetigt() {
        Penalty p = new Penalty();
        p.setGespielt(true);
        p.setBestaetigt(true);

        when(penaltyRepo.findByGame("TestGame")).thenReturn(List.of(p));

        Penalty result = factory.loadPenaltyGespielt("TestGame");
        assertNull(result);
    }

    @Test
    void testLoadPenaltyGespielt_NoneGespielt() {
        Penalty p = new Penalty();
        p.setGespielt(false);

        when(penaltyRepo.findByGame("TestGame")).thenReturn(List.of(p));

        Penalty result = factory.loadPenaltyGespielt("TestGame");
        assertNull(result);
    }

    @Test
    void testPenaltyGespielt() {
        Penalty p = new Penalty();
        p.setId(42L);
        when(penaltyRepo.findById(42L)).thenReturn(Optional.of(p));

        factory.penaltyGespielt("42");

        assertTrue(p.isGespielt());
        verify(penaltyRepo).save(p);
    }

    @Test
    void testPenaltyEingetragen() {
        Penalty p = new Penalty();
        p.setId(42L);
        when(penaltyRepo.findById(42L)).thenReturn(Optional.of(p));
        when(penaltyRepo.save(p)).thenReturn(p);

        Penalty result = factory.penaltyEingetragen("42", "M501,M502");

        assertTrue(result.isBestaetigt());
        assertEquals("M501,M502", result.getReihenfolge());
    }

    @Test
    void testSave() {
        Penalty p = new Penalty();
        when(penaltyRepo.save(p)).thenReturn(p);

        Penalty result = factory.save(p);
        assertSame(p, result);
        verify(penaltyRepo).save(p);
    }
}
