package ch.plaintext.schuetu.service.html;

import ch.plaintext.schuetu.service.websiteinfo.model.KlassenrangZeile;
import ch.plaintext.schuetu.entity.Mannschaft;
import ch.plaintext.schuetu.model.enums.GeschlechtEnum;
import ch.plaintext.schuetu.model.ranglistensortierung.RanglisteneintragHistorie;
import ch.plaintext.schuetu.model.ranglistensortierung.RanglisteneintragZeile;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * konvertiert die RanglisteneintragHistorie in ein Endresultat
 */
@Component
public class ModelConverterRangliste {

    private final String[] kategorien = {"M1", "M2", "M3", "M4", "M5", "M6", "M7", "M8", "M9", "K1", "K2", "K3", "K4", "K5", "K6", "K7", "K8", "K9"};

    public List<KlassenrangZeile> convertKlassenrangZeile(final Collection<RanglisteneintragHistorie> kat) {

        List<KlassenrangZeile> ret = new ArrayList<>();

        for (final String k : this.kategorien) {

            GeschlechtEnum geschlecht = null;
            if (k.contains("K")) {
                geschlecht = GeschlechtEnum.K;
            } else {
                geschlecht = GeschlechtEnum.M;
            }

            int klasse = Integer.parseInt(k.substring(1, 2));
            final List<Mannschaft> mannschaften = this.getRangliste(kat, klasse, geschlecht);

            KlassenrangZeile zeile = new KlassenrangZeile();
            zeile.setGeschlecht(geschlecht);
            zeile.setKlasse(klasse);

            for (int i = 0; i < 4; i++) {
                Mannschaft ma = null;
                if ((mannschaften.size() > 0) && (mannschaften.size() > i)) {
                    ma = mannschaften.get(i);
                }

                if ((ma != null && ma.getGruppe().getKategorie().isFertigGespielt())) {
                    if (!BooleanUtils.isTrue(ma.getDisqualifiziert())) {
                        zeile.addNext(ma);
                    }
                }
            }
            ret.add(zeile);

        }
        return ret;
    }

    public List<KlassenrangZeile> convertKlassenrangZeileVerkuendigung(final Collection<RanglisteneintragHistorie> kat) {

        List<KlassenrangZeile> ret = new ArrayList<>();

        for (final String k : this.kategorien) {

            GeschlechtEnum geschlecht = null;
            if (k.contains("K")) {
                geschlecht = GeschlechtEnum.K;
            } else {
                geschlecht = GeschlechtEnum.M;
            }

            int klasse = Integer.parseInt(k.substring(1, 2));
            final List<Mannschaft> mannschaften = this.getRangliste(kat, klasse, geschlecht);

            KlassenrangZeile zeile = new KlassenrangZeile();
            zeile.setGeschlecht(geschlecht);
            zeile.setKlasse(klasse);

            for (int i = 2; i > -1; i--) {
                Mannschaft ma = null;
                if ((mannschaften.size() > 0) && (mannschaften.size() > i)) {
                    ma = mannschaften.get(i);
                }

                if ((ma != null && ma.getGruppe().getKategorie().isFertigGespielt())) {
                    zeile.addNext(ma);
                } else {
                    zeile.addNext(new Mannschaft());
                }
            }
            ret.add(zeile);

        }
        return ret;
    }

    private List<Mannschaft> getRangliste(final Collection<RanglisteneintragHistorie> kategorien, int klasse, GeschlechtEnum geschlecht) {

        ArrayList<Mannschaft> res = new ArrayList<>();

        for (RanglisteneintragHistorie ranglisteneintragHistorie : kategorien) {
            List<RanglisteneintragZeile> m = ranglisteneintragHistorie.getZeilen();

            for (RanglisteneintragZeile temp : m) {

                if (temp.getMannschaft().getGeschlecht() == geschlecht) {
                    if (temp.getMannschaft().getKlasse() == klasse) {
                        res.add(temp.getMannschaft());
                    }
                }
            }
        }
        return res;
    }
}
