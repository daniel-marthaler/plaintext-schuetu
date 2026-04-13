package ch.plaintext.schuetu.web.controllers;

import ch.plaintext.PlaintextSecurity;
import ch.plaintext.schuetu.service.GameRoot;
import ch.plaintext.schuetu.service.juriwagen.JuriwagenTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class JuriwagenTokenController {

    private final JuriwagenTokenService tokenService;
    private final PlaintextSecurity plaintextSecurity;
    private final GameRoot gameRoot;

    @GetMapping("/schiri-token")
    @PreAuthorize("hasAnyRole('speaker', 'admin')")
    public String getToken(@RequestParam String game) {
        var gameModel = gameRoot.selectGame(game);
        if (gameModel == null) {
            log.warn("Juriwagen token request for unknown game: {}", game);
            return "redirect:/index.html";
        }

        List<String> roles = List.of("speaker");
        if (plaintextSecurity.ifGranted("admin")) {
            roles = List.of("speaker", "admin");
        }

        String token = tokenService.generateToken(
                plaintextSecurity.getId(),
                plaintextSecurity.getUser(),
                game,
                roles
        );

        log.info("Juriwagen token generated for user {} game {}", plaintextSecurity.getUser(), game);
        return "redirect:/nosec/speaker-app?token=" + token;
    }
}
