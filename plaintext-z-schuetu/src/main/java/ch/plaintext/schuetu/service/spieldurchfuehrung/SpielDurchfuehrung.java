package ch.plaintext.schuetu.service.spieldurchfuehrung;

import ch.plaintext.schuetu.service.Game;
import ch.plaintext.schuetu.service.GameConnectable;
import ch.plaintext.schuetu.service.PenaltyLoaderFactory;
import ch.plaintext.schuetu.service.ResultateVerarbeiter;
import ch.plaintext.schuetu.service.zeit.Countdown;
import ch.plaintext.schuetu.service.zeit.Zeitgeber;
import ch.plaintext.schuetu.service.websiteinfo.WebsiteInfoService;
import ch.plaintext.schuetu.entity.Penalty;
import ch.plaintext.schuetu.entity.Spiel;
import ch.plaintext.schuetu.entity.SpielZeile;
import ch.plaintext.schuetu.model.enums.SpielEnum;
import ch.plaintext.schuetu.model.enums.SpielPhasenEnum;
import ch.plaintext.schuetu.model.enums.SpielZeilenPhaseEnum;
import ch.plaintext.schuetu.repository.SpielRepository;
import ch.plaintext.schuetu.repository.SpielZeilenRepository;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("SimplifiableIfStatement")
@Component
@Scope("prototype")
@Slf4j
@Data
@EqualsAndHashCode(exclude = {"game"})
@ToString(exclude = "game")
public class SpielDurchfuehrung implements GameConnectable {

    private int wartendSize = 3;
    private int minutenZumVorbereiten = 3;
    private String delim = "/";
    private boolean init = false;
    private Game game;
    private int count = 0;

    @Autowired
    private WebsiteInfoService infoservice;
    @Autowired
    private SpielDurchfuehrungTrigger trigger;
    @Autowired
    private ResultateVerarbeiter verarbeiter;
    @Autowired
    private SpielZeilenRepository spielzeilenRepo;
    @Autowired
    private SpielRepository spielRepo;
    @Autowired
    private DurchfuehrungDataDatabase durchfuehrungData;
    @Autowired
    private PenaltyLoaderFactory penalty;

    private Zeitgeber zeitgeber;
    private boolean endranglistegedruckt = false;
    private Countdown countdown;
    private Countdown countdownToStart;

    @PostConstruct
    public void initA() {
        trigger.add(this);
    }

    public List<Penalty> getPenaltyAnstehend() {
        List<Penalty> ret = new ArrayList<>();
        Penalty p = penalty.loadPenaltyAnstehend(game.getModel().getGameName());
        if (p != null) { ret.add(p); }
        return ret;
    }

    public List<Penalty> getPenaltyGespielt() {
        List<Penalty> ret = new ArrayList<>();
        Penalty p = penalty.loadPenaltyGespielt(game.getModel().getGameName());
        if (p != null) { ret.add(p); }
        return ret;
    }

    public void setPenaltyGespielt(String id) {
        penalty.penaltyGespielt(id);
    }

    private void init() {
        durchfuehrungData.getList2ZumVorbereiten().clear();
        durchfuehrungData.getList3Vorbereitet().clear();
        durchfuehrungData.getList4Spielend().clear();
        durchfuehrungData.getList2ZumVorbereiten().addAll(spielzeilenRepo.findBZurVorbereitung(getGame().getModel().getGameName()));
        durchfuehrungData.getList3Vorbereitet().addAll(spielzeilenRepo.findCVorbereitet(getGame().getModel().getGameName()));
        durchfuehrungData.getList4Spielend().addAll(spielzeilenRepo.findDSpielend(getGame().getModel().getGameName()));
    }

    public void enter() {
        if (game.getModel().isAbbrechenZulassen() && getList4Spielend().size() > 0) {
            this.beenden();
            return;
        }
        if (this.getList3Vorbereitet().size() > 0 && this.getReadyToSpielen()) {
            this.spielen();
            return;
        }
        if (!this.getList2ZumVorbereiten().isEmpty() && this.getReadyToVorbereiten()) {
            this.vorbereitet();
            return;
        }
        log.info("Enter -> ohne Effekt");
    }

