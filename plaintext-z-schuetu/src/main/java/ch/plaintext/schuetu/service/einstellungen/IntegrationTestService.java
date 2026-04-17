package ch.plaintext.schuetu.service.einstellungen;

import ch.plaintext.schuetu.entity.Spiel;
import ch.plaintext.schuetu.repository.SpielRepository;
import ch.plaintext.schuetu.service.ResultateVerarbeiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service fuer Integrationstest-Operationen.
 * Laeuft in eigener Transaktion, damit Background-Threads korrekt funktionieren.
 */
@Service
@Slf4j
public class IntegrationTestService {

    @Autowired
    private SpielRepository spielRepository;

    @Transactional(readOnly = true)
    public List<Spiel> findAllEinzutragende(String gameName) {
        return spielRepository.findAllEinzutragende(gameName);
    }

    @Transactional(readOnly = true)
    public List<Spiel> findAllZuBestaetigen(String gameName) {
        return spielRepository.findAllZuBestaetigen(gameName);
    }

    /**
     * Traegt automatisch Resultate ein. Gibt einen Status-String zurueck fuer die UI.
     */
    @Transactional
    public String autoEintragen(Long spielId) {
        Spiel spiel = spielRepository.findById(spielId).orElse(null);
        if (spiel == null || spiel.isFertigEingetragen()) return null;

        if (spiel.getMannschaftA() != null && spiel.getMannschaftB() != null) {
            int toreA = spiel.getMannschaftA().getTeamNummer() % 10;
            int toreB = spiel.getMannschaftB().getTeamNummer() % 10;
            spiel.setToreA(toreA);
            spiel.setToreB(toreB);
            spiel.setFertigEingetragen(true);
            spiel.setEintrager("Auto-Schiri");
            spiel.setSchiriName("Auto-Schiri");
            spielRepository.save(spiel);
            String mannA = spiel.getMannschaftA().getName();
            String mannB = spiel.getMannschaftB().getName();
            log.info("Auto-Schiri: Spiel {} {} vs {} = {}:{}", spiel.getIdString(), mannA, mannB, toreA, toreB);
            return spiel.getIdString() + " - " + mannA + " vs " + mannB + " (" + toreA + ":" + toreB + ")";
        }
        return null;
    }

    @Transactional
    public String autoBestaetigen(Long spielId, ResultateVerarbeiter resultate) {
        Spiel spiel = spielRepository.findById(spielId).orElse(null);
        if (spiel == null || spiel.isFertigBestaetigt()) return null;

        spiel.setFertigBestaetigt(true);
        spiel.setToreABestaetigt(spiel.getToreA());
        spiel.setToreBBestaetigt(spiel.getToreB());
        spiel.setKontrolle("Auto-Kontrolleur");
        spielRepository.save(spiel);
        resultate.signalFertigesSpiel(spielId);
        String mannA = spiel.getMannschaftA() != null ? spiel.getMannschaftA().getName() : "?";
        String mannB = spiel.getMannschaftB() != null ? spiel.getMannschaftB().getName() : "?";
        log.info("Auto-Kontrolleur: {} {} vs {} ({}:{}) bestaetigt", spiel.getIdString(), mannA, mannB, spiel.getToreA(), spiel.getToreB());
        return spiel.getIdString() + " - " + mannA + " vs " + mannB + " (" + spiel.getToreA() + ":" + spiel.getToreB() + ")";
    }
}
