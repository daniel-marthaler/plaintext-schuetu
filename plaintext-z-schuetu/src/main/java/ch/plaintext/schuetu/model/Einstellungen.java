package ch.plaintext.schuetu.model;

import ch.plaintext.schuetu.model.enums.SpielPhasenEnum;

import java.util.Date;

/**
 * Einstellungen Interface fuer Spiel-Konfiguration
 */
public interface Einstellungen {
    String getStarttagstr();

    int getPause();

    int getSpiellaenge();

    SpielPhasenEnum getPhase();

    Date getStarttag();

    String getTest();

    @Deprecated
    Date getStart();

    boolean isStartJetzt();

    int getVerschnellerungsFaktor();

    boolean isAutomatischesAufholen();

    int getAufholzeitInSekunden();

    boolean isAutomatischesAnsagen();

    boolean isAutomatischesVorbereiten();

    boolean isAbbrechenZulassen();

    boolean isGongEinschalten();

    boolean isBehandleFinaleProKlassebeiZusammengefuehrten();

    boolean getWebsiteInMannschaftslistenmode();

    int getZweiPausenBisKlasse();

    boolean isWebsiteInMannschaftslistenmode();

    boolean isWebsiteEnableDownloadLink();

    String getWebsiteDownloadLink();

    String getWebsiteTurnierTitel();

    boolean isWebsiteEnableProgrammDownloadLink();

    String getWebsiteProgrammDownloadLink();

    boolean isMobileLinkOn();

    String getMobileLink();

    int getPollrequestSpeaker();

    int getSpiellaengefinale();

    boolean isSkipDump();

    boolean isAnmeldephase();

    String getWebsiteFixString();

    void setWebsiteFixString(String in);

}