    private void prepare2ZumVorbereiten() {
        if (!this.durchfuehrungData.getList1Wartend(this.wartendSize).isEmpty()) {
            if ((this.durchfuehrungData.getList2ZumVorbereiten().isEmpty())) {
                long naechste = this.durchfuehrungData.getList1Wartend(wartendSize).get(this.durchfuehrungData.getList1Wartend(wartendSize).size() - 1).getStart().getTime() - (60L * minutenZumVorbereiten * 1000);
                long now = zeitgeber.getRichtigeZeitMillis();
                if (naechste < now) {
                    SpielZeile temp = this.durchfuehrungData.getList1Wartend(wartendSize).remove(this.durchfuehrungData.getList1Wartend(wartendSize).size() - 1);
                    temp.setPhase(SpielZeilenPhaseEnum.B_ZUR_VORBEREITUNG);
                    temp = spielezeileUpdatenBetreffendZeilenphaseAndSave(temp);
                    this.durchfuehrungData.getList2ZumVorbereiten().add(temp);
                    if (game.getModel().isAutomatischesVorbereiten()) {
                        this.vorbereitet();
                    }
                }
            }
        }
        if (stopBecauseZumVorbereiten() && this.zeitgeber.isGameStarted()) {
            this.zeitgeber.stopGame("spielzeit des zu_vorbereitenden ist abgelaufen");
        }
        if (!zeitgeber.isGameRunning() && !stopBecauseWartenAufStart()) {
            DateTime time = new DateTime(zeitgeber.getSpielZeitInMillis());
            if (this.durchfuehrungData.getList2ZumVorbereiten().isEmpty() || time.isBefore(new DateTime(durchfuehrungData.getList2ZumVorbereiten().get(0).getStart()))) {
                this.zeitgeber.startGame(0, "liste mit zu_vorbereiteten ist wieder leer");
            }
        }
    }

    private SpielZeile spielezeileUpdatenBetreffendZeilenphaseAndSave(SpielZeile temp) {
        if (temp.getA() != null) { temp.getA().setSpielZeilenPhase(temp.getPhase()); }
        if (temp.getB() != null) { temp.getB().setSpielZeilenPhase(temp.getPhase()); }
        if (temp.getC() != null) { temp.getC().setSpielZeilenPhase(temp.getPhase()); }
        if (temp.getD() != null) { temp.getD().setSpielZeilenPhase(temp.getPhase()); }
        return this.spielzeilenRepo.save(temp);
    }

    private boolean stopBecauseZumVorbereiten() {
        if (durchfuehrungData.getList2ZumVorbereiten().isEmpty()) { return false; }
        DateTime time = new DateTime(zeitgeber.getSpielZeitInMillis());
        return !this.durchfuehrungData.getList2ZumVorbereiten().isEmpty() && time.isAfter(new DateTime(durchfuehrungData.getList2ZumVorbereiten().get(0).getStart()));
    }

    private boolean stopBecauseWartenAufStart() {
        if (durchfuehrungData.getList3Vorbereitet().isEmpty()) { return false; }
        DateTime time = new DateTime(zeitgeber.getSpielZeitInMillis());
        return !this.durchfuehrungData.getList3Vorbereitet().isEmpty() && time.isAfter(new DateTime(durchfuehrungData.getList3Vorbereitet().get(0).getStart()));
    }

    private void prepare3WartenAufStart() {
        if (stopBecauseWartenAufStart()) {
            this.zeitgeber.stopGame("spielzeit des vorbereitenden ist abgelaufen");
        }
        if (!zeitgeber.isGameRunning() && !stopBecauseZumVorbereiten()) {
            DateTime time = new DateTime(zeitgeber.getSpielZeitInMillis());
            if (this.durchfuehrungData.getList3Vorbereitet().isEmpty() || time.isBefore(new DateTime(durchfuehrungData.getList3Vorbereitet().get(0).getStart()))) {
                this.zeitgeber.startGame(0, "liste mit vorbereiteten ist wieder leer");
            }
        }
    }

