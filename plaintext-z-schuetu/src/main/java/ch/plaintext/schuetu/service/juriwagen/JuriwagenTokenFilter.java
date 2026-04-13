package ch.plaintext.schuetu.service.juriwagen;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
public class JuriwagenTokenFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String API_PATH = "/api/juriwagen/";

    private final JuriwagenTokenService tokenService;
    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith(API_PATH);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "TOKEN_MISSING", "Authorization header required");
            return;
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        var claims = tokenService.validateToken(token);
        if (claims.isEmpty()) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "TOKEN_INVALID", "Invalid or expired token");
            return;
        }

        JuriwagenTokenClaims jwt = claims.get();

        var authorities = jwt.roles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                .toList();

        var authentication = new UsernamePasswordAuthenticationToken(jwt, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    private void sendError(HttpServletResponse response, int status, String error, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        objectMapper.writeValue(response.getOutputStream(), Map.of(
                "status", status,
                "error", error,
                "message", message,
                "timestamp", Instant.now().toString()
        ));
    }
}
