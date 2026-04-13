package ch.plaintext.schuetu.service.juriwagen;

import java.util.List;

public record JuriwagenTokenClaims(Long userId, String username, String gameName, List<String> roles) {
}