    private void prepare4Spielend() {
        if (this.getList4Spielend().isEmpty()) { return; }
        if (this.countdown != null && this.getCountdown().isFertig()) {
            this.beenden();
        }
    }

    private void checkSpielende() {
        if (!endranglistegedruckt && game.getModel().getPhase() == SpielPhasenEnum.G_ABGESCHLOSSEN) {
            String jahr = "" + new DateTime(game.getModel().getStart()).getYear();
            this.infoservice.dumpJetzt(jahr);
            endranglistegedruckt = true;
        }
    }

    public void vorbereitet() {
        SpielZeile temp = this.durchfuehrungData.getList2ZumVorbereiten().remove(0);
        DateTime time = new DateTime(zeitgeber.getSpielZeitInMillis());
        this.countdownToStart = new Countdown(time, new DateTime(temp.getStart()));
        temp.setPhase(SpielZeilenPhaseEnum.C_VORBEREITET);
        temp = spielezeileUpdatenBetreffendZeilenphaseAndSave(temp);
        List<SpielZeile> zeile = this.durchfuehrungData.getList3Vorbereitet();
        zeile.add(temp);
        this.durchfuehrungData.setList3Vorbereitet(zeile);
    }

    public void spielen() {
        SpielZeile temp = this.durchfuehrungData.getList3Vorbereitet().remove(0);
        Spiel tempSpiel = null;
        temp.setPhase(SpielZeilenPhaseEnum.D_SPIELEND);
        if (temp.getA() != null) { temp.getA().setAmSpielen(true); if (temp.getA().getStart() != null) { tempSpiel = temp.getA(); } }
        if (temp.getB() != null) { temp.getB().setAmSpielen(true); tempSpiel = temp.getB(); if (temp.getB().getStart() != null) { tempSpiel = temp.getB(); } }
        if (temp.getC() != null) { temp.getC().setAmSpielen(true); if (temp.getC().getStart() != null) { tempSpiel = temp.getC(); } }
        if (temp.getD() != null) { temp.getD().setAmSpielen(true); if (temp.getD().getStart() != null) { tempSpiel = temp.getD(); } }
        temp = spielezeileUpdatenBetreffendZeilenphaseAndSave(temp);
        this.durchfuehrungData.getList4Spielend().add(temp);
        DateTime time = new DateTime(zeitgeber.getSpielZeitInMillis());
        if (tempSpiel.getTyp() == SpielEnum.GRUPPE) {
            this.countdown = new Countdown(time, 60 * this.game.getModel().getSpiellaenge());
        } else {
            this.countdown = new Countdown(time, 60 * this.game.getModel().getSpiellaengefinale());
        }
        if (getGame().getModel().isAutomatischesAufholen()) {
            spielzeitEinholen(getGame().getModel().getAufholzeitInSekunden());
        }
    }

    public void spielzeitEinholen(int seconds) {
        int effVerspaetung = this.game.getZeit().getVerspaetung();
        effVerspaetung = Math.abs(effVerspaetung);
        if (effVerspaetung < 1) { return; }
        if (effVerspaetung >= seconds) {
            this.game.getZeit().startGame(seconds, "einholung: " + seconds);
        } else {
            this.game.getZeit().startGame(effVerspaetung, "einholung effektiv: " + effVerspaetung);
        }
        if (this.countdown != null) { this.countdown.aufholen(seconds); }
    }

