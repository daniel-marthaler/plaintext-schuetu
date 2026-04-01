package ch.plaintext.schuetu.service.stats;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.Principal;
import java.util.Set;

/**
 * Servlet filter that records page accesses via {@link AccessStatisticsService}.
 * Skips static resources (CSS, JS, images, fonts, favicon).
 *
 * @author info@emad.ch
 * @since 1.60.0
 */
@Component
@Order(1)
@Slf4j
public class AccessStatisticsFilter implements Filter {

    private static final Set<String> SKIP_EXTENSIONS = Set.of(
            ".css", ".js", ".png", ".jpg", ".jpeg", ".gif", ".svg",
            ".ico", ".woff", ".woff2", ".ttf", ".eot", ".map"
    );

    private static final Set<String> SKIP_PREFIXES = Set.of(
            "/jakarta.faces.resource", "/javax.faces.resource",
            "/resources/", "/static/"
    );

    @Autowired
    private AccessStatisticsService statisticsService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (request instanceof HttpServletRequest httpRequest) {
            String uri = httpRequest.getRequestURI();

            if (!shouldSkip(uri)) {
                String clientIp = getClientIp(httpRequest);
                String userAgent = httpRequest.getHeader("User-Agent");
                Principal principal = httpRequest.getUserPrincipal();
                String username = (principal != null) ? principal.getName() : "anonymous";

                statisticsService.recordAccess(uri, clientIp, userAgent, username);
            }
        }

        chain.doFilter(request, response);
    }

    private boolean shouldSkip(String uri) {
        if (uri == null) return true;
        String lower = uri.toLowerCase();
        for (String ext : SKIP_EXTENSIONS) {
            if (lower.endsWith(ext)) return true;
        }
        for (String prefix : SKIP_PREFIXES) {
            if (lower.contains(prefix)) return true;
        }
        return false;
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
