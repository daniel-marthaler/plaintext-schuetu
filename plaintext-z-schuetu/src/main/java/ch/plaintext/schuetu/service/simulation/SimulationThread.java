package ch.plaintext.schuetu.service.simulation;

import ch.plaintext.schuetu.service.Game;
import ch.plaintext.schuetu.service.GameRoot;
import ch.plaintext.schuetu.entity.Penalty;
import ch.plaintext.schuetu.entity.Spiel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class SimulationThread implements Serializable {

    private static final long serialVersionUID = 1L;
    private boolean durchlaufmode = true;

    @Autowired
    private SimulationModel backing;

    @Autowired
    private GameRoot gameRoot;

    private Game game;

    @Scheduled(fixedRate = 1000)
    public void run() {
        if (!backing.isOn()) { return; }
        if (game == null) { game = gameRoot.selectGame(backing.getGameName()); }
        speaker();
        eintrager();
        kontrollierer();
    }

    private void speaker() { game.getDurchfuehrung().enter(); }

    private boolean eintrager() {
        List<Penalty> penaltys = game.getPenalty().anstehendePenalty();
        for (Penalty penalty : penaltys) {
            log.info("PENALTY: " + penalty.toString());
            penalty.setReihenfolge(penalty.toString());
        }
        game.getPenalty().penaltyEintragen(penaltys);

        List<Spiel> eintragen = game.getEintragen().findAllEinzutragende();
        if (!eintragen.isEmpty()) {
            for (Spiel spiel : eintragen) {
                List<String> res = new ArrayList<>();
                res.add("" + spiel.getMannschaftAName().charAt(3));
                res.add("" + spiel.getMannschaftBName().charAt(3));
                Integer a = 99;
                Integer b = 99;
                String methode = this.backing.getMethode();
                switch (methode) {
                    case "echt": break;
                    case "random3": { java.util.Random random = new java.util.Random(); a = random.nextInt(3); b = random.nextInt(3); break; }
                    case "random9": { java.util.Random random = new java.util.Random(); a = random.nextInt(9); b = +random.nextInt(9); break; }
                    case "random2": { java.util.Random random = new java.util.Random(); a = random.nextInt(2); b = +random.nextInt(2); break; }
                    case "absteigend": a = (10 - Integer.parseInt(res.get(0))); b = (10 - Integer.parseInt(res.get(1))); break;
                    case "aufsteigend": a = Integer.parseInt(res.get(0)); b = Integer.parseInt(res.get(1)); break;
                }
                spiel.setToreA(a);
                spiel.setToreB(b);
                List<Spiel> temp = new ArrayList<>();
                temp.add(spiel);
                game.getEintragen().eintragen(temp, "" + spiel.getId(), "SimulationThread");
                if (!durchlaufmode) { return true; }
            }
            return true;
        }
        return false;
    }

    private boolean kontrollierer() {
        List<Spiel> best = game.getEintragen().findAllZuBestaetigen();
        for (Spiel spiel : best) {
            List<Spiel> temp = new ArrayList<>();
            temp.add(spiel);
            game.getEintragen().bestaetigen(temp, "" + spiel.getId(), "ok");
            if (!durchlaufmode) { return true; }
        }
        return false;
    }

}
