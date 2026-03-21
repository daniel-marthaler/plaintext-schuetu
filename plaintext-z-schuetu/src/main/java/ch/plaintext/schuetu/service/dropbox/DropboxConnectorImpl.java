package ch.plaintext.schuetu.service.dropbox;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Dropbox connector - functionality removed as the old Dropbox framework is no longer available.
 *
 * TODO: Re-implement with a modern Dropbox SDK or alternative cloud storage if needed.
 */
@Slf4j
@Component
public class DropboxConnectorImpl {

    @Getter
    private String rootFolder = "/shared/apps/schuetu";

    public Boolean isConnected() {
        log.warn("DropboxConnectorImpl: Dropbox integration removed - not connected");
        return false;
    }

    public String getLoginURL() { return ""; }
    public void insertToken(String token) { }
    public List<String> getFilesInFolder() { return new ArrayList<>(); }
    public List<String> getFilesInFolder(String folder) { return new ArrayList<>(); }
    public List<String> getFilesInAltFolder() { return new ArrayList<>(); }
    public List<String> getFoldersInRootFolder() { return new ArrayList<>(); }
    public byte[] loadFile(String file) { return new byte[0]; }
    public void saveFile(String file, byte[] content) { }
    public byte[] selectGame(String folder) { return new byte[0]; }
    public List<String> getAllGames() { return new ArrayList<>(); }
    public String getSelectedGame() { return ""; }
    public void setSelectedGame(String selectedGame) { }
    public byte[] loadGameAttachemt(String file, String suffix) { return new byte[0]; }
    public void saveGameAttachemt(String file, String suffix, byte[] content) { }
    public void deleteGameAttachemt(String file, String suffix) { }
    public void saveGame(byte[] content) { }
    public void saveOldGame(String jahr, String content) { }
    public Map<String, String> loadOldGames() { return Map.of(); }
    public void deleteFile(String file) { }
    public String getDescription() { return "Dropbox integration removed"; }
}
