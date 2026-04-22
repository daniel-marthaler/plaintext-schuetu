package ch.plaintext.schuetu.service;

import ch.plaintext.schuetu.entity.GameModel;
import ch.plaintext.schuetu.repository.GameRepository;
import ch.plaintext.schuetu.service.exportimport.TournamentExportDto;
import ch.plaintext.schuetu.service.exportimport.TournamentImportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.model.file.UploadedFile;
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
@Slf4j
public class GameBackingBean {

    @Autowired
    private GameRoot root;

    @Autowired
    private GameRepository repo;

    @Autowired
    private GameService gameService;

    @Autowired
    private TournamentImportService importService;

    @Autowired
    private ObjectMapper objectMapper;

    private GameModel selected = new GameModel();

    private String renameOldName;
    private String renameNewName;

    private String copySourceName;
    private String copyNewName;

    private String deleteGameName;

    private UploadedFile importFile;
    private String importName;

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
        FacesContext ctx = FacesContext.getCurrentInstance();
        try {
            if (renameOldName != null && renameNewName != null && !renameNewName.isBlank() && !renameOldName.equals(renameNewName)) {
                gameService.renameGame(renameOldName, renameNewName);
                root.clearCache();
                ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Umbenannt", "'" + renameOldName + "' -> '" + renameNewName + "'"));
            }
        } catch (Exception e) {
            log.error("Fehler beim Umbenennen von '{}' zu '{}'", renameOldName, renameNewName, e);
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Umbenennen fehlgeschlagen", e.getMessage()));
        } finally {
            renameOldName = null;
            renameNewName = null;
        }
    }

    public void prepareCopy(String gameName) {
        this.copySourceName = gameName;
        this.copyNewName = gameName + " (Kopie)";
    }

    public void copyGame() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        try {
            if (copySourceName != null && copyNewName != null && !copyNewName.isBlank()) {
                gameService.copyGame(copySourceName, copyNewName);
                root.clearCache();
                ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Kopiert", "'" + copySourceName + "' -> '" + copyNewName + "'"));
            }
        } catch (Exception e) {
            log.error("Fehler beim Kopieren von '{}' zu '{}'", copySourceName, copyNewName, e);
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Kopieren fehlgeschlagen", e.getMessage()));
        } finally {
            copySourceName = null;
            copyNewName = null;
        }
    }

    public void prepareDelete(String gameName) {
        this.deleteGameName = gameName;
    }

    public void deleteGame() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        try {
            if (deleteGameName != null) {
                gameService.deleteGame(deleteGameName);
                root.clearCache();
                ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Geloescht", "Turnier '" + deleteGameName + "' wurde geloescht"));
            }
        } catch (Exception e) {
            log.error("Fehler beim Loeschen von '{}'", deleteGameName, e);
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Loeschen fehlgeschlagen", e.getMessage()));
        } finally {
            deleteGameName = null;
        }
    }

    public void importTournament() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        if (importFile == null || importFile.getContent() == null || importFile.getContent().length == 0) {
            ctx.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Bitte eine JSON-Datei auswaehlen", null));
            return;
        }
        if (importName == null || importName.trim().isEmpty()) {
            ctx.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Bitte einen Turniernamen eingeben", null));
            return;
        }
        try {
            TournamentExportDto dto = objectMapper.readValue(importFile.getContent(), TournamentExportDto.class);
            importService.importTournament(dto, importName.trim());
            log.info("Turnier '{}' erfolgreich importiert als '{}'", dto.getOriginalGameName(), importName);
            root.clearCache();
            ctx.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Import erfolgreich", "Turnier '" + importName.trim() + "' wurde importiert."));
        } catch (IllegalArgumentException e) {
            log.warn("Import fehlgeschlagen: {}", e.getMessage());
            ctx.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Import fehlgeschlagen", e.getMessage()));
        } catch (Exception e) {
            log.error("Fehler beim Import", e);
            ctx.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Import fehlgeschlagen", e.getMessage()));
        }
        importFile = null;
        importName = null;
    }

}
