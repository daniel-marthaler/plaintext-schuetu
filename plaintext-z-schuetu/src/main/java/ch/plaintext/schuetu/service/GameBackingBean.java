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

    @Autowired
    private GameService gameService;

    private GameModel selected = new GameModel();

    private String renameOldName;
    private String renameNewName;

    private String copySourceName;
    private String copyNewName;

    public List<GameModel> displayGames() {
        return root.displayGames();
    }

    public void save() {
        repo.save(selected);
        selected = new GameModel();
    }

    public void prepareRename(String gameName) {
        this.renameOldName = gameName;
        this.renameNewName = gameName;
    }

    public void rename() {
        if (renameOldName != null && renameNewName != null && !renameNewName.isBlank() && !renameOldName.equals(renameNewName)) {
            gameService.renameGame(renameOldName, renameNewName);
            root.clearCache();
        }
        renameOldName = null;
        renameNewName = null;
    }

    public void prepareCopy(String gameName) {
        this.copySourceName = gameName;
        this.copyNewName = gameName + " (Kopie)";
    }

    public void copyGame() {
        if (copySourceName != null && copyNewName != null && !copyNewName.isBlank()) {
            gameService.copyGame(copySourceName, copyNewName);
            root.clearCache();
        }
        copySourceName = null;
        copyNewName = null;
    }

}