    public synchronized void beenden() {
        SpielZeile temp = this.durchfuehrungData.getList4Spielend().remove(0);
        temp.setPhase(SpielZeilenPhaseEnum.E_BEENDET);
        this.durchfuehrungData.getList5Beendet().add(temp);
        if (temp.getA() != null) { Spiel spiel = temp.getA(); spiel.setAmSpielen(false); spiel.setFertigGespielt(true); spielRepo.save(spiel); }
        if (temp.getB() != null) { Spiel spiel = temp.getB(); spiel.setAmSpielen(false); spiel.setFertigGespielt(true); spielRepo.save(spiel); }
        if (temp.getC() != null) { Spiel spiel = temp.getC(); spiel.setAmSpielen(false); spiel.setFertigGespielt(true); spielRepo.save(spiel); }
        if (temp.getD() != null) { Spiel spiel = temp.getD(); spiel.setAmSpielen(false); spiel.setFertigGespielt(true); spielRepo.save(spiel); }
        spielezeileUpdatenBetreffendZeilenphaseAndSave(temp);
    }

    public synchronized List<SpielZeile> getList1Wartend() { return this.durchfuehrungData.getList1Wartend(this.wartendSize); }
    public String getWait() { int millis = 15000; if (countdown != null && !countdown.isFertig()) { if (millis > (countdown.getSecondsPlus2() * 1000)) { millis = (countdown.getSecondsPlus2() * 1000); } } if (countdownToStart != null && !countdownToStart.isFertig()) { if (millis > (countdownToStart.getSecondsPlus2() * 1000)) { millis = (countdownToStart.getSecondsPlus2() * 1000); } } if (millis < 5000) { millis = 5000; } return "" + millis; }
    public boolean getReadyToVorbereiten() { return this.durchfuehrungData.getList3Vorbereitet().isEmpty(); }
    public boolean getReadyToSpielen() { return this.durchfuehrungData.getList4Spielend().isEmpty(); }
    public List<SpielZeile> getList2ZumVorbereiten() { return this.durchfuehrungData.getList2ZumVorbereiten(); }
    public List<SpielZeile> getList3Vorbereitet() { return this.durchfuehrungData.getList3Vorbereitet(); }
    public List<SpielZeile> getList4Spielend() { return this.durchfuehrungData.getList4Spielend(); }
    public List<SpielZeile> getList5Beendet() { return this.durchfuehrungData.getList5Beendet(); }
    public Countdown getCountdown() { return this.countdown; }
    public Countdown getCountdownToStart() { return countdownToStart; }
    public void fertigesSpiel(Spiel spiel) { this.verarbeiter.signalFertigesSpiel(spiel.getId()); }
    @Override public Game getGame() { return game; }
    @Override public void setGame(Game game) { this.game = game; }

    public void pulse() {
        count++;
        if (!init) { init(); init = true; }
        if (this.countdown != null) { DateTime time = new DateTime(zeitgeber.getSpielZeitInMillis()); this.countdown.signalTime(time); }
        if (this.countdownToStart != null) { DateTime time = new DateTime(zeitgeber.getSpielZeitInMillis()); this.countdownToStart.signalTime(time); }
        if (getGame().getModel().getPhase().equals(SpielPhasenEnum.F_SPIELEN)) { prepare2ZumVorbereiten(); prepare3WartenAufStart(); prepare4Spielend(); checkSpielende(); }
        if (getGame().getModel().equals(SpielPhasenEnum.G_ABGESCHLOSSEN)) { checkSpielende(); }
    }

    public void reset() {
        if (game.getModel().getPhase() != SpielPhasenEnum.F_SPIELEN) { return; }
        for (SpielZeile zeile : durchfuehrungData.getList1Wartend(5)) { spielBeenden(zeile); }
        for (SpielZeile zeile : durchfuehrungData.getList2ZumVorbereiten()) { spielBeenden(zeile); }
        for (SpielZeile zeile : durchfuehrungData.getList3Vorbereitet()) { spielBeenden(zeile); }
        init();
    }

    private void spielBeenden(SpielZeile zeile) {
        if (zeile.getA() != null && !zeile.getA().isFertigEingetragen()) { return; }
        if (zeile.getB() != null && !zeile.getB().isFertigEingetragen()) { return; }
        if (zeile.getC() != null && !zeile.getC().isFertigEingetragen()) { return; }
        zeile.setPhase(SpielZeilenPhaseEnum.E_BEENDET);
        spielzeilenRepo.save(zeile);
    }

}
