package ch.plaintext.schuetu.service.vorbereitung;

import ch.plaintext.schuetu.entity.Spiel;
import ch.plaintext.schuetu.entity.SpielZeile;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VertauschungsUtilTest {

    private final VertauschungsUtil util = new VertauschungsUtil();

    @Test
    void testKorrekturenVornehmen_NullKorrekturen() {
        List<SpielZeile> zeilen = new ArrayList<>();
        util.korrekturenVornehmen(zeilen, null);
        // should not throw
    }

    @Test
    void testKorrekturenVornehmen_EmptyKorrekturen() {
        List<SpielZeile> zeilen = new ArrayList<>();
        util.korrekturenVornehmen(zeilen, new ArrayList<>());
        // should not throw
    }
}
