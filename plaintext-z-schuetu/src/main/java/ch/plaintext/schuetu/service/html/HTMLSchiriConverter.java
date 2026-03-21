package ch.plaintext.schuetu.service.html;

import ch.plaintext.schuetu.entity.Spiel;
import ch.plaintext.schuetu.model.enums.SpielEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class HTMLSchiriConverter {

    private static final String TR = "<tr>";
    private static final String TD = "<td> ";
    private static final String TD_E = "</td>";
    private static final String TR_E = "</tr>";
    private static final String TABLE_E = "</table>";
    private static final String BR = "<br>";
    private static final String TBODY_E = "</tbody>";

    @Autowired
    private XHTMLOutputUtil xhtml;

    public String getTable(final List<Spiel> list) {
        String responseString;
        final List<String> listT = new ArrayList<>();

        for (final Spiel spiel : list) {
            if (spiel.getPlatz() == null) {
                HTMLSchiriConverter.log.warn("spielkorrektur gefunden ohne Platz... werde dieses ueberspringen... " + spiel.toString());
                continue;
            }
            String nameA = "";
            if (spiel.getMannschaftA() == null) {
                if (spiel.getTyp() == SpielEnum.GFINAL) {
                    nameA = "GrFin-" + spiel.getKategorieName();
                }
                if (spiel.getTyp() == SpielEnum.KFINAL) {
                    nameA = "KlFin-" + spiel.getKategorieName();
                }
            } else {
                nameA = spiel.getMannschaftA().getName();
            }
            String nameB = "";
            if (spiel.getMannschaftB() != null) {
                nameB = spiel.getMannschaftB().getName();
            }
            final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            String ret = getTemplate2().replace("[zeit]", sdf.format(spiel.getStart()));
            ret = ret.replace("[idstring]", spiel.getIdString().toUpperCase());
            ret = ret.replace("[platz]", spiel.getPlatz().toString());
            ret = ret.replace("[a]", nameA);
            ret = ret.replace("[b]", nameB);
            if (spiel.getMannschaftA() != null) {
                ret = ret.replace("[farbea]", spiel.getMannschaftA().getFarbe());
            } else {
                ret = ret.replace("[farbea]", "____________");
            }
            if (spiel.getMannschaftB() != null) {
                ret = ret.replace("[farbeb]", spiel.getMannschaftB().getFarbe());
            } else {
                ret = ret.replace("[farbeb]", "____________");
            }
            listT.add(ret);
        }

        int i = 0;
        StringBuilder responseStringBuilder = new StringBuilder();
        for (final String string : listT) {
            if (i % 2 == 0) {
                if ((i % 12 == 0) && (i > 0)) {
                    responseStringBuilder.append("<table class='bb' border='0' cellspacing='0' cellpadding='3' width='750' style=\"page-break-after:always;\">");
                } else {
                    responseStringBuilder.append("<table border='0' cellspacing='0' cellpadding='3' width='750'>");
                }
                responseStringBuilder.append(TR);
            }
            responseStringBuilder.append(TD).append(string).append(TD_E);
            if (i % 2 == 2) {
                responseStringBuilder.append(TR_E);
                responseStringBuilder.append(TABLE_E);
            }
            i++;
        }
        responseString = responseStringBuilder.toString();
        responseString = responseString + BR;
        return xhtml.cleanup(responseString, true);
    }

    private String getTemplate2() {
        return "<table style=\"border:2px solid black;\" border='2' cellpadding=\"0\" cellspacing=\"0\" height=\"122\"" +
                "width=\"350\">" +
                "<tbody>" +
                TR +
                "<td colspan=\"2\"><b>&nbsp;Platz [platz] um [zeit]&nbsp;&nbsp;</b> </td>" +
                TR_E +
                TR +
                "<td colspan=\"1\" align=\"left\"><b>&nbsp;[a]</b>&nbsp; Farbe:" +
                "[farbea]&nbsp;&nbsp; Tore:</td>" +
                "<td rowspan=\"4\" colspan=\"1\"><h1>[idstring]</h1>" +
                "</td>" +
                TR_E +
                "<tr align=\"center\">" +
                "<td colspan=\"1\" rowspan=\"1\">" +
                "<input name=\"1\" value=\"1\" type=\"checkbox\"> 1 " + "<input name=\"1\" value=\"1\" type=\"checkbox\"> 2 " + "<input name=\"1\" value=\"1\" type=\"checkbox\"> 3 "
                + "<input name=\"1\" value=\"1\" type=\"checkbox\"> 4 " + "<input name=\"1\" value=\"1\" type=\"checkbox\"> 5 " + "<input name=\"1\" value=\"1\" type=\"checkbox\"> 6 "
                + "<input name=\"1\" value=\"1\" type=\"checkbox\"> 7 " + "<input name=\"1\" value=\"1\" type=\"checkbox\"> 8 " + TD_E +
                TR_E +
                TR +
                "<td colspan=\"1\" align=\"left\"><b>&nbsp;[b]</b>&nbsp; Farbe:" +
                "[farbeb]&nbsp;&nbsp; Tore:</td>" +
                TR_E +
                "<tr align=\"center\">" +
                "<td colspan=\"1\" rowspan=\"1\">" + "<input name=\"1\" value=\"1\" type=\"checkbox\"> 1 " + "<input name=\"1\" value=\"1\" type=\"checkbox\"> 2 "
                + "<input name=\"1\" value=\"1\" type=\"checkbox\"> 3 " + "<input name=\"1\" value=\"1\" type=\"checkbox\"> 4 " + "<input name=\"1\" value=\"1\" type=\"checkbox\"> 5 "
                + "<input name=\"1\" value=\"1\" type=\"checkbox\"> 6 " + "<input name=\"1\" value=\"1\" type=\"checkbox\"> 7 " + "<input name=\"1\" value=\"1\" type=\"checkbox\"> 8" + TD_E +
                TR_E +
                TBODY_E +
                TABLE_E;
    }

}
