package ch.plaintext.schuetu.service.spieldurchfuehrung;

import ch.plaintext.schuetu.service.Game;
import ch.plaintext.schuetu.entity.Spiel;
import ch.plaintext.schuetu.repository.SpielRepository;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Scope("prototype")
public class EintragerService {

    @Autowired
    private SpielRepository spielRepository;

    @Setter
    private Game game;

    public List<Spiel> findAllEinzutragende() {
        return spielRepository.findAllEinzutragende(game.getModel().getGameName());
    }

    public List<Spiel> findAllZuBestaetigen() {
        return spielRepository.findAllZuBestaetigen(game.getModel().getGameName());
    }

    public synchronized void eintragen(List<Spiel> spiele, String id, String eintrager) {
        long idl = Long.parseLong(id);
        for (Spiel spiel : spiele) {
            if (spiel.getId() == idl && spiel.getToreA() > -1 && spiel.getToreB() > -1) {
                spiel.setFertigEingetragen(true);
                spiel.setEintrager(eintrager);
                spielRepository.save(spiel);
            }
        }
    }

    public synchronized void bestaetigen(List<Spiel> spiele, String id, String ok) {
        long idl = Long.parseLong(id);
        for (Spiel spiel : spiele) {
            if (spiel.getId() == idl) {
                if (ok.equals("ok")) {
                    spiel.setFertigBestaetigt(true);
                    spiel.setToreABestaetigt(spiel.getToreA());
                    spiel.setToreBBestaetigt(spiel.getToreB());
                    spielRepository.save(spiel);
                    game.getResultate().signalFertigesSpiel(spiel.getId());
                } else {
                    spiel.setZurueckgewiesen(true);
                    spiel.setFertigEingetragen(false);
                }
                spielRepository.save(spiel);
            }
        }
    }
}
