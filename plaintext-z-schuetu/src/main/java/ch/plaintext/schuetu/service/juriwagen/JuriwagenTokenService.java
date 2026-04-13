package ch.plaintext.schuetu.service.juriwagen;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class JuriwagenTokenService {

    private final SecretKey secretKey;
    private final int validityHours;

    public JuriwagenTokenService(
            @Value("${plaintext.juriwagen.token-secret:default-dev-secret-change-in-prod-min-32-chars!!}") String secret,
            @Value("${plaintext.juriwagen.token-validity-hours:12}") int validityHours) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
            keyBytes = padded;
        }
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.validityHours = validityHours;
    }

    public String generateToken(Long userId, String username, String gameName, List<String> roles) {
        Instant now = Instant.now();
        Instant expiry = now.plus(validityHours, ChronoUnit.HOURS);

        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("game", gameName)
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(secretKey)
                .compact();
    }

    @SuppressWarnings("unchecked")
    public Optional<JuriwagenTokenClaims> validateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String username = claims.getSubject();
            Long userId = claims.get("userId", Long.class);
            String gameName = claims.get("game", String.class);
            List<String> roles = claims.get("roles", List.class);

            return Optional.of(new JuriwagenTokenClaims(userId, username, gameName, roles));
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Juriwagen token validation failed: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
