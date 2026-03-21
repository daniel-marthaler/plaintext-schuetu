package ch.plaintext.schuetu.service.vorbereitung.helper;

import ch.plaintext.schuetu.entity.GameModel;
import ch.plaintext.schuetu.entity.Kategorie;
import ch.plaintext.schuetu.entity.Spiel;
import ch.plaintext.schuetu.model.enums.SpielEnum;

public class SpielRealNameHelperVorSpielzuteilung {

    public static void setRealNameKlein(Kategorie kategorie, GameModel model, Spiel kf) {
        if (kategorie.isMixedKlassen() && model.isBehandleFinaleProKlassebeiZusammengefuehrten() && !kategorie.isMixedAndWithEinzelklasse() && kategorie.computeAnzahlFinale() == 2) {
            kf.setTyp(SpielEnum.GFINAL);
            if (kategorie.computeAnzahlFinale() > 1) {
                kf.setRealName("GrFin-" + kategorie.getGroessereMannschaftsGruppe().get(1).getGeschlecht() + "Kl" + kategorie.getGroessereMannschaftsGruppe().get(0).getKlasse() + "&" + kategorie.getKleinereMannschaftsGruppe().get(0).getKlasse());
            } else {
                kf.setRealName("GrFin-" + kategorie.getGroessereMannschaftsGruppe().get(1).getGeschlecht() + "Kl" + kategorie.getGroessereMannschaftsGruppe().get(0).getKlasse());
            }
            if (kategorie.computeAnzahlFinale() > 1) {
                kf.setRealName(kf.getRealName() + " (Kl. " + kategorie.getKleinereMannschaftsGruppe().get(1).getKlasse() + ")");
            }
        } else {
            kf.setTyp(SpielEnum.KFINAL);
        }
    }

    public static void setRealGross(Kategorie kategorie, GameModel model, Spiel gf) {
        if (kategorie.isMixedKlassen() && model.isBehandleFinaleProKlassebeiZusammengefuehrten() && !kategorie.isMixedAndWithEinzelklasse()) {
            if (kategorie.computeAnzahlFinale() > 1) {
                gf.setRealName("GrFin-" + kategorie.getGroessereMannschaftsGruppe().get(1).getGeschlecht() + "Kl" + kategorie.getGroessereMannschaftsGruppe().get(0).getKlasse() + "&" + kategorie.getKleinereMannschaftsGruppe().get(0).getKlasse());
            } else {
                gf.setRealName("GrFin-" + kategorie.getGroessereMannschaftsGruppe().get(1).getGeschlecht() + "Kl" + kategorie.getGroessereMannschaftsGruppe().get(0).getKlasse());
            }
            if (kategorie.computeAnzahlFinale() > 1) {
                gf.setRealName(gf.getRealName() + " (Kl. " + kategorie.getGroessereMannschaftsGruppe().get(1).getKlasse() + ")");
            }
        } else if (kategorie.isMixedAndWithEinzelklasse()) {
            gf.setRealName("GrFin-" + kategorie.getGroessereMannschaftsGruppe().get(0).getGeschlecht() + "Kl" + kategorie.getGroessereMannschaftsGruppe().get(0).getKlasse() + gf.getKlasse());
        }
    }
}
