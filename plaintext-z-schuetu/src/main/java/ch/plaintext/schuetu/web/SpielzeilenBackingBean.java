package ch.plaintext.schuetu.web;

import ch.plaintext.schuetu.service.GameSelectionHolder;
import ch.plaintext.schuetu.service.SpielzeilenService;
import ch.plaintext.schuetu.service.vorbereitung.D5GenerateSpielzeilen;
import ch.plaintext.schuetu.service.vorbereitung.E5Spielverteiler;
import ch.plaintext.schuetu.service.vorbereitung.VertauschungsUtil;
import ch.plaintext.schuetu.entity.Korrektur;
import ch.plaintext.schuetu.entity.Mannschaft;
import ch.plaintext.schuetu.entity.Spiel;
import ch.plaintext.schuetu.entity.SpielZeile;
import ch.plaintext.schuetu.repository.KorrekturPersistence;
import ch.plaintext.schuetu.repository.KorrekturRepository;
import ch.plaintext.schuetu.repository.SpielRepository;
import ch.plaintext.schuetu.repository.SpielZeilenRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.*;

/**
 * Spielzeilen manuell Vertauschen und Vertauschungen recorden
 *
 * @author info@emad.ch
 * @since 1.2.8
 */
@Component
@Scope("session")
@Data
@Slf4j
public class SpielzeilenBackingBean {

    @Autowired
    private KorrekturPersistence korrekturPers;

    @Autowired
    private E5Spielverteiler verteiler;

    @Autowired
    private KorrekturRepository korrekturRepository;

    @Autowired
    private VertauschungsUtil vertauschungsUtil;

    @Autowired
    private GameSelectionHolder gameHolder;

    @Autowired
    private SpielRepository spielRepository;

    @Autowired
    private SpielzeilenService spielzeilenService;

    @Autowired
    private SpielZeilenRepository spielZeilenRepository;

    private List<SpielZeile> samstag = new LinkedList<>();
    private List<SpielZeile> sonntag = new LinkedList<>();

    private LinkedList<SpielZeile> allZeilen;
    private List<Spiel> alleSpiele = new ArrayList<>();
    private Map<Spiel, SpielZeile> zeilenmap = new HashMap<>();
    private String platzA = "";
    private String platzB = "";
    private String zeilesa = "";
    private String zeilesb = "";
    private SpielZeile zeilea = null;
    private SpielZeile zeileb = null;
    private String vertString = "";

    @Autowired
    private D5GenerateSpielzeilen zeilenFactory;

    private List<SpielZeile> zeilen;

    @PostConstruct
    public void init() {
        if (!gameHolder.hasGame()) return;

        samstag = spielZeilenRepository.findSpieleSamstag(gameHolder.getGame().getModel().getGameName());
        sonntag = spielZeilenRepository.findSpieleSonntag(gameHolder.getGame().getModel().getGameName());

        if (samstag.size() == 0) {
            samstag.addAll(zeilenFactory.initZeilen(false, gameHolder.getGame()));
            sonntag.addAll(zeilenFactory.initZeilen(true, gameHolder.getGame()));
        }

    }

    public void plusOneHour() {
        if (!gameHolder.hasGame()) return;
        List<SpielZeile> alle = new ArrayList<>();
        samstag = spielZeilenRepository.findSpieleSamstag(gameHolder.getGame().getModel().getGameName());
        sonntag = spielZeilenRepository.findSpieleSonntag(gameHolder.getGame().getModel().getGameName());
        alle.addAll(samstag);
        alle.addAll(sonntag);

        for (SpielZeile zeile : alle) {
            zeile.setStart(new Date(zeile.getStart().getTime() + 60 * 60 * 1000));
        }
        spielZeilenRepository.saveAll(alle);
        init();
    }

