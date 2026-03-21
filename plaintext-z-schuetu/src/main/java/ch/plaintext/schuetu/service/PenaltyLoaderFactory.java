package ch.plaintext.schuetu.service;

import ch.plaintext.schuetu.service.utils.IDGeneratorContainer;
import ch.plaintext.schuetu.entity.Mannschaft;
import ch.plaintext.schuetu.entity.Penalty;
import ch.plaintext.schuetu.repository.PenaltyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Dient dazu eine Penalty Instanz zu erzeugen oder aus der db zu laden
 */
@Component
@Slf4j
public class PenaltyLoaderFactory {

    private static PenaltyLoaderFactory INSTANCE;

    @Autowired
    private PenaltyRepository penaltyRepo;

    public PenaltyLoaderFactory() {
        INSTANCE = this;
    }

    public static PenaltyLoaderFactory getInstance() {
        return INSTANCE;
    }

    public Penalty loadPenaltyAnstehend(String game) {
        List<Penalty> penaltys = penaltyRepo.findByGame(game);

        for (Penalty temp : penaltys) {
            if (!temp.isGespielt()) {
                return temp;
            }
        }
        return null;
    }

    public Penalty loadPenaltyGespielt(String game) {
        List<Penalty> penaltys = penaltyRepo.findByGame(game);

        for (Penalty temp : penaltys) {
            if (temp.isGespielt() && !temp.isBestaetigt()) {
                return temp;
            }
        }
        return null;
    }

    public void penaltyGespielt(String id) {
        Penalty penalty = penaltyRepo.findById(Long.parseLong(id)).get();
        penalty.setGespielt(true);
        penaltyRepo.save(penalty);
    }

    public Penalty penaltyEingetragen(String id, String reihenfolge) {
        Penalty penalty = penaltyRepo.findById(Long.parseLong(id)).get();
        penalty.setBestaetigt(true);
        penalty.setReihenfolge(reihenfolge);
        return penaltyRepo.save(penalty);
    }

    public Penalty getPenalty(List<Mannschaft> mannschaften, String game) {

        Penalty tempPenalty = new Penalty();

        for (Mannschaft m : mannschaften) {
            tempPenalty.addMannschaftInitial(m);
        }

        Penalty result = penaltyRepo.findPenaltyByOriginalreihenfolge(tempPenalty.toMannschaftsString(), game);

        if (result != null) {

            if (result.getRealFinalList() != null && !result.getRealFinalList().isEmpty()) {
                return result;
            }

            for (Mannschaft m : mannschaften) {
                result.addMannschaftInitial(m);
            }

            return penaltyRepo.save(result);
        }

        tempPenalty.setIdString(IDGeneratorContainer.getNext());
        tempPenalty.setGame(game);
        log.info("neuer penalty: " + tempPenalty);
        return penaltyRepo.save(tempPenalty);

    }

    public Penalty save(Penalty penalty) {
        return this.penaltyRepo.save(penalty);
    }

}
