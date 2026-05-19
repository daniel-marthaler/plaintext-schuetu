package ch.plaintext.schuetu.web;

import ch.plaintext.PlaintextSecurity;
import ch.plaintext.schuetu.entity.Schiri;
import ch.plaintext.schuetu.entity.Spiel;
import ch.plaintext.schuetu.repository.SchiriRepository;
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
class MeineSpieleBeanTest {

    @InjectMocks
    private MeineSpieleBean bean;

    @Mock
    private SchiriRepository schiriRepository;

    @Mock
    private SpielRepository spielRepository;

    @Mock
    private GameSelectionHolder holder;

    @Mock
    private PlaintextSecurity plaintextSecurity;

    private static final String GAME = "TEST";

    private Schiri schiri(Long id, String loginName, String vorname) {
        Schiri s = new Schiri();
        s.setId(id);
        s.setLoginName(loginName);
        s.setVorname(vorname);
        s.setNachname("Muster");
        return s;
    }

    private Spiel spiel(Schiri schiri, Date start, boolean amSpielen, boolean fertig) {
        Spiel s = new Spiel();
        s.setSchiri(schiri);
        s.setStart(start);
        s.setAmSpielen(amSpielen);
        s.setFertigGespielt(fertig);
        return s;
    }

    @BeforeEach
    void setUp() {
        lenient().when(holder.hasGame()).thenReturn(true);
        lenient().when(holder.getGameName()).thenReturn(GAME);
    }

    @Test
    void testSchiriBekannt_WhenLoginMatches() {
        Schiri me = schiri(1L, "alice", "Alice");
        Schiri other = schiri(2L, "bob", "Bob");
        when(plaintextSecurity.getUser()).thenReturn("alice");
        when(schiriRepository.findByGame(GAME)).thenReturn(List.of(other, me));

        bean.init();

        assertTrue(bean.isSchiriBekannt());
        assertEquals("Alice", bean.getSchiriVorname());
    }

    @Test
    void testSchiriBekannt_FalseWhenNoMatch() {
        when(plaintextSecurity.getUser()).thenReturn("charlie");
        when(schiriRepository.findByGame(GAME)).thenReturn(List.of(schiri(1L, "alice", "Alice")));

        bean.init();

        assertFalse(bean.isSchiriBekannt());
        assertEquals("", bean.getSchiriVorname());
    }

    @Test
    void testSchiriBekannt_FalseWhenSecurityThrows() {
        when(plaintextSecurity.getUser()).thenThrow(new RuntimeException("no auth"));

        bean.init();

        assertFalse(bean.isSchiriBekannt());
        assertTrue(bean.getNaechsteEinsaetze().isEmpty());
    }

    @Test
    void testNaechsteEinsaetze_FilterNachSchiriId_SortiertNachStart_Limit10() {
        Schiri me = schiri(1L, "alice", "Alice");
        Schiri other = schiri(2L, "bob", "Bob");
        long now = System.currentTimeMillis();
        Spiel s1 = spiel(me, new Date(now + 30 * 60_000), false, false);
        Spiel s2 = spiel(me, new Date(now + 10 * 60_000), false, false);
        Spiel s3 = spiel(other, new Date(now + 5 * 60_000), false, false);
        Spiel altKeineSchiri = new Spiel();
        altKeineSchiri.setStart(new Date(now + 1_000));
        Spiel zuAlt = spiel(me, new Date(now - 3L * 60 * 60 * 1000), false, true);

        when(plaintextSecurity.getUser()).thenReturn("alice");
        when(schiriRepository.findByGame(GAME)).thenReturn(List.of(me, other));
        when(spielRepository.findByGame(GAME)).thenReturn(List.of(s1, s2, s3, altKeineSchiri, zuAlt));

        bean.init();
        List<Spiel> result = bean.getNaechsteEinsaetze();

        assertEquals(2, result.size());
        assertEquals(s2, result.get(0)); // frueher zuerst
        assertEquals(s1, result.get(1));
        assertFalse(result.contains(s3), "Andere Schiris raus");
        assertFalse(result.contains(altKeineSchiri), "Spiel ohne Schiri raus");
        assertFalse(result.contains(zuAlt), "Aelter als 2h raus");
    }

    @Test
    void testNaechsteEinsaetze_LimitAt10() {
        Schiri me = schiri(1L, "alice", "Alice");
        when(plaintextSecurity.getUser()).thenReturn("alice");
        when(schiriRepository.findByGame(GAME)).thenReturn(List.of(me));
        long now = System.currentTimeMillis();
        java.util.List<Spiel> many = new java.util.ArrayList<>();
        for (int i = 0; i < 15; i++) {
            many.add(spiel(me, new Date(now + (i + 1) * 60_000L), false, false));
        }
        when(spielRepository.findByGame(GAME)).thenReturn(many);

        bean.init();
        List<Spiel> result = bean.getNaechsteEinsaetze();

        assertEquals(10, result.size());
    }

    @Test
    void testStatusBadge_VerschiedeneStates() {
        long now = System.currentTimeMillis();
        Spiel live = spiel(null, new Date(now), true, false);
        Spiel gleich = spiel(null, new Date(now + 5 * 60_000), false, false);
        Spiel geplant = spiel(null, new Date(now + 60 * 60_000), false, false);
        Spiel fertig = spiel(null, new Date(now - 60_000), false, true);
        fertig.setToreABestaetigt(2);
        fertig.setToreBBestaetigt(1);

        assertEquals("LIVE", bean.statusBadge(live));
        assertEquals("GLEICH", bean.statusBadge(gleich));
        assertEquals("GEPLANT", bean.statusBadge(geplant));
        assertEquals("FERTIG", bean.statusBadge(fertig));
        assertEquals("", bean.statusBadge(null));
    }

    @Test
    void testCountdownText_LiveUndZeit() {
        long now = System.currentTimeMillis();
        Spiel live = spiel(null, new Date(now), true, false);
        Spiel in75m = spiel(null, new Date(now + 75 * 60_000), false, false);
        Spiel in20m = spiel(null, new Date(now + 20 * 60_000), false, false);

        assertEquals("läuft", bean.countdownText(live));
        assertTrue(bean.countdownText(in75m).startsWith("in 1h"));
        assertTrue(bean.countdownText(in20m).startsWith("in 20m") || bean.countdownText(in20m).startsWith("in 19m"));
    }

    @Test
    void testErledigteUndTotalHeute() {
        Schiri me = schiri(1L, "alice", "Alice");
        long now = System.currentTimeMillis();
        Spiel heuteErledigt = spiel(me, new Date(now - 60_000), false, true);
        Spiel heuteOffen = spiel(me, new Date(now + 60_000), false, false);
        Spiel langeWeg = spiel(me, new Date(now + 5L * 24 * 60 * 60 * 1000), false, false);

        when(plaintextSecurity.getUser()).thenReturn("alice");
        when(schiriRepository.findByGame(GAME)).thenReturn(List.of(me));
        when(spielRepository.findByGame(GAME)).thenReturn(List.of(heuteErledigt, heuteOffen, langeWeg));

        bean.init();

        assertEquals(2, bean.getTotalHeute());
        assertEquals(0, bean.getErledigteHeute());
    }
}
