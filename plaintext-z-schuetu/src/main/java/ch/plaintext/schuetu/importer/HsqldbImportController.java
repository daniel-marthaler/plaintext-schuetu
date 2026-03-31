package ch.plaintext.schuetu.importer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * REST Controller for importing HSQLDB .script files into the PostgreSQL database.
 * Endpoint is under /nosec/ so no authentication is required.
 */
@RestController
@RequestMapping("/nosec/api/import")
@Slf4j
public class HsqldbImportController {

    @Autowired
    private HsqldbImportService importService;

    @PostMapping("/hsqldb")
    public ResponseEntity<Map<String, Object>> importHsqldb(@RequestParam("file") MultipartFile file) {
        log.info("HSQLDB import started, file: {}, size: {} bytes", file.getOriginalFilename(), file.getSize());

        try {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            Map<String, Object> result = importService.importFromScript(content);
            log.info("HSQLDB import completed successfully: {}", result);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("HSQLDB import failed", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName(),
                    "status", "FAILED"
            ));
        }
    }
}
