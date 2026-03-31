package ch.plaintext.schuetu.service.spieldurchfuehrung;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class SpielDurchfuehrungTrigger {

    private List<SpielDurchfuehrung> durchfuehrungen = new ArrayList<>();

    public void add(SpielDurchfuehrung durch) {
        durchfuehrungen.add(durch);
    }

    @Scheduled(fixedRate = 5000)
    @Transactional
    public void run() {
        log.debug("anzahl durchfuehrungen: " + durchfuehrungen.size());
        for (SpielDurchfuehrung durch : durchfuehrungen) {
            durch.pulse();
        }
    }
}
