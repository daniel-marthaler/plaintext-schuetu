package ch.plaintext.schuetu.service.spieldurchfuehrung;

import ch.plaintext.schuetu.service.Game;
import ch.plaintext.schuetu.entity.Penalty;
import ch.plaintext.schuetu.repository.PenaltyRepository;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Scope("prototype")
@Slf4j
public class PenaltyService {

    @Autowired
    private PenaltyRepository penaltyRepo;

    @Setter
    private Game game;

    public List<Penalty> anstehendePenalty() {
        List<Penalty> alle = penaltyRepo.findByGame(game.getModel().getGameName());
        List<Penalty> result = new ArrayList<>();
        for (Penalty p : alle) {
            if (!p.isBestaetigt() && !p.isGespielt()) {
                result.add(p);
            }
        }
        return result;
    }

    public List<Penalty> gespieltePenalty() {
        List<Penalty> alle = penaltyRepo.findByGame(game.getModel().getGameName());
        List<Penalty> result = new ArrayList<>();
        for (Penalty p : alle) {
            if (!p.isBestaetigt() && p.isGespielt()) {
                result.add(p);
                return result;
            }
        }
        return result;
    }

    public void penaltyEintragen(List<Penalty> list) {
        for (Penalty p : list) {
            if (p.getReihenfolge() != null && !p.getReihenfolge().isEmpty()) {
                if (p.isBestaetigt() && p.isGespielt()) {
                    continue;
                }
                if (p.getReihenfolge().equals(Penalty.LEER)) {
                    continue;
                }
                p.setGespielt(true);
                p.setBestaetigt(true);
                p.setGame(game.getModel().getGameName());
                p = this.penaltyRepo.save(p);
                game.getResultate().signalPenalty(p);
            }
        }
    }

    public List<Penalty> eingetragenePenalty() {
        List<Penalty> alle = penaltyRepo.findByGame(game.getModel().getGameName());
        List<Penalty> result = new ArrayList<>();
        for (Penalty p : alle) {
            if (p.isBestaetigt() && p.isGespielt()) {
                result.add(p);
            }
        }
        return result;
    }

}