    public Boolean isDefinemode() {
        if (!gameHolder.hasGame()) return Boolean.TRUE;
        if (spielZeilenRepository.findSpieleSamstag(gameHolder.getGame().getModel().getGameName()).size() == 0) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    public void exitDefinemode() {
        if (!gameHolder.hasGame()) return;

        for (SpielZeile zeile : samstag) {
            zeile.setGame(gameHolder.getGame().getModel().getGameName());
            if (!zeile.isPause()) {
                save(zeile);
            }
        }

        for (SpielZeile zeile : sonntag) {
            zeile.setGame(gameHolder.getGame().getModel().getGameName());
            if (!zeile.isPause()) {
                save(zeile);
            }
        }

        verteiler.spieleAutomatischVerteilen(gameHolder.getGame().getModel());

        samstag = spielZeilenRepository.findSpieleSamstag(gameHolder.getGame().getModel().getGameName());
        sonntag = spielZeilenRepository.findSpieleSonntag(gameHolder.getGame().getModel().getGameName());


    }

    public void togglePause(String guid) {
        for (SpielZeile zeile : samstag) {
            if (zeile.getGuid().equals(guid)) {
                zeile.togglePause();
            }
        }
        for (SpielZeile zeile : sonntag) {
            if (zeile.getGuid().equals(guid)) {
                zeile.togglePause();
            }
        }
    }

    public void setZeilea(String in) {
        if (in.equals("reset")) {
            reset();
        }
        if (zeilesa.isEmpty()) {
            platzA = "a";
            zeilesa = in + "a";
            zeilea = findZeile(zeilesa);
            zeilea.setAdisabled(true);
            vertString = vertString + zeilesa;
        } else {
            platzB = "a";
            zeilesb = in + "a";
            zeileb = findZeile(zeilesb);
            vertString = vertString + "-" + zeilesb;
            vertausche();
        }
    }

    public void setZeileb(String in) {
        if (in.equals("reset")) {
            reset();
        }
        if (zeilesa.isEmpty()) {
            platzA = "b";
            zeilesa = in + "b";
            zeilea = findZeile(zeilesa);
            zeilea.setBdisabled(true);
            vertString = vertString + zeilesa;
        } else {
            platzB = "b";
            zeilesb = in + "b";
            zeileb = findZeile(zeilesb);
            vertString = vertString + "-" + zeilesb;
            vertausche();
        }
    }

    public void setZeilec(String in) {
        if (in.equals("reset")) {
            reset();
        }

        if (zeilesa.isEmpty()) {
            platzA = "c";
            zeilesa = in + "c";
            zeilea = findZeile(zeilesa);
            zeilea.setCdisabled(true);
            vertString = vertString + zeilesa;
        } else {
            platzB = "c";
            zeilesb = in + "c";
            zeileb = findZeile(zeilesb);
            vertString = vertString + "-" + zeilesb;
            vertausche();
        }
    }

    public void setZeiled(String in) {
        if (in.equals("reset")) {
            reset();
        }

        if (zeilesa.isEmpty()) {
            platzA = "d";
            zeilesa = in + "d";
            zeilea = findZeile(zeilesa);
            zeilea.setDdisabled(true);
            vertString = vertString + zeilesa;
        } else {
            platzB = "d";
            zeilesb = in + "d";
            zeileb = findZeile(zeilesb);
            vertString = vertString + "-" + zeilesb;
            vertausche();
        }
    }


    private void vertausche() {

        log.info("VERT: " + vertString);

        Korrektur korrektur = new Korrektur();
        korrektur.setGame(this.gameHolder.getGameName());
        korrektur.setTyp("vertauschung");
        korrektur.setWert(vertString);

        korrekturRepository.save(korrektur);

        vertString = "";

        Spiel a = this.zeilea.getSpiel(platzA);
        Spiel b = this.zeileb.getSpiel(platzB);

        this.zeilea.setSpiel(b, platzA);
        this.zeileb.setSpiel(a, platzB);

        this.zeilea = save(this.zeilea);
        this.zeileb = save(this.zeileb);

        reset();

    }

    public List<Mannschaft> getAllRelevant4Check(Spiel spiel) {
        List<Mannschaft> res = new ArrayList<>();
        if (!gameHolder.hasGame()) return res;
        int p = gameHolder.getGame().getModel().getZweiPausenBisKlasse();
        int k = spiel.getMannschaftA().getKlasse();

        int pausen = 1;
        if (k <= p) {
            pausen = 2;
        }

        SpielZeile zeile = zeilenmap.get(spiel);
        int index = samstag.indexOf(zeile);
        if (index > -1) {
            if (pausen == 1) {
                if (index - 1 > -1) {
                    res.addAll(samstag.get(index - 1).getAllMannschaften());
                }
                res.addAll(samstag.get(index).getAllMannschaften());
                if (index + 1 <= samstag.size()) {
                    res.addAll(samstag.get(index + 1).getAllMannschaften());
                }
            }
            if (pausen == 2) {
                if (index - 2 > -1) {
                    res.addAll(samstag.get(index - 2).getAllMannschaften());
                }
                if (index - 1 > -1) {
                    res.addAll(samstag.get(index - 1).getAllMannschaften());
                }

                // alle der eigenen zeile ausser die beiden ses gesuchten spiels
                List<Mannschaft> diese = samstag.get(index).getAllMannschaften();
                diese.remove(spiel.getMannschaftA());
                diese.remove(spiel.getMannschaftB());
                res.addAll(diese);

                if (index + 1 <= samstag.size()) {
                    res.addAll(samstag.get(index + 1).getAllMannschaften());
                }
                if (index + 2 <= samstag.size()) {
                    res.addAll(samstag.get(index + 2).getAllMannschaften());
                }
            }
        } else {
            index = sonntag.indexOf(zeile);
            if (index > -1) {
                if (pausen == 1) {
                    if (index - 1 > -1) {
                        res.addAll(sonntag.get(index - 1).getAllMannschaften());
                    }
                    res.addAll(sonntag.get(index).getAllMannschaften());
                    if (index + 1 <= sonntag.size()) {
                        res.addAll(sonntag.get(index + 1).getAllMannschaften());
                    }
                }
                if (pausen == 2) {
                    if (index - 2 > -1) {
                        res.addAll(sonntag.get(index - 2).getAllMannschaften());
                    }
                    if (index - 1 > -1) {
                        res.addAll(sonntag.get(index - 1).getAllMannschaften());
                    }

                    // alle der eigenen zeile ausser die beiden des gesuchten
                    // spiels
                    List<Mannschaft> diese = sonntag.get(index).getAllMannschaften();
                    diese.remove(spiel.getMannschaftA());
                    diese.remove(spiel.getMannschaftB());
                    res.addAll(diese);

                    if (index + 1 <= sonntag.size()) {
                        res.addAll(sonntag.get(index + 1).getAllMannschaften());
                    }
                    if (index + 2 <= sonntag.size()) {
                        res.addAll(sonntag.get(index + 2).getAllMannschaften());
                    }
                }
            }
        }
        return res;
    }

    private void reset() {

        for (Spiel spiel : alleSpiele) {
            spiel.resetTausch();
        }

        for (SpielZeile zl : samstag) {
            zl.setAdisabled(false);
            zl.setBdisabled(false);
            zl.setCdisabled(false);
            zl.setDdisabled(false);
        }

        for (SpielZeile zl : sonntag) {
            zl.setAdisabled(false);
            zl.setBdisabled(false);
            zl.setCdisabled(false);
            zl.setDdisabled(false);
        }


        this.zeilesa = "";
        this.zeilesb = "";

        platzA = "";
        platzB = "";

        this.vertString = "";

    }

    public SpielZeile save(SpielZeile zl) {

        if (zl.getA().toString().equals("-")) {
            zl.setA(null);
            zl = spielZeilenRepository.save(zl);
        }

        if (zl.getB().toString().equals("-")) {
            zl.setB(null);
            zl = spielZeilenRepository.save(zl);
        }

        if (zl.getC().toString().equals("-")) {
            zl.setC(null);
            zl = spielZeilenRepository.save(zl);
        }

        if (zl.getD().toString().equals("-")) {
            zl.setD(null);
            zl = spielZeilenRepository.save(zl);
        }

        return spielZeilenRepository.save(zl);
    }


    public List<SpielZeile> getZeilenSamstag() {
        return samstag;
    }

    public List<SpielZeile> getZeilenSonntag() {
        return sonntag;
    }

    private SpielZeile findZeile(String in) {
        List<SpielZeile> suchen;
        if (in.startsWith("so")) {
            suchen = sonntag;
        } else {
            suchen = samstag;
        }

        for (SpielZeile spielZeile : suchen) {
            if (spielZeile.getZeitAsString().equals(in.split(",")[1])) {
                return spielZeile;
            }
        }
        return null;
    }

    public int getAnzahlSpiele() {
        if (!gameHolder.hasGame()) return 0;
        return spielRepository.findGruppenSpiel(this.gameHolder.getGame().getModel().getGameName()).size();
    }

    public int getAnzahlFinale() {
        if (!gameHolder.hasGame()) return 0;
        return spielRepository.findFinalSpiel(this.gameHolder.getGame().getModel().getGameName()).size();
    }


    public int getAnzahlNoetigeDurchfuehrungen(int spiele, int plaetze) {
        int mod = spiele % plaetze;

        int ret = spiele / plaetze;

        if (mod > 0) {
            ret++;
        }
        return ret;
    }

    public Integer getVorhandeneSpielplaetze() {
        Integer ret = 0;
        List<SpielZeile> zeilen = new ArrayList<>();
        zeilen.addAll(sonntag);
        zeilen.addAll(samstag);
        for (SpielZeile zeile : zeilen) {
            if (!zeile.isFinale() && !zeile.isPause()) {
                if (!zeile.isAdisabled()) {
                    ret++;
                }
                if (!zeile.isBdisabled()) {
                    ret++;
                }
                if (!zeile.isCdisabled()) {
                    ret++;
                }
                if (!zeile.isDdisabled()) {
                    ret++;
                }
            }
        }

        return ret;
    }

    public Integer getVorhandeneFinalspielplaetze() {

        Integer ret = 0;
        List<SpielZeile> zeilen = new ArrayList<>();
        zeilen.addAll(sonntag);
        zeilen.addAll(samstag);
        for (SpielZeile zeile : zeilen) {
            if (zeile.isFinale() && !zeile.isPause()) {
                if (!zeile.isAdisabled()) {
                    ret++;
                }
                if (!zeile.isBdisabled()) {
                    ret++;
                }
                if (!zeile.isCdisabled()) {
                    ret++;
                }
                if (!zeile.isDdisabled()) {
                    ret++;
                }
            }
        }
        return ret;
    }

    public void spielZeitenAnpassen() {
        spielzeilenService.spielZeitenAnpassen();
    }

}
