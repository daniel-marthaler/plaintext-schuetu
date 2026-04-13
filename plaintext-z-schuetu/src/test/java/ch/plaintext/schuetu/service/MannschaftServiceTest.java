package ch.plaintext.schuetu.service;

import ch.plaintext.schuetu.entity.Mannschaft;
import ch.plaintext.schuetu.repository.GameRepository;
import ch.plaintext.schuetu.repository.MannschaftRepository;
import ch.plaintext.schuetu.service.websiteinfo.VelocityReplacer;
import ch.plaintext.schuetu.service.websiteinfo.WebsiteInfoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MannschaftServiceTest {

    @InjectMocks
    private MannschaftService service;

    @Mock
    private MannschaftRepository repo;

    @Mock
    private WebsiteInfoService infoService;

    @Mock
    private GameRepository gameRepository;

    @Mock
    private VelocityReplacer website;

    @Test
    void testGetJahre() {
        when(infoService.getOldJahre()).thenReturn(Set.of("2024", "2025"));

        List<String> jahre = service.getJahre();
        assertEquals(2, jahre.size());
        assertTrue(jahre.contains("2024"));
        assertTrue(jahre.contains("2025"));
    }

    @Test
    void testGetMannschaften() {
        Mannschaft m1 = new Mannschaft();
        m1.setNickname("Tigers");
        when(repo.findByGame("TestGame")).thenReturn(List.of(m1));

        List<Mannschaft> result = service.getMannschaften("2025", "TestGame");
        assertEquals(1, result.size());
    }

    @Test
    void testSave() {
        Mannschaft m = new Mannschaft();
        m.setNickname("Eagles");
        when(repo.save(m)).thenReturn(m);

        Mannschaft result = service.save(m);
        assertNotNull(result);
        verify(website).dumpWebsite();
        verify(repo).save(m);
    }

    @Test
    void testCopyFromOldGame() {
        Mannschaft m = new Mannschaft();
        m.setId(1L);
        m.setGame("OldGame");
        m.setNickname("Tigers");
        m.setKlasse(5);

        when(repo.findByGame("OldGame")).thenReturn(List.of(m));

        service.copyFromOldGame("OldGame", "NewGame");

        verify(repo).save(argThat(saved ->
                "NewGame".equals(saved.getGame()) && saved.getTeamNummer() == 0));
    }
}
