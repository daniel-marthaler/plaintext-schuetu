package ch.plaintext.schuetu.service;

import ch.plaintext.schuetu.entity.GameModel;
import ch.plaintext.schuetu.repository.GameRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Erstellt neue Game Instanzen oder laed diese aus der datenbank und stellt sie
 * wieder her
 */
@Component
@Slf4j
public class GameRoot {

    @Getter
    private Map<String, Game> gameCache = new HashMap<>();

    @Autowired
    private GameRepository repo;

    @Autowired
    private ApplicationContext ctx;

    @Value("${spring.profiles.active:}")
    private String activeProfile;

    @PostConstruct
    private void init() {
        List<GameModel> alle = repo.findAll();
        boolean hasRrr = false;
        for (GameModel model : alle) {
            if (model.getGameName().equals("rrr")) {
                hasRrr = true;
            }
            model.setInitialisiert(Boolean.FALSE);
        }
        repo.saveAll(alle);

        if (hasRrr && activeProfile != null && activeProfile.contains("devl")) {
            this.selectGame("rrr");
        }

    }

    public List<GameModel> displayGames() {
        return repo.findAll();
    }

    public Game selectGame(String gameName) {
        if (gameCache.containsKey(gameName)) {
            return gameCache.get(gameName);
        }
        GameModel model = repo.findByGameName(gameName);

        if (model == null) {
            return null;
        }

        model.setInitialisiert(Boolean.TRUE);
        Game game = ctx.getBean(Game.class);
        game.setModel(model);
        gameCache.put(gameName, game);
        repo.save(model);
        game.init();
        return game;
    }

}
