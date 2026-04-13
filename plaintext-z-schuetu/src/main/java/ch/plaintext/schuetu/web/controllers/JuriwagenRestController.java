package ch.plaintext.schuetu.web.controllers;

import ch.plaintext.schuetu.service.juriwagen.JuriwagenService;
import ch.plaintext.schuetu.service.juriwagen.JuriwagenTokenClaims;
import ch.plaintext.schuetu.service.juriwagen.dto.JuriwagenStatusDto;
import ch.plaintext.schuetu.service.juriwagen.dto.ZeitDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/juriwagen")
@RequiredArgsConstructor
public class JuriwagenRestController {

    private final JuriwagenService juriwagenService;

    @GetMapping("/status")
    public ResponseEntity<JuriwagenStatusDto> getStatus(@AuthenticationPrincipal JuriwagenTokenClaims claims) {
        JuriwagenStatusDto status = juriwagenService.getStatus(claims.gameName());
        if (status == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(status);
    }

    @GetMapping("/zeit")
    public ResponseEntity<ZeitDto> getZeit(@AuthenticationPrincipal JuriwagenTokenClaims claims) {
        ZeitDto zeit = juriwagenService.getZeit(claims.gameName());
        if (zeit == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(zeit);
    }

    @PostMapping("/vorbereitet")
    public ResponseEntity<Map<String, String>> vorbereitet(@AuthenticationPrincipal JuriwagenTokenClaims claims) {
        juriwagenService.vorbereitet(claims.gameName());
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PostMapping("/spielen")
    public ResponseEntity<Map<String, String>> spielen(@AuthenticationPrincipal JuriwagenTokenClaims claims) {
        juriwagenService.spielen(claims.gameName());
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PostMapping("/beenden")
    public ResponseEntity<Map<String, String>> beenden(@AuthenticationPrincipal JuriwagenTokenClaims claims) {
        juriwagenService.beenden(claims.gameName());
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PostMapping("/enter")
    public ResponseEntity<Map<String, String>> enter(@AuthenticationPrincipal JuriwagenTokenClaims claims) {
        juriwagenService.enter(claims.gameName());
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PostMapping("/penalty/{id}/gespielt")
    public ResponseEntity<Map<String, String>> penaltyGespielt(
            @AuthenticationPrincipal JuriwagenTokenClaims claims,
            @PathVariable String id) {
        juriwagenService.penaltyGespielt(claims.gameName(), id);
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PostMapping("/zeit/add")
    public ResponseEntity<Map<String, String>> zeitAdd(
            @AuthenticationPrincipal JuriwagenTokenClaims claims,
            @RequestParam(defaultValue = "60") int seconds) {
        juriwagenService.zeitAdd(claims.gameName(), seconds);
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PostMapping("/zeit/einholen60")
    public ResponseEntity<Map<String, String>> zeitEinholen60(@AuthenticationPrincipal JuriwagenTokenClaims claims) {
        juriwagenService.zeitEinholen60(claims.gameName());
        return ResponseEntity.ok(Map.of("status", "ok"));
    }
}
