package ch.plaintext.schuetu.model;

import ch.plaintext.schuetu.model.enums.SpielPhasenEnum;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Implementiert Persistent nicht und ist keine Entity, wird mit Xstream
 * serialisiert und als Text in die DB gespeichert
 */
@Data
@Deprecated //2020
public class SpielEinstellungen implements Serializable, Einstellungen {

    private static final long serialVersionUID = 1L;

    private SpielPhasenEnum phase = SpielPhasenEnum.A_ANMELDEPHASE;

    private Date starttag = new Date();

    private String starttagstr = "";

    private boolean skipDump = false;

    private String test;

    private Date start = new Date();

    private int verschnellerungsFaktor = 1;

    private boolean startJetzt = true;

    private String spielVertauschungen;

    private int pause = 2;

    private int spiellaenge = 10;

    private int spiellaengefinale = 13;

    private int aufholzeitInSekunden = 60;

    private int pollrequestSpeaker = 1;

    private boolean automatischesAufholen = false;

    private boolean automatischesVorbereiten = false;

    @Deprecated // Nicht mehr verwendet seit 2026
    private boolean automatischesAnsagen = false;

    private boolean abbrechenZulassen = false;

    @Deprecated // Nicht mehr verwendet seit 2026
    private boolean gongEinschalten = false;

    private boolean behandleFinaleProKlassebeiZusammengefuehrten = true;

    private boolean webcamdemomode = false;

    private boolean webcamdemomodescharf = false;

    private boolean websiteInMannschaftslistenmode = false;

    private boolean websiteEnableDownloadLink = false;
    private String websiteDownloadLink = "";

    private boolean websiteEnableProgrammDownloadLink = false;
    private String websiteProgrammDownloadLink = "";

    private String websiteTurnierTitel = "";

    private String websiteTurnierMeldung = "nichts";

    private boolean mobileLinkOn = false;
    private String mobileLink = "";
    private boolean master = false;
    // 05 Spielverteilung
    private int zweiPausenBisKlasse = 3;
    private boolean groesser6AufC = true;
    private int groesser6AufCMin = 7;

    public SpielEinstellungen() {
        LocalDateTime date = LocalDateTime.of(2013, 6, 8, 0, 0);
        starttag = Date.from(date.atZone(ZoneId.systemDefault()).toInstant());
    }

    public boolean isMaster() {
        return master;
    }

    public void setMaster(boolean master) {
        this.master = master;
    }

    public void setSpielPhaseString(String phaseIn) {
        String phaseS = phaseIn.toLowerCase();

        if (phaseS.startsWith("a")) {
            this.setPhase(SpielPhasenEnum.A_ANMELDEPHASE);
        }

        if (phaseS.startsWith("b")) {
            this.setPhase(SpielPhasenEnum.B_KATEGORIE_ZUORDNUNG);
        }

        if (phaseS.startsWith("c")) {
            this.setPhase(SpielPhasenEnum.C_SPIELTAGE_DEFINIEREN);
        }

        if (phaseS.startsWith("d")) {
            this.setPhase(SpielPhasenEnum.D_SPIELE_ZUORDNUNG);
        }

        if (phaseS.startsWith("e")) {
            this.setPhase(SpielPhasenEnum.E_SPIELBEREIT);
        }

        if (phaseS.startsWith("f")) {
            this.setPhase(SpielPhasenEnum.F_SPIELEN);
        }

        if (phaseS.startsWith("g")) {
            this.setPhase(SpielPhasenEnum.G_ABGESCHLOSSEN);
        }

    }

    @Override
    public String getStarttagstr() {
        return this.starttagstr;
    }

    public void setStarttagstr(final String starttagstr) {
        this.starttagstr = starttagstr;
    }

    @Override
    public int getPause() {
        return this.pause;
    }

    public void setPause(final int pause) {
        this.pause = pause;
    }

    @Override
    public int getSpiellaenge() {
        return this.spiellaenge;
    }

    public void setSpiellaenge(final int spiellaenge) {
        this.spiellaenge = spiellaenge;
    }

    @Override
    public SpielPhasenEnum getPhase() {
        return this.phase;
    }

    public void setPhase(final SpielPhasenEnum phase) {
        this.phase = phase;
    }

    @Override
    public Date getStarttag() {
        return this.starttag;
    }

    public void setStarttag(final Date starttag) {
        this.starttag = starttag;
    }

    @Override
    public String getTest() {
        return this.test;
    }

    public void setTest(final String test) {
        this.test = test;
    }

    @Override
    @Deprecated
    public Date getStart() {
        return this.start;
    }

    @Deprecated
    public void setStart(final Date start) {
        this.start = start;
    }

    @Override
    public boolean isStartJetzt() {
        return this.startJetzt;
    }

    public void setStartJetzt(final boolean startJetzt) {
        this.startJetzt = startJetzt;
    }

    @Override
    public int getVerschnellerungsFaktor() {
        return this.verschnellerungsFaktor;
    }

    public void setVerschnellerungsFaktor(final int verschnellerungsFaktor) {
        this.verschnellerungsFaktor = verschnellerungsFaktor;
    }

    public String getSpielVertauschungen() {
        return spielVertauschungen;
    }

    public void setSpielVertauschungen(String spielVertauschungen) {
        this.spielVertauschungen = spielVertauschungen;
    }

    @Override
    public boolean isAutomatischesAufholen() {
        return automatischesAufholen;
    }

    public void setAutomatischesAufholen(boolean automatischesAufholen) {
        this.automatischesAufholen = automatischesAufholen;
    }

    @Override
    public int getAufholzeitInSekunden() {
        return aufholzeitInSekunden;
    }

    public void setAufholzeitInSekunden(int aufholzeitInSekunden) {
        this.aufholzeitInSekunden = aufholzeitInSekunden;
    }

