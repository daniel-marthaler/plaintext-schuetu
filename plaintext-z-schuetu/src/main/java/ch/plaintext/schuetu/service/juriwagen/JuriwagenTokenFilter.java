package ch.plaintext.schuetu.service.juriwagen;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
public class JuriwagenTokenFilter implements Filter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JuriwagenTokenService tokenService;
    private final ObjectMapper objectMapper;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

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
