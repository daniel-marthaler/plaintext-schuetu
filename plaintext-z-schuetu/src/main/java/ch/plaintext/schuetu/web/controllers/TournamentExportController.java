package ch.plaintext.schuetu.web.controllers;

import ch.plaintext.schuetu.service.exportimport.TournamentExportDto;
import ch.plaintext.schuetu.service.exportimport.TournamentExportService;
import ch.plaintext.schuetu.service.exportimport.TournamentImportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST Controller fuer den Export und Import von Turnieren als JSON.
 */
@RestController
@RequestMapping("/api/tournament")
@Slf4j
public class TournamentExportController {

    @Autowired
    private TournamentExportService exportService;

    @Autowired
    private TournamentImportService importService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Exportiert ein Turnier als JSON-Datei.
     *
     * @param gameName der Name des zu exportierenden Spiels
     * @return JSON-Datei als Download
     */
    @GetMapping("/export/{gameName}")
    public ResponseEntity<byte[]> exportTournament(@PathVariable String gameName) {
        try {
            TournamentExportDto dto = exportService.exportTournament(gameName);
            byte[] json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(dto);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setContentDispositionFormData("attachment", "turnier_" + gameName + ".json");
            headers.setContentLength(json.length);

            log.info("Turnier '{}' exportiert ({} bytes)", gameName, json.length);
            return new ResponseEntity<>(json, headers, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            log.warn("Export fehlgeschlagen: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage().getBytes());
        } catch (Exception e) {
            log.error("Fehler beim Export von Turnier '{}'", gameName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Export fehlgeschlagen: " + e.getMessage()).getBytes());
        }
    }

    /**
     * Importiert ein Turnier aus einer JSON-Datei.
     *
     * @param file        die JSON-Datei mit den Turnierdaten
     * @param newGameName der Name fuer das neue Spiel
     * @return Erfolgsmeldung oder Fehlerbeschreibung
     */
    @PostMapping("/import")
    public ResponseEntity<String> importTournament(@RequestParam("file") MultipartFile file,
                                                    @RequestParam("name") String newGameName) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Keine Datei hochgeladen");
            }

            if (newGameName == null || newGameName.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Spielname darf nicht leer sein");
            }

            TournamentExportDto dto = objectMapper.readValue(file.getBytes(), TournamentExportDto.class);
            importService.importTournament(dto, newGameName.trim());

            log.info("Turnier '{}' erfolgreich importiert als '{}'", dto.getOriginalGameName(), newGameName);
            return ResponseEntity.ok("Import erfolgreich");
        } catch (IllegalArgumentException e) {
            log.warn("Import fehlgeschlagen: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(e.getMessage());
        } catch (Exception e) {
            log.error("Fehler beim Import", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Import fehlgeschlagen: " + e.getMessage());
        }
    }
}
