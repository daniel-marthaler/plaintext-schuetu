package ch.plaintext.schuetu.entity;

import ch.plaintext.framework.SuperModel;
import ch.plaintext.schuetu.model.CreationDateProvider;
import ch.plaintext.schuetu.model.Einstellungen;
import ch.plaintext.schuetu.model.enums.SpielPhasenEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.persistence.Entity;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Zustand eines Games
 */
@Entity
@Data
@EqualsAndHashCode(callSuper = false)
public class GameModel extends SuperModel implements CreationDateProvider, Einstellungen {

    private Date creationdate = new Date();

    private String websiteFixString;

    private String gameName;

    @Deprecated
    private Boolean initialisiert = Boolean.FALSE;

    @Deprecated
    // verwende start
    private Date starttag = new Date();

    private String starttagstr = "";

    @Deprecated
    private boolean skipDump = false;

    private String test;

    private Date start = new Date();

    private int verschnellerungsFaktor = 1;

    @Deprecated
    private boolean startJetzt = true;

    private String spielVertauschungen;

    private int pause = 2;

    private int spiellaenge = 10;

    private int spiellaengefinale = 13;

    private int aufholzeitInSekunden = 60;

    private int pollrequestSpeaker = 1;

    private boolean automatischesAufholen = false;

    private boolean automatischesVorbereiten = false;

    @Deprecated // Nicht mehr verwendet seit 2026, DB-Feld beibehalten
    private boolean automatischesAnsagen = false;

    private boolean abbrechenZulassen = false;

    @Deprecated // Nicht mehr verwendet seit 2026, DB-Feld beibehalten
    private boolean gongEinschalten = false;

    private boolean behandleFinaleProKlassebeiZusammengefuehrten = true;

    @Deprecated
    private boolean webcamdemomode = false;

    @Deprecated
    private boolean webcamdemomodescharf = false;

    private boolean websiteInMannschaftslistenmode = false;

    private boolean websiteEnableDownloadLink = false;

    private String websiteDownloadLink = "";

    private String websiteId = "";

    private String websiteUrl = "https://www.schuelerturnierworb.ch/";

    private boolean websiteEnableProgrammDownloadLink = false;

    private String websiteProgrammDownloadLink = "";

    private String websiteTurnierTitel = "";

    private String websiteTurnierMeldung = "nichts";

    private boolean mobileLinkOn = false;

    private String mobileLink = "";

    @Deprecated // Nicht mehr verwendet seit 2026, ersetzt durch MQTT. DB-Feld beibehalten
    private String backportSync = "";
    @Deprecated // Nicht mehr verwendet seit 2026, ersetzt durch MQTT. DB-Feld beibehalten
    private boolean backportSyncOn = false;

    private boolean uploadOn = false;

    // MQTT Konfiguration
    private String mqttBrokerUrl = "tcp://192.168.1.224";
    private int mqttPort = 1883;
    private String mqttTopic = "schuetu/events";
    private String mqttMode = "OUT"; // OUT oder IN
    private boolean mqttEnabled = false;

    @Deprecated
    private boolean master = false;

    // 05 Spielverteilung
    private int zweiPausenBisKlasse = 3;

    private boolean groesser6AufC = true;

    private int groesser6AufCMin = 6;

    // anmeldung, kategorie, spieltage, spielen, abgeschlossen
    private String spielPhase = "anmeldung";

    public SpielPhasenEnum getPhase() {
        if (spielPhase.equals("anmeldung")) {
            return SpielPhasenEnum.A_ANMELDEPHASE;
        }

        if (spielPhase.equals("kategorie")) {
            return SpielPhasenEnum.B_KATEGORIE_ZUORDNUNG;
        }

        if (spielPhase.equals("spieltage")) {
            return SpielPhasenEnum.C_SPIELTAGE_DEFINIEREN;
        }

        if (spielPhase.equals("spielen")) {
            return SpielPhasenEnum.F_SPIELEN;
        }

        return SpielPhasenEnum.G_ABGESCHLOSSEN;
    }

    @Override
    public boolean getWebsiteInMannschaftslistenmode() {
        return websiteInMannschaftslistenmode;
    }

    @Override
    public boolean isWebsiteInMannschaftslistenmode() {
        return websiteInMannschaftslistenmode;
    }

    public boolean isAnmeldephase() {
        return this.getPhase() == SpielPhasenEnum.A_ANMELDEPHASE;
    }

    public GameModel() {
        LocalDateTime date = LocalDateTime.of(2013, 6, 8, 0, 0);
        starttag = Date.from(date.atZone(ZoneId.systemDefault()).toInstant());
    }

}
