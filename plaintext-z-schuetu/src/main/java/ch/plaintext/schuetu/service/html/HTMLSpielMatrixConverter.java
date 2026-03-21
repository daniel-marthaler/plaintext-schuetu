package ch.plaintext.schuetu.service.html;

import ch.plaintext.schuetu.entity.Gruppe;
import ch.plaintext.schuetu.entity.Kategorie;
import ch.plaintext.schuetu.entity.Mannschaft;
import ch.plaintext.schuetu.entity.Spiel;
import ch.plaintext.schuetu.model.comparators.SpielMannschaftsnamenComparator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class HTMLSpielMatrixConverter {

    private final SimpleDateFormat sdf = new SimpleDateFormat("E HH:mm");
    @Autowired
    private XHTMLOutputUtil xhtml;

    public String generateSpieleTable(final List<Kategorie> list) {
        final StringBuilder stringBuffer = new StringBuilder();
        stringBuffer.append("<table border='1' cellspacing='0' cellpadding='3' width='700'>");

        for (final Kategorie kat : list) {
            if (kat.getGruppeA() == null) {
                log.info("kategorie ohne gruppe a: " + kat.getName());
                continue;
            }
            String col = "";
            if (kat.hasVorUndRueckrunde()) {
                col = "bgcolor='green'";
            } else if (!kat.hasVorUndRueckrunde() && (kat.getGruppeB() != null) && !kat.getGruppeB().getMannschaften().isEmpty()) {
                col = "bgcolor='blue'";
            } else {
                col = "bgcolor='cyan'";
            }
            stringBuffer.append("<tr><td width='10' ").append(col).append(" rowspan='3'>").append(kat.getName().replace(".", "")).append("</td>");
            stringBuffer.append("<td>A: ").append(kat.getGruppeA().getName());
            List<Mannschaft> str = kat.getGruppeA().getMannschaften();
            Boolean rr = null;
            if (kat.hasVorUndRueckrunde()) {
                rr = Boolean.TRUE;
            }
            this.printMannschaften2(stringBuffer, str, rr);
            stringBuffer.append("</td></tr><tr><td>");
            if (kat.getGruppeB() != null) {
                stringBuffer.append("  B: ").append(kat.getGruppeB().getName());
                str = kat.getGruppeB().getMannschaften();
                if (kat.hasVorUndRueckrunde()) {
                    rr = Boolean.FALSE;
                }
                this.printMannschaften2(stringBuffer, str, rr);
            } else {
                stringBuffer.append("B: -</td><td>&nbsp;</td>");
            }
            stringBuffer.append("</td></tr>");
            stringBuffer.append("</td></tr>");

            final Gruppe a = kat.getGruppeA();
            final Gruppe ub = kat.getGruppeB();
            final List<Spiel> spiele = new ArrayList<>();
            for (final Mannschaft mannschaft : a.getMannschaften()) {
                spiele.addAll(mannschaft.getSpiele());
            }
            if (ub != null) {
                for (final Mannschaft mannschaft : ub.getMannschaften()) {
                    spiele.addAll(mannschaft.getSpiele());
                }
            }
            try {
                stringBuffer.append("<tr><td>&nbsp;</td><td><p>Letztes Gruppenspiel: ").append(this.sdf.format(kat.getLatestSpiel().getStart())).append("</p></td></tr>");
            } catch (final Exception e) {
                stringBuffer.append("<tr><td>&nbsp;</td><td><p>Letztes Gruppenspiel: " + "!!!!!!" + "</p></td></tr>");
            }
        }
        stringBuffer.append("</table>");
        return xhtml.cleanup(stringBuffer.toString(), false);
    }

    private void printMannschaften2(final StringBuilder stringBuilder, final List<Mannschaft> str, final Boolean vorrunde) {
        stringBuilder.append("<td>");
        stringBuilder.append("<style type='text/css'>table.inner {border-spacing: 0px; border-padding:0px;width:100%;border:0px; vertical-align:top; overflow:hidden; font-size:10pt; font-family:Arial,sans-serif }td { border:1px solid #000; vertical-align:top; overflow:hidden; }</style>");
        stringBuilder.append("<table class = 'inner'>");
        stringBuilder.append("<tr><td>&nbsp;</td>");
        for (final Mannschaft m1 : str) {
            stringBuilder.append("<td><b>").append(m1.getName()).append("</b></td>");
        }
        int iZeile = 0;
        for (final Mannschaft mannschaft : str) {
            stringBuilder.append("<tr><td>");
            stringBuilder.append("<b>").append(mannschaft.getName()).append("</b>");
            final List<Spiel> tempSpiele = new ArrayList<>();
            if ((vorrunde != null && vorrunde) && mannschaft.getSpiele().size() > 0) {
                tempSpiele.add(mannschaft.getSpiele().get(0));
                tempSpiele.add(mannschaft.getSpiele().get(1));
            } else if ((vorrunde != null && !vorrunde) && mannschaft.getSpiele().size() > 2) {
                tempSpiele.add(mannschaft.getSpiele().get(2));
                tempSpiele.add(mannschaft.getSpiele().get(3));
            } else {
                tempSpiele.addAll(mannschaft.getSpiele());
            }
            int iSpalte = 0;
            boolean linefin = false;
            tempSpiele.sort(new SpielMannschaftsnamenComparator());
            for (final Spiel spiel : tempSpiele) {
                if ((iZeile == iSpalte) || linefin) {
                    stringBuilder.append("<td bgcolor='white'>&nbsp;</td>");
                    linefin = true;
                    continue;
                }
                if (spiel == null) {
                    stringBuilder.append("<td  bgcolor='blue'>");
                } else if (spiel.getToreABestaetigt() > -1) {
                    stringBuilder.append("<td  bgcolor='green'>");
                } else if (spiel.isAmSpielen()) {
                    stringBuilder.append("<td  bgcolor='yellow'>");
                } else {
                    stringBuilder.append("<td>");
                }
                if (spiel == null) {
                    stringBuilder.append("!!!");
                } else {
                    printSpiel(stringBuilder, spiel);
                }
                stringBuilder.append("</td>");
                iSpalte++;
            }
            stringBuilder.append("<td bgcolor='white'>&nbsp;</td>");
            stringBuilder.append("</tr>");
            iZeile++;
        }
        stringBuilder.append("</table>");
        stringBuilder.append("</td>");
    }

    private void printSpiel(StringBuilder stringBuilder, Spiel spiel) {
        stringBuilder.append(spiel.getPlatz()).append(";").append(spiel.getIdString()).append(";");
        if (spiel.getStart() != null) {
            stringBuilder.append(this.sdf.format(spiel.getStart()));
        }
    }
}
