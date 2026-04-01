package ch.plaintext.schuetu.service.stats;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * In-memory access statistics tracker. Tracks page visits with
 * anonymized client IPs, user agents, timestamps, and usernames.
 *
 * @author info@emad.ch
 * @since 1.60.0
 */
@Component
@Slf4j
public class AccessStatisticsService {

    private final ConcurrentHashMap<String, Integer> pageAccessCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Integer> userAccessCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Boolean> uniqueClients = new ConcurrentHashMap<>();
    private final CopyOnWriteArrayList<AccessRecord> recentAccesses = new CopyOnWriteArrayList<>();

    private static final int MAX_RECENT = 500;

    /**
     * Records a page access with anonymized IP.
     *
     * @param page      the page URL
     * @param clientIp  the client IP address (last octet will be anonymized)
     * @param userAgent the client user agent string
     * @param username  the authenticated username, or "anonymous"
     */
    public void recordAccess(String page, String clientIp, String userAgent, String username) {
        String anonymizedIp = anonymizeIp(clientIp);
        String user = (username == null || username.isBlank()) ? "anonymous" : username;

        pageAccessCounts.merge(page, 1, Integer::sum);
        userAccessCounts.merge(user, 1, Integer::sum);
        uniqueClients.putIfAbsent(anonymizedIp, Boolean.TRUE);

        AccessRecord record = new AccessRecord(page, anonymizedIp, userAgent, user, LocalDateTime.now());
        recentAccesses.addFirst(record);

        // Trim list if too large
        while (recentAccesses.size() > MAX_RECENT) {
            recentAccesses.removeLast();
        }

        log.debug("Access recorded: page={}, ip={}, user={}", page, anonymizedIp, user);
    }

    /**
     * Returns page access counts sorted descending by count.
     */
    public Map<String, Integer> getPageStats() {
        return pageAccessCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new));
    }

    /**
     * Returns the number of unique client IPs.
     */
    public int getUniqueClients() {
        return uniqueClients.size();
    }

    /**
     * Returns the last N access records.
     */
    public List<AccessRecord> getRecentAccesses(int limit) {
        int end = Math.min(limit, recentAccesses.size());
        return new ArrayList<>(recentAccesses.subList(0, end));
    }

    /**
     * Returns the total number of recorded accesses.
     */
    public int getTotalAccesses() {
        return pageAccessCounts.values().stream().mapToInt(Integer::intValue).sum();
    }

    /**
     * Returns access counts per username sorted descending.
     */
    public Map<String, Integer> getAccessesByUser() {
        return userAccessCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new));
    }

    /**
     * Anonymizes the last octet of an IPv4 address, or the last group of an IPv6 address.
     */
    private String anonymizeIp(String ip) {
        if (ip == null) return "unknown";
        int lastDot = ip.lastIndexOf('.');
        if (lastDot > 0) {
            return ip.substring(0, lastDot) + ".xxx";
        }
        int lastColon = ip.lastIndexOf(':');
        if (lastColon > 0) {
            return ip.substring(0, lastColon) + ":xxxx";
        }
        return "unknown";
    }

    /**
     * Immutable record representing a single page access.
     */
    public record AccessRecord(
            String page,
            String clientIp,
            String userAgent,
            String username,
            LocalDateTime timestamp
    ) {
    }
}
