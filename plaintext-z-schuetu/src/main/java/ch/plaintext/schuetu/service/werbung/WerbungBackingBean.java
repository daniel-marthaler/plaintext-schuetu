package ch.plaintext.schuetu.service.werbung;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Stellt die Werbungsseite auf die Website
 *
 * TODO: Old framework class EmadIHTTP removed - re-implement HTTP download/posting if needed
 * TODO: Unirest dependency removed - replace with Spring WebClient or RestTemplate
 */
@Component
@Scope("session")
@Data
@Slf4j
public class WerbungBackingBean {

    // TODO: EmadIHTTP removed - re-implement HTTP download
    // @Autowired
    // private EmadIHTTP http;

    public void go() {
        log.info("*** Go: ");
        main();
    }

    public String getLines(String breite) {
        // TODO: Re-implement sponsor line generation from Excel data
        String res = "test";
        return res;
    }

    public List<String> getLinesX(String mappe, byte[] arr) {
        List<String> ret = new ArrayList<>();

        Workbook workbook;
        try {
            workbook = new XSSFWorkbook(new BufferedInputStream(new ByteArrayInputStream(arr)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Iterator<Sheet> sheetIterator = workbook.sheetIterator();
        while (sheetIterator.hasNext()) {
            Sheet sheet = sheetIterator.next();
            if (sheet.getSheetName().contains(mappe)) {
                Iterator<Row> rowIterator = sheet.rowIterator();
                rowIterator.next();
                while (rowIterator.hasNext()) {
                    Row row = rowIterator.next();
                    Iterator<Cell> cellIterator = row.cellIterator();
                    List<String> l = new ArrayList<>();
                    while (cellIterator.hasNext()) {
                        l.add(cellIterator.next().toString());
                    }
                    ret.add(String.join(",", l));
                }
            }
        }

        return ret;
    }

    void main() {
        // TODO: HTTP download removed

        // TODO: Unirest removed - re-implement website update
        // update(getHeaderFront() + getLines("300"), "39");
        // update(getHeaderSponsoren() + getLines("400"), "42");
        log.warn("WerbungBackingBean.main() - not fully implemented, old dependencies removed");
    }

    String getHeaderFront() {
        return "<img style=\"margin-top: 5px; margin-bottom: 5px;\" src=\"https://schuelerturnierworb.imgix.net/sponsoren2.png\" /> <br />";
    }

    String getHeaderSponsoren() {
        return "<h4>Ein herzliches Dankeschoen unseren Sponsoren, Goennern Donatoren und Inserenten</h4>";
    }

    String getLine(String firma, String pic, String link, String breite) {

        if (link.isEmpty() || link.equals("--")) {
            return "";
        }

        if (pic.isEmpty() || pic.equals("--")) {
            return "";
        }

        pic = pic.replace("pdf", "png");

        String lin = link;
        if (!lin.startsWith("https")) {
            lin = "https://" + lin;
        }
        return "<a alt=\"${firma}\" target=\"_blank\" href=\"${lin}\"><img src=\"https://schuelerturnierworb.imgix.net/${pic}?w=${breite}&ar=4:1&fit=fill&fill=solid&fill-color=white&exp=1&border=3,FFFFFF\" /></a><br />"
                .replace("${firma}", firma)
                .replace("${lin}", lin)
                .replace("${pic}", pic)
                .replace("${breite}", breite);
    }

    void update(String update, String id) {
        // TODO: Unirest dependency removed - re-implement with Spring WebClient or RestTemplate
        log.warn("WerbungBackingBean.update() - Unirest dependency removed, not implemented");
    }
}
