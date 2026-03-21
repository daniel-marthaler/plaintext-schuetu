package ch.plaintext.schuetu.service.utils;

import ch.plaintext.schuetu.entity.Kategorie;
import ch.plaintext.schuetu.entity.Mannschaft;
import ch.plaintext.schuetu.model.comperators.MannschaftsNamenComperator;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Helper for debug output
 */
@Slf4j
public class SysoutHelper {

    public static final String BEGRENZER = "*************************************";

    public static void printKategorieList(List<Kategorie> map) {
        log.info("");
        log.info(BEGRENZER);

        for (Kategorie key : map) {
            String name = (key.getName() + "                           ").substring(0, 10);
            if (key.getGruppeA() == null) {
                continue;
            }

            List<Mannschaft> listA = key.getGruppeA().getMannschaften();
            listA.sort(new MannschaftsNamenComperator());

            List<Mannschaft> listB = key.getGruppeB().getMannschaften();
            listB.sort(new MannschaftsNamenComperator());

            log.info("" + " " + name + "   a --> " + listA);
            if (key.getGruppeB() != null) {
                log.info("" + "              b --> " + listB);
            } else {
                log.info("" + "              --> ");
            }

            log.info("" + "              -->       spiele: " + key.getSpiele().size());
            log.info("" + "              --> mannschaften: " + key.getMannschaften().size());
            log.info("");
        }
        log.info("" + BEGRENZER);
        log.info("");
    }

}
