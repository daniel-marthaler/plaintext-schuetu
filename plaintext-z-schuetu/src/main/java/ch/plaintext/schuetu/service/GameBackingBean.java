package ch.plaintext.schuetu.service;

import ch.plaintext.schuetu.entity.GameModel;
import ch.plaintext.schuetu.repository.GameRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Erstellt neue Game Instanzen oder laed diese aus der datenbank und stellt sie
 * wieder her
 */
@Component
@Scope("session")
@Data
public class GameBackingBean {

    @Autowired
    private GameRoot root;

    @Autowired
    private GameRepository repo;

    private GameModel selected = new GameModel();

    public List<GameModel> displayGames() {
        return root.displayGames();
    }

    public void save() {
        repo.save(selected);
        selected = new GameModel();
    }

}
