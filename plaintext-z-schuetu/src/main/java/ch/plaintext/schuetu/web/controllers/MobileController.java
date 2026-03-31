package ch.plaintext.schuetu.web.controllers;

import ch.plaintext.schuetu.service.mobile.MatchInfoService;
import ch.plaintext.schuetu.service.utils.DateUtil;
import ch.plaintext.schuetu.entity.Spiel;
import ch.plaintext.schuetu.model.comparators.SpielZeitComparator;
import ch.plaintext.schuetu.model.enums.SpielEnum;
import ch.plaintext.schuetu.repository.SpielRepository;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Controller fuer die Mobile Spielanzeige
 *
 * @author info@emad.ch
 * @since 1.2.8
 */
@Component
@Scope("session")
@Slf4j
public class MobileController {

    @Autowired
    private SpielRepository srepo;
    @Autowired
    private MatchInfoService matchinfo;

    @Getter
    private Map<String, String> mannschaften = new TreeMap<>();

    private MobileSpiel finale;

    @Getter
    @Setter
    private String mannschaftAuswahl;

    @PostConstruct
    private void init() {
        matchinfo.init(getGameCookie());
        mannschaften.putAll(matchinfo.getMannschaftsNamen());
    }

    public void load() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }
    }

    public List<MobileSpiel> getSpiele() {

        if (this.getMannschaftAuswahl() == null) {
            return new ArrayList<>();
        }

        List<MobileSpiel> res = new ArrayList<>();

        if (mannschaftAuswahl.length() > 4) {
            mannschaftAuswahl = "";
            return new ArrayList<>();
        }

        if (mannschaftAuswahl.equalsIgnoreCase("0")) {
            return List.of();
        }

        List<Spiel> spiele = srepo.findSpielFromMannschaft(matchinfo.getMannschaftByName(mannschaftAuswahl).getId());
        spiele.sort(new SpielZeitComparator());

        boolean finaleok = false;

        for (Spiel s : spiele) {

            if (this.getMannschaftAuswahl() != null && s.getMannschaftA() != null && s.getMannschaftA().getNameNoNickname().toUpperCase().equals(this.getMannschaftAuswahl().toUpperCase())) {

                if (!finaleok) {
                    finaleUpdaten(s, this.getMannschaftAuswahl());
                    finaleok = true;
                }

                Boolean verloren = null;
                if (s.getToreABestaetigt() < s.getToreBBestaetigt()) {
                    verloren = Boolean.TRUE;
                }
                if (s.getToreABestaetigt() > s.getToreBBestaetigt()) {
                    verloren = Boolean.FALSE;
                }

                MobileSpiel sp = getMobileSpiel(s, s.getMannschaftB().getName(), "(" + s.getToreABestaetigt() + ":" + s.getToreBBestaetigt() + ")", verloren);

                if (s.getTyp() == SpielEnum.GRUPPE) {
                    res.add(sp);
                }

            }

            if (this.mannschaftAuswahl != null && s.getMannschaftA() != null && s.getMannschaftB().getNameNoNickname().toUpperCase().equals(mannschaftAuswahl.toUpperCase())) {

                if (!finaleok) {
                    finaleUpdaten(s, this.getMannschaftAuswahl());
                    finaleok = true;
                }

                Boolean verloren = null;
                if (s.getToreABestaetigt() < s.getToreBBestaetigt()) {
                    verloren = Boolean.FALSE;
                }
                if (s.getToreABestaetigt() > s.getToreBBestaetigt()) {
                    verloren = Boolean.TRUE;
                }

                MobileSpiel sp = getMobileSpiel(s, s.getMannschaftA().getName(), "(" + s.getToreBBestaetigt() + ":" + s.getToreABestaetigt() + ")", verloren);

                if (s.getTyp() == SpielEnum.GRUPPE) {
                    res.add(sp);
                }
            }
        }
        return res;
    }

    public List<MobileSpiel> getFinale() {
        List<MobileSpiel> res = new ArrayList<>();
        res.add(this.finale);
        return res;
    }

    public MobileSpiel getMobileSpiel(Spiel s, String mannschaft, String tore, Boolean verloren) {

        MobileSpiel sp = new MobileSpiel();

        sp.setStart(DateUtil.getShortTimeDayString(s.getStart()));
        sp.setPlatz(s.getPlatz().getText());

        sp.setGegner(mannschaft);

        sp.setResultat(tore);

        sp.setZeile(sp.getZeile() + DateUtil.getShortTimeDayString(s.getStart()) + " | " + " Platz:" + s.getPlatz() + " | vs: " + mannschaft);

        // tore anfuegen wenn fertig
        if (s.isFertigBestaetigt()) {
            sp.setZeile(sp.getZeile() + " | " + tore);
        }

        // unentschieden gespielt = gelb
        if (s.isAmSpielen()) {
            sp.setColor("blue");
        }

        // unentschieden gespielt = gelb
        if (s.isFertigEingetragen() && verloren == null) {
            sp.setColor("orange");
        }

        // gewonnen = gruen
        if (s.isFertigEingetragen() && verloren == Boolean.FALSE) {
            sp.setColor("green");
        }

        // verloren = rot
        if (s.isFertigEingetragen() && verloren == Boolean.TRUE) {
            sp.setColor("red");
        }

        return sp;
    }

    private void finaleUpdaten(Spiel s, String mannschaft) {

        Spiel kfinale = s.getMannschaftA().getKategorie().getKleineFinal();
        Spiel gfinale = s.getMannschaftA().getKategorie().getGrosserFinal();

        // kleiner Finale evaluieren
        if (kfinale != null && kfinale.getMannschaftA() != null && kfinale.getMannschaftB() != null) {
            if (kfinale.getMannschaftA().getNameNoNickname().equals(mannschaft)) {
                Boolean verloren = null;
                if (kfinale.getToreABestaetigt() < kfinale.getToreBBestaetigt()) {
                    verloren = Boolean.TRUE;
                }
                if (kfinale.getToreABestaetigt() > kfinale.getToreBBestaetigt()) {
                    verloren = Boolean.FALSE;
                }

                this.finale = getMobileSpiel(kfinale, kfinale.getMannschaftBName(), "(" + kfinale.getToreABestaetigt() + ":" + kfinale.getToreBBestaetigt() + ")", verloren);
                this.finale.setZeile("Kl. Finale: " + this.finale.getZeile().replace("Platz", "Pl."));
                return;
            } else if (kfinale.getMannschaftB().getNameNoNickname().equals(mannschaft)) {
                Boolean verloren = null;
                if (kfinale.getToreABestaetigt() < kfinale.getToreBBestaetigt()) {
                    verloren = Boolean.FALSE;
                }
                if (kfinale.getToreABestaetigt() > kfinale.getToreBBestaetigt()) {
                    verloren = Boolean.TRUE;
                }

                this.finale = getMobileSpiel(kfinale, kfinale.getMannschaftAName(), "(" + kfinale.getToreBBestaetigt() + ":" + kfinale.getToreABestaetigt() + ")", verloren);
                this.finale.setZeile("Kl. Finale: " + this.finale.getZeile().replace("Platz", "Pl."));
                return;
            }
        }

        // grosser Finale evaluieren
        if (gfinale != null && gfinale.getMannschaftA() != null && gfinale.getMannschaftB() != null) {

            if (gfinale.getMannschaftA().getNameNoNickname().equals(mannschaft)) {
                Boolean verloren = null;
                if (gfinale.getToreABestaetigt() < gfinale.getToreBBestaetigt()) {
                    verloren = Boolean.TRUE;
                }
                if (gfinale.getToreABestaetigt() > gfinale.getToreBBestaetigt()) {
                    verloren = Boolean.FALSE;
                }

                this.finale = getMobileSpiel(gfinale, gfinale.getMannschaftBName(), "(" + gfinale.getToreABestaetigt() + ":" + gfinale.getToreBBestaetigt() + ")", verloren);
                this.finale.setZeile("Gr. Finale: " + this.finale.getZeile().replace("Platz", "Pl."));
                return;
            } else if (gfinale.getMannschaftB().getNameNoNickname().equals(mannschaft)) {
                Boolean verloren = null;
                if (gfinale.getToreABestaetigt() < gfinale.getToreBBestaetigt()) {
                    verloren = Boolean.FALSE;
                }
                if (gfinale.getToreABestaetigt() > gfinale.getToreBBestaetigt()) {
                    verloren = Boolean.TRUE;
                }

                this.finale = getMobileSpiel(gfinale, gfinale.getMannschaftAName(), "(" + gfinale.getToreBBestaetigt() + ":" + gfinale.getToreABestaetigt() + ")", verloren);
                this.finale.setZeile("Gr. Finale: " + this.finale.getZeile().replace("Platz", "Pl."));
                return;
            }
        }

        // finale noch nicht gesetzt !!
        if (gfinale != null && gfinale.getMannschaftA() == null && gfinale.getMannschaftB() == null) {
            this.finale = new MobileSpiel();
            finale.setZeile("Paarung bekannt ab: " + this.matchinfo.evaluateFinalSpielPaarungBekannt(s.getMannschaftA(), this.getGameCookie()));
        } else {
            this.finale = new MobileSpiel();
            finale.setZeile("Einzug ins Finale leider nicht geschafft");
        }
    }

    public String getMannschaftAuswahl() {

        if (this.mannschaftAuswahl != null && !this.mannschaftAuswahl.isEmpty()) {
            return this.mannschaftAuswahl;
        }

        Map<String, Object> requestCookieMap = FacesContext.getCurrentInstance().getExternalContext().getRequestCookieMap();
        jakarta.servlet.http.Cookie msch = (Cookie) requestCookieMap.get("SchuetuMannschaft");
        if (msch != null && msch.getValue() != null) {
            mannschaftAuswahl = msch.getValue();
        }
        return mannschaftAuswahl;
    }

    public String getGameCookie() {
        Map<String, Object> requestCookieMap = FacesContext.getCurrentInstance().getExternalContext().getRequestCookieMap();
        jakarta.servlet.http.Cookie msch = (Cookie) requestCookieMap.get("SchuetuMobileGame");
        if (msch != null && msch.getValue() != null) {
            return msch.getValue();
        }
        log.warn("no game cookie");
        return "";
    }

    public void setMannschaftAuswahl(String mannschaftAuswahl) {
        this.mannschaftAuswahl = mannschaftAuswahl;
        HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
        Cookie cookie = new Cookie("SchuetuMannschaft", "" + mannschaftAuswahl);
        cookie.setMaxAge(60 * 60 * 24 * 30 * 2);
        response.addCookie(cookie);
    }
}
