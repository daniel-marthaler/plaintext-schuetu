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

    @Transactional
    public void autoEintragen(Long spielId) {
        Spiel spiel = spielRepository.findById(spielId).orElse(null);
        if (spiel == null || spiel.isFertigEingetragen()) return;

        if (spiel.getMannschaftA() != null && spiel.getMannschaftB() != null) {
            int toreA = spiel.getMannschaftA().getTeamNummer() % 10;
            int toreB = spiel.getMannschaftB().getTeamNummer() % 10;
            spiel.setToreA(toreA);
            spiel.setToreB(toreB);
            spiel.setFertigEingetragen(true);
            spiel.setEintrager("Auto-Schiri");
            spiel.setSchiriName("Auto-Schiri");
            spielRepository.save(spiel);
            log.info("Auto-Schiri: Spiel {} = {}:{}", spiel.getIdString(), toreA, toreB);
        }
    }

    @Transactional
    public void autoBestaetigen(Long spielId, ResultateVerarbeiter resultate) {
        Spiel spiel = spielRepository.findById(spielId).orElse(null);
        if (spiel == null || spiel.isFertigBestaetigt()) return;

        spiel.setFertigBestaetigt(true);
        spiel.setToreABestaetigt(spiel.getToreA());
        spiel.setToreBBestaetigt(spiel.getToreB());
        spiel.setKontrolle("Auto-Kontrolleur");
        spielRepository.save(spiel);
        resultate.signalFertigesSpiel(spielId);
        log.info("Auto-Kontrolleur: Spiel {} bestaetigt", spiel.getIdString());
    }
}
