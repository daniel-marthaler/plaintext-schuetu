package ch.plaintext.schuetu.web;

import ch.plaintext.schuetu.service.stats.AccessStatisticsService;
import ch.plaintext.schuetu.service.stats.AccessStatisticsService.AccessRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Backing bean for the access statistics page (statistik.xhtml).
 * Wraps {@link AccessStatisticsService} for JSF EL access.
 *
 * @author info@emad.ch
 * @since 1.60.0
 */
@Controller
@Scope("session")
@Slf4j
public class StatistikBackingBean {

    @Autowired
    private AccessStatisticsService accessStatisticsService;

    public int getTotalAccesses() {
        return accessStatisticsService.getTotalAccesses();
    }

    public int getUniqueClients() {
        return accessStatisticsService.getUniqueClients();
    }

    public List<PageStatEntry> getPageStats() {
        List<PageStatEntry> list = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : accessStatisticsService.getPageStats().entrySet()) {
            list.add(new PageStatEntry(entry.getKey(), entry.getValue()));
        }
        return list;
    }

    public List<UserStatEntry> getUserStats() {
        List<UserStatEntry> list = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : accessStatisticsService.getAccessesByUser().entrySet()) {
            list.add(new UserStatEntry(entry.getKey(), entry.getValue()));
        }
        return list;
    }

    public List<AccessRecord> getRecentAccesses() {
        return accessStatisticsService.getRecentAccesses(50);
    }

    /**
     * DTO for page statistics table.
     */
    public record PageStatEntry(String page, int count) {
    }

    /**
     * DTO for user statistics table.
     */
    public record UserStatEntry(String username, int count) {
    }
}
