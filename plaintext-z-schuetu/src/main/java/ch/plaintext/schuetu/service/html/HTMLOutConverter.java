package ch.plaintext.schuetu.service.html;

import ch.plaintext.schuetu.entity.Kategorie;
import ch.plaintext.schuetu.entity.Spiel;
import ch.plaintext.schuetu.model.enums.SpielEnum;
import ch.plaintext.schuetu.model.ranglistensortierung.RanglisteneintragHistorie;
import ch.plaintext.schuetu.model.ranglistensortierung.RanglisteneintragZeile;
import ch.plaintext.schuetu.repository.KategorieRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class HTMLOutConverter {

    public static final String TABLE = "<style type='text/css'>table.normal {border-spacing: 0px; border-padding:0px;width:600px;border:1px solid #000; vertical-align:top; overflow:hidden; font-size:10pt; font-family:Arial,sans-serif }td { border:1px solid #000; vertical-align:top; overflow:hidden; }</style><table class='normal'>";
    public static final String TD_COLSPAN_1 = "<td colspan='1'>";
    public static final String TD = "<td>";
    public static final String STRICH = " -";
    public static final String STRICH2 = " - ";

    @Autowired
    private XHTMLOutputUtil util;

    @Autowired
    private HTMLMenu menu;

    @Autowired
    private KategorieRepository katRepo;

    private String path = "";

    private static void getTitelzeile(final StringBuilder b, final String name) {
        b.append(HTMLTags.TR);
        b.append(HTMLTags.TR);
        b.append("<td colspan='7'>");
        b.append("<b>").append(name).append("</b>");
        b.append(HTMLTags.TD_E);
        b.append(HTMLTags.TR_E);
        b.append(HTMLTags.TD);
        b.append("<b>Nr.</b>");
        b.append(HTMLTags.TD_E);
        b.append(HTMLTags.TD);
        b.append("<b>Startzeit</b>");
        b.append(HTMLTags.TD_E);
        b.append(HTMLTags.TD);
        b.append("<b>Platz</b>");
        b.append(HTMLTags.TD_E);
        b.append(HTMLTags.TD);
        b.append("<b>Mannschaft A</b>");
        b.append(HTMLTags.TD_E);
        b.append(HTMLTags.TD);
        b.append(HTMLTags.TD_E);
        b.append(HTMLTags.TD);
        b.append("<b>Mannschaft B</b>");
        b.append(HTMLTags.TD_E);
        b.append(HTMLTags.TD);
        b.append(HTMLTags.TD_E);
        b.append(HTMLTags.TR_E);
    }

    private static String getSpielRow(final List<Spiel> gruppen) {
        final StringBuilder builder = new StringBuilder();
        int i = 1;
        for (final Spiel spiel : gruppen) {
            if (spiel == null) {
                continue;
            }
            builder.append(HTMLTags.TR);
            builder.append(TD);
            builder.append(i);
            i++;
            builder.append(HTMLTags.TD_E);
            builder.append(TD);
            final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM HH:mm");
            builder.append(sdf.format(spiel.getStart()));
            builder.append(HTMLTags.TD_E);
            builder.append(TD);
            builder.append(spiel.getPlatz());
            builder.append(HTMLTags.TD_E);
            builder.append(TD);
            if (spiel.getMannschaftA() != null) {
                builder.append(spiel.getMannschaftA().getName());
            } else {
                if (spiel.getGruppe() != null) {
                    builder.append(spiel.getTyp()).append(" ").append(spiel.getGruppe().getName());
                } else {
                    builder.append(spiel.getTyp()).append(STRICH);
                }
            }
            builder.append(HTMLTags.TD_E);
            if (spiel.getToreABestaetigt() < 0) {
                builder.append("<td bgcolor='red'>");
            } else {
                builder.append(TD);
                builder.append(spiel.getToreABestaetigt());
            }
            builder.append(HTMLTags.TD_E);
            builder.append(TD);
            if (spiel.getMannschaftB() != null) {
                builder.append(spiel.getMannschaftB().getName());
            } else {
                if (spiel.getGruppe() != null) {
                    builder.append(spiel.getTyp()).append(" ").append(spiel.getGruppe().getName());
                } else {
                    builder.append(spiel.getTyp()).append(STRICH);
                }
            }
            builder.append(HTMLTags.TD_E);
            if (spiel.getToreBBestaetigt() < 0) {
                builder.append("<td bgcolor='red'>");
            } else {
                builder.append(TD);
                builder.append(spiel.getToreBBestaetigt());
            }
            builder.append(HTMLTags.TR_E);
        }
        return builder.toString();
    }

    public String getRangliste(RanglisteneintragHistorie historieIn) {
        RanglisteneintragHistorie historie = historieIn;
        if (historie == null) {
            return "";
        }
        final StringBuilder stringBuilder = new StringBuilder();
        Kategorie kategorie = historie.getKategorie();
        boolean first = true;
        do {
            if (!first) {
                historie = historie.getVorherigerEintrag();
            }
            Spiel spiel = historie.getSpiel();
            first = false;
            stringBuilder.append(HTMLTags.BR);
            stringBuilder.append("<table border='1' cellspacing='0' cellpadding='3' width='700'>");
            stringBuilder.append(HTMLTags.TR);
            stringBuilder.append("<td colspan='8'>");
            final SimpleDateFormat sdf = new SimpleDateFormat("EEE HH:mm:ss");
            if (spiel != null) {
                stringBuilder.append(HTMLTags.P);
                if (spiel.getTyp() == SpielEnum.GFINAL) {
                    stringBuilder.append(kategorie.getName()).append(STRICH2).append(sdf.format(historie.getSpiel().getStart())).append(" - Finale </p>");
                } else if (spiel.getTyp() == SpielEnum.KFINAL) {
                    stringBuilder.append(kategorie.getName()).append(STRICH2).append(sdf.format(historie.getSpiel().getStart())).append(" - kleiner Finale</p>");
                } else {
                    if (kategorie.getGruppeB() != null) {
                        stringBuilder.append(kategorie.getGruppeA().getName()).append(STRICH2).append(sdf.format(historie.getSpiel().getStart())).append("");
                    } else {
                        stringBuilder.append(kategorie.getName()).append(STRICH2).append(sdf.format(historie.getSpiel().getStart())).append(" - Spiele: ").append("</p>");
                    }
                }
            } else {
                stringBuilder.append("<p>").append(kategorie.getName().replace(".", "")).append(" - Penalty: ").append(HTMLTags.P_E);
            }
            stringBuilder.append(HTMLTags.TD_E);
            stringBuilder.append(HTMLTags.TR_E);
            stringBuilder.append(HTMLTags.TR);
            stringBuilder.append("<td colspan='8'>");
            if (spiel != null) {
                stringBuilder.append("spiel: ").append(historie.getSpiel().getMannschaftA().getName()).append("-").append(historie.getSpiel().getMannschaftB().getName()).append(" ").append(historie.getSpiel().getToreABestaetigt()).append(":").append(historie.getSpiel().getToreBBestaetigt()).append("");
            } else {
                stringBuilder.append("spiel: Penalty");
            }
            stringBuilder.append(HTMLTags.TD_E);
            stringBuilder.append(HTMLTags.TR_E);
            stringBuilder.append(HTMLTags.TR);
            stringBuilder.append(TD_COLSPAN_1);
            stringBuilder.append("<p>Mannschaft</p>");
            stringBuilder.append(HTMLTags.TD_E);
            stringBuilder.append(TD_COLSPAN_1);
            stringBuilder.append("<p>Sp. gespielt</p>");
            stringBuilder.append(HTMLTags.TD_E);
            stringBuilder.append(TD_COLSPAN_1);
            stringBuilder.append("<p>Sp. anstehend</p>");
            stringBuilder.append(HTMLTags.TD_E);
            stringBuilder.append(TD_COLSPAN_1);
            stringBuilder.append("<p>Punkte</p>");
            stringBuilder.append(HTMLTags.TD_E);
            stringBuilder.append(TD_COLSPAN_1);
            stringBuilder.append("<p>Tordifferenz (Tore erz. - erh.)</p>");
            stringBuilder.append(HTMLTags.TD_E);
            stringBuilder.append(TD_COLSPAN_1);
            stringBuilder.append("<p>Rangierungsgrund</p>");
            stringBuilder.append(HTMLTags.TD_E);
            generateZeilen(historie, stringBuilder);
        } while (historie.getVorherigerEintrag() != null);
        return stringBuilder.toString();
    }

    private void generateZeilen(RanglisteneintragHistorie gr, StringBuilder stringBuilder) {
        final List<RanglisteneintragZeile> zeilen = gr.getZeilen();
        for (final RanglisteneintragZeile ranglisteneintragZeile : zeilen) {
            stringBuilder.append(HTMLTags.TR);
            if (ranglisteneintragZeile.getMannschaft().isMemberofGroupA()) {
                stringBuilder.append(TD_COLSPAN_1);
            } else {
                stringBuilder.append("<td colspan='1' bgcolor='gray'>");
            }
            stringBuilder.append("").append(ranglisteneintragZeile.getMannschaft().getName()).append("");
            stringBuilder.append(HTMLTags.TD_E);
            stringBuilder.append(TD_COLSPAN_1);
            stringBuilder.append("").append(ranglisteneintragZeile.getSpieleVorbei()).append("");
            stringBuilder.append(HTMLTags.TD_E);
            stringBuilder.append(TD_COLSPAN_1);
            stringBuilder.append("").append(ranglisteneintragZeile.getSpieleAnstehend()).append("");
            stringBuilder.append(HTMLTags.TD_E);
            stringBuilder.append(TD_COLSPAN_1);
            stringBuilder.append("").append(ranglisteneintragZeile.getPunkte()).append("");
            stringBuilder.append(HTMLTags.TD_E);
            stringBuilder.append(TD_COLSPAN_1);
            stringBuilder.append("").append(ranglisteneintragZeile.getToreErziehlt()).append("-").append(ranglisteneintragZeile.getToreKassiert()).append("=").append(ranglisteneintragZeile.getTordifferenz());
            stringBuilder.append(HTMLTags.TD_E);
            final int diferenz = gr.compareWithLast(ranglisteneintragZeile);
            if (diferenz < 0) {
                stringBuilder.append("<td colspan='1' bgcolor='red'>");
            } else if (diferenz > 0) {
                stringBuilder.append("<td colspan='1' bgcolor='green'>");
            } else {
                stringBuilder.append(TD_COLSPAN_1);
            }
            stringBuilder.append(ranglisteneintragZeile.getRangierungsgrund().toString());
            stringBuilder.append(HTMLTags.TD_E);
            stringBuilder.append(HTMLTags.TR_E);
        }
        stringBuilder.append("</table>");
    }

    public String convertSpiele(final List<Spiel> gruppen, final List<Spiel> finale) {
        final StringBuilder builder = new StringBuilder();
        builder.append(HTMLTags.BR);
        builder.append(TABLE);
        HTMLOutConverter.getTitelzeile(builder, "Gruppenspiele");
        builder.append(HTMLOutConverter.getSpielRow(gruppen));
        HTMLOutConverter.getTitelzeile(builder, "Finalspiele");
        builder.append(HTMLOutConverter.getSpielRow(finale));
        builder.append("</table>");
        return builder.toString();
    }

    public void dumpoutPages() {
        List<Kategorie> liste = katRepo.findAll();
        for (Kategorie kategorie : liste) {
            String page = generatePageKategorie(kategorie);
            try {
                FileUtils.writeStringToFile(new File(this.path + "/website/" + kategorie.getName() + ".html"), page);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    public String generatePageIndex() {
        return util.cleanup(menu.generateMenu(""), false);
    }

    public String generatePageKategorie(Kategorie kategorie) {
        final StringBuilder builder = new StringBuilder();
        builder.append(menu.generateMenu(kategorie.getName()));
        List<Spiel> finale = new ArrayList<>();
        List<Spiel> gruppen = kategorie.getSpieleSorted();
        finale.add(kategorie.getGrosserFinal());
        finale.add(kategorie.getKleineFinal());
        builder.append(this.convertSpiele(gruppen, finale));
        return this.util.cleanup(builder.toString(), false);
    }

    public void setPath(String path) {
        this.path = path + System.getProperty("file.separator") + "sound";
    }

}