    @Override
    public boolean isAutomatischesAnsagen() {
        return automatischesAnsagen;
    }

    public void setAutomatischesAnsagen(boolean automatischesAnsagen) {
        this.automatischesAnsagen = automatischesAnsagen;
    }

    @Override
    public boolean isAutomatischesVorbereiten() {
        return automatischesVorbereiten;
    }

    public void setAutomatischesVorbereiten(boolean automatischesVorbereiten) {
        this.automatischesVorbereiten = automatischesVorbereiten;
    }

    @Override
    public boolean isAbbrechenZulassen() {
        return abbrechenZulassen;
    }

    public void setAbbrechenZulassen(boolean abbrechenZulassen) {
        this.abbrechenZulassen = abbrechenZulassen;
    }

    @Override
    public boolean isGongEinschalten() {
        return gongEinschalten;
    }

    public void setGongEinschalten(boolean gongEinschalten) {
        this.gongEinschalten = gongEinschalten;
    }

    public boolean isWebcamdemomode() {
        return webcamdemomode;
    }

    public void setWebcamdemomode(boolean webcamdemomode) {
        this.webcamdemomode = webcamdemomode;
    }

    @Override
    public boolean isBehandleFinaleProKlassebeiZusammengefuehrten() {
        return behandleFinaleProKlassebeiZusammengefuehrten;
    }

    public void setBehandleFinaleProKlassebeiZusammengefuehrten(boolean behandleFinaleProKlassebeiZusammengefuehrten) {
        this.behandleFinaleProKlassebeiZusammengefuehrten = behandleFinaleProKlassebeiZusammengefuehrten;
    }

    @Override
    public boolean getWebsiteInMannschaftslistenmode() {
        return websiteInMannschaftslistenmode;
    }

    @Override
    public int getZweiPausenBisKlasse() {
        return zweiPausenBisKlasse;
    }

    public void setZweiPausenBisKlasse(int zweiPausenBisKlasse) {
        this.zweiPausenBisKlasse = zweiPausenBisKlasse;
    }

    @Override
    public boolean isWebsiteInMannschaftslistenmode() {
        return websiteInMannschaftslistenmode;
    }

    public void setWebsiteInMannschaftslistenmode(boolean websiteInMannschaftslistenmode) {
        this.websiteInMannschaftslistenmode = websiteInMannschaftslistenmode;
    }

    @Override
    public boolean isWebsiteEnableDownloadLink() {
        return websiteEnableDownloadLink;
    }

    public void setWebsiteEnableDownloadLink(boolean websiteEnableDownloadLink) {
        this.websiteEnableDownloadLink = websiteEnableDownloadLink;
    }

    @Override
    public String getWebsiteDownloadLink() {
        return websiteDownloadLink;
    }

    public void setWebsiteDownloadLink(String websiteDownloadLink) {
        this.websiteDownloadLink = websiteDownloadLink;
    }

    @Override
    public String getWebsiteTurnierTitel() {
        return websiteTurnierTitel;
    }

    public void setWebsiteTurnierTitel(String websiteTurnierTitel) {
        this.websiteTurnierTitel = websiteTurnierTitel;
    }

    @Override
    public boolean isWebsiteEnableProgrammDownloadLink() {
        return websiteEnableProgrammDownloadLink;
    }

    public void setWebsiteEnableProgrammDownloadLink(boolean websiteEnableProgrammDownloadLink) {
        this.websiteEnableProgrammDownloadLink = websiteEnableProgrammDownloadLink;
    }

    @Override
    public String getWebsiteProgrammDownloadLink() {
        return websiteProgrammDownloadLink;
    }

    public void setWebsiteProgrammDownloadLink(String websiteProgrammDownloadLink) {
        this.websiteProgrammDownloadLink = websiteProgrammDownloadLink;
    }

    public boolean isWebcamdemomodescharf() {
        return webcamdemomodescharf;
    }

    public void setWebcamdemomodescharf(boolean webcamdemomodescharf) {
        this.webcamdemomodescharf = webcamdemomodescharf;
    }

    @Override
    public boolean isMobileLinkOn() {
        return mobileLinkOn;
    }

    public void setMobileLinkOn(boolean mobileLinkOn) {
        this.mobileLinkOn = mobileLinkOn;
    }

    @Override
    public String getMobileLink() {
        return mobileLink;
    }

    public void setMobileLink(String mobileLink) {
        this.mobileLink = mobileLink;
    }

    @Override
    public int getPollrequestSpeaker() {
        return pollrequestSpeaker;
    }

    public void setPollrequestSpeaker(int pollrequestSpeaker) {
        this.pollrequestSpeaker = pollrequestSpeaker;
    }

    public boolean isGroesser6AufC() {
        return groesser6AufC;
    }

    public void setGroesser6AufC(boolean groesser6AufC) {
        this.groesser6AufC = groesser6AufC;
    }

    @Override
    public int getSpiellaengefinale() {
        return spiellaengefinale;
    }

    public void setSpiellaengefinale(int spiellaengefinale) {
        this.spiellaengefinale = spiellaengefinale;
    }

    @Override
    public boolean isSkipDump() {
        return skipDump;
    }

    public void setSkipDump(boolean skipDump) {
        this.skipDump = skipDump;
    }

    public int getGroesser6AufCMin() {
        return groesser6AufCMin;
    }

    public void setGroesser6AufCMin(int groesser6AufCMin) {
        this.groesser6AufCMin = groesser6AufCMin;
    }

    @Override
    public boolean isAnmeldephase() {
        return false;
    }

    @Override
    public String getWebsiteFixString() {
        return null;
    }

    @Override
    public void setWebsiteFixString(String in) {

    }
}
