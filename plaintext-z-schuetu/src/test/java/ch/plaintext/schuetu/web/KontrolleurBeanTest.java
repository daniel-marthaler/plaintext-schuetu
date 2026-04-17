package ch.plaintext.schuetu.web;

import ch.plaintext.PlaintextSecurity;
import ch.plaintext.schuetu.entity.Spiel;
import ch.plaintext.schuetu.repository.SpielRepository;
import ch.plaintext.schuetu.service.Game;
import ch.plaintext.schuetu.service.GameSelectionHolder;
import ch.plaintext.schuetu.service.ResultateVerarbeiter;
import ch.plaintext.schuetu.service.mqtt.MqttEventPublisher;
import ch.plaintext.schuetu.service.spieldurchfuehrung.EintragerService;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KontrolleurBeanTest {

    @InjectMocks
    private KontrolleurBean bean;

    @Mock
    private GameSelectionHolder holder;

    @Mock
    private SpielRepository spielRepository;

    @Mock
    private PlaintextSecurity plaintextSecurity;

    @Mock
    private MqttEventPublisher mqttEventPublisher;

    @Mock
    private Game game;

    @Mock
    private ResultateVerarbeiter resultate;

    @Mock
    private EintragerService eintragen;

    @Mock
    private FacesContext facesContext;

    private MockedStatic<FacesContext> facesContextMock;

    @BeforeEach
    void setUp() {
        facesContextMock = mockStatic(FacesContext.class);
        facesContextMock.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
    }

    @AfterEach
    void tearDown() {
        facesContextMock.close();
    }

    // --- getZuBestaetigendeSpiele ---

    @Test
    void testGetZuBestaetigendeSpiele_NoGame() {
        when(holder.hasGame()).thenReturn(false);

        List<Spiel> result = bean.getZuBestaetigendeSpiele();

        assertTrue(result.isEmpty());
    }

    @Test
    void testGetZuBestaetigendeSpiele_WithGame() {
        when(holder.hasGame()).thenReturn(true);
        when(holder.getGame()).thenReturn(game);
        when(game.getEintragen()).thenReturn(eintragen);
        List<Spiel> expected = List.of(new Spiel(), new Spiel());
        when(eintragen.findAllZuBestaetigen()).thenReturn(expected);

        List<Spiel> result = bean.getZuBestaetigendeSpiele();

        assertEquals(2, result.size());
    }

    // --- bestaetigen ---

    @Test
    void testBestaetigen_ValidSpiel() {
        Spiel spiel = new Spiel();
        spiel.setId(42L);
        spiel.setIdString("S01");
        spiel.setToreA(3);
        spiel.setToreB(1);

        when(plaintextSecurity.getUser()).thenReturn("TestUser");
        when(holder.getGame()).thenReturn(game);
        when(game.getResultate()).thenReturn(resultate);

        bean.bestaetigen(spiel);

        assertTrue(spiel.isFertigBestaetigt());
        assertEquals(3, spiel.getToreABestaetigt());
        assertEquals(1, spiel.getToreBBestaetigt());
        assertEquals("TestUser", spiel.getKontrolle());
        verify(spielRepository).save(spiel);
        verify(resultate).signalFertigesSpiel(42L);
        verify(mqttEventPublisher).spielKontrolle(spiel);
        verify(facesContext).addMessage(eq(null), any(FacesMessage.class));
    }

    @Test
    void testBestaetigen_NullSpiel() {
        bean.bestaetigen(null);

        verify(spielRepository, never()).save(any());
        verify(mqttEventPublisher, never()).spielKontrolle(any());
    }

    @Test
    void testBestaetigen_SecurityException_FallsBackToAdmin() {
        Spiel spiel = new Spiel();
        spiel.setId(10L);
        spiel.setIdString("S02");
        spiel.setToreA(2);
        spiel.setToreB(2);

        when(plaintextSecurity.getUser()).thenThrow(new RuntimeException("no session"));
        when(holder.getGame()).thenReturn(game);
        when(game.getResultate()).thenReturn(resultate);

        bean.bestaetigen(spiel);

        assertEquals("admin", spiel.getKontrolle());
        assertTrue(spiel.isFertigBestaetigt());
        verify(spielRepository).save(spiel);
    }

    // --- zurueckweisen ---

    @Test
    void testZurueckweisen_ValidSpiel() {
        Spiel spiel = new Spiel();
        spiel.setId(42L);
        spiel.setIdString("S01");
        spiel.setFertigEingetragen(true);
        spiel.setNotizen("");

        when(plaintextSecurity.getUser()).thenReturn("Kontrolleur1");
        bean.setKorrekturBemerkung("Falsche Tore");

        bean.zurueckweisen(spiel);

        assertTrue(spiel.isZurueckgewiesen());
        assertFalse(spiel.isFertigEingetragen());
        assertEquals("Kontrolleur1", spiel.getKontrolle());
        assertTrue(spiel.getNotizen().contains("Zurueckgewiesen von Kontrolleur1: Falsche Tore"));
        verify(spielRepository).save(spiel);
        verify(facesContext).addMessage(eq(null), any(FacesMessage.class));
        assertEquals("", bean.getKorrekturBemerkung());
    }

    @Test
    void testZurueckweisen_NullSpiel() {
        bean.zurueckweisen(null);

        verify(spielRepository, never()).save(any());
    }

    @Test
    void testZurueckweisen_EmptyBemerkung_NoNotizenAppended() {
        Spiel spiel = new Spiel();
        spiel.setId(42L);
        spiel.setIdString("S01");
        spiel.setFertigEingetragen(true);
        spiel.setNotizen("");

        when(plaintextSecurity.getUser()).thenReturn("Kontrolleur1");
        bean.setKorrekturBemerkung("   "); // blank

        bean.zurueckweisen(spiel);

        assertTrue(spiel.isZurueckgewiesen());
        assertEquals("", spiel.getNotizen());
        verify(spielRepository).save(spiel);
    }

    @Test
    void testZurueckweisen_ExistingNotizen_Appended() {
        Spiel spiel = new Spiel();
        spiel.setId(42L);
        spiel.setIdString("S01");
        spiel.setFertigEingetragen(true);
        spiel.setNotizen("Vorherige Notiz");

        when(plaintextSecurity.getUser()).thenReturn("User2");
        bean.setKorrekturBemerkung("Tore vertauscht");

        bean.zurueckweisen(spiel);

        assertTrue(spiel.getNotizen().startsWith("Vorherige Notiz\n"));
        assertTrue(spiel.getNotizen().contains("Zurueckgewiesen von User2: Tore vertauscht"));
    }

    @Test
    void testZurueckweisen_NullNotizen_HandledGracefully() {
        Spiel spiel = new Spiel();
        spiel.setId(42L);
        spiel.setIdString("S01");
        spiel.setFertigEingetragen(true);
        spiel.setNotizen(null);

        when(plaintextSecurity.getUser()).thenReturn("User3");
        bean.setKorrekturBemerkung("Bemerkung");

        bean.zurueckweisen(spiel);

        assertNotNull(spiel.getNotizen());
        assertTrue(spiel.getNotizen().contains("Zurueckgewiesen von User3: Bemerkung"));
    }

    // --- korrigieren ---

    @Test
    void testKorrigieren_ValidSpiel() {
        Spiel spiel = new Spiel();
        spiel.setId(42L);
        spiel.setIdString("S01");
        spiel.setToreA(3);
        spiel.setToreB(1);
        spiel.setNotizen("");

        when(plaintextSecurity.getUser()).thenReturn("Kontrolleur1");
        when(holder.getGame()).thenReturn(game);
        when(game.getResultate()).thenReturn(resultate);

        bean.setKorrekturToreA(5);
        bean.setKorrekturToreB(2);
        bean.setKorrekturBemerkung("Tor uebersehen");

        bean.korrigieren(spiel);

        assertTrue(spiel.isFertigBestaetigt());
        assertEquals(5, spiel.getToreA());
        assertEquals(2, spiel.getToreB());
        assertEquals(5, spiel.getToreABestaetigt());
        assertEquals(2, spiel.getToreBBestaetigt());
        assertEquals("Kontrolleur1", spiel.getKontrolle());
        assertTrue(spiel.getNotizen().contains("Korrektur von Kontrolleur1: Tor uebersehen"));
        verify(spielRepository).save(spiel);
        verify(resultate).signalFertigesSpiel(42L);
        verify(mqttEventPublisher).spielKorrektur(spiel);
        // fields reset after korrigieren
        assertEquals(0, bean.getKorrekturToreA());
        assertEquals(0, bean.getKorrekturToreB());
        assertEquals("", bean.getKorrekturBemerkung());
    }

    @Test
    void testKorrigieren_NullSpiel() {
        bean.korrigieren(null);

        verify(spielRepository, never()).save(any());
        verify(mqttEventPublisher, never()).spielKorrektur(any());
    }

    @Test
    void testKorrigieren_EmptyBemerkung_NoNotizenAppended() {
        Spiel spiel = new Spiel();
        spiel.setId(42L);
        spiel.setIdString("S01");
        spiel.setNotizen("");

        when(plaintextSecurity.getUser()).thenReturn("Admin");
        when(holder.getGame()).thenReturn(game);
        when(game.getResultate()).thenReturn(resultate);

        bean.setKorrekturToreA(1);
        bean.setKorrekturToreB(0);
        bean.setKorrekturBemerkung("   "); // blank

        bean.korrigieren(spiel);

        assertEquals("", spiel.getNotizen());
        assertTrue(spiel.isFertigBestaetigt());
        verify(spielRepository).save(spiel);
    }

    @Test
    void testKorrigieren_ExistingNotizen_Appended() {
        Spiel spiel = new Spiel();
        spiel.setId(42L);
        spiel.setIdString("S01");
        spiel.setNotizen("Alt");

        when(plaintextSecurity.getUser()).thenReturn("Admin");
        when(holder.getGame()).thenReturn(game);
        when(game.getResultate()).thenReturn(resultate);

        bean.setKorrekturToreA(2);
        bean.setKorrekturToreB(3);
        bean.setKorrekturBemerkung("Fix");

        bean.korrigieren(spiel);

        assertTrue(spiel.getNotizen().startsWith("Alt\n"));
        assertTrue(spiel.getNotizen().contains("Korrektur von Admin: Fix"));
    }

    // --- prepareKorrektur ---

    @Test
    void testPrepareKorrektur() {
        Spiel spiel = new Spiel();
        spiel.setToreA(4);
        spiel.setToreB(2);

        bean.setKorrekturBemerkung("alte bemerkung");
        bean.prepareKorrektur(spiel);

        assertEquals(4, bean.getKorrekturToreA());
        assertEquals(2, bean.getKorrekturToreB());
        assertEquals("", bean.getKorrekturBemerkung());
    }

    @Test
    void testPrepareKorrektur_NullSpiel() {
        bean.setKorrekturToreA(99);
        bean.setKorrekturToreB(99);

        bean.prepareKorrektur(null);

        // values unchanged when null
        assertEquals(99, bean.getKorrekturToreA());
        assertEquals(99, bean.getKorrekturToreB());
    }

    // --- isGameSelected ---

    @Test
    void testIsGameSelected_True() {
        when(holder.hasGame()).thenReturn(true);
        assertTrue(bean.isGameSelected());
    }

    @Test
    void testIsGameSelected_False() {
        when(holder.hasGame()).thenReturn(false);
        assertFalse(bean.isGameSelected());
    }
}
