package ch.plaintext.schuetu.service.exportimport;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * DTO fuer den vollstaendigen Export/Import eines Turniers.
 * Alle Entity-Beziehungen werden als ID-Referenzen gespeichert,
 * um zirkulaere Referenzen bei der JSON-Serialisierung zu vermeiden.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TournamentExportDto {

    private String exportVersion = "1.0";
    private String exportDate;
    private String originalGameName;

    private GameModelDto gameModel;
    private List<MannschaftDto> mannschaften = new ArrayList<>();
    private List<SchiriDto> schiris = new ArrayList<>();
    private List<KategorieDto> kategorien = new ArrayList<>();
    private List<GruppeDto> gruppen = new ArrayList<>();
    private List<SpielDto> spiele = new ArrayList<>();
    private List<SpielZeileDto> spielzeilen = new ArrayList<>();
    private List<PenaltyDto> penalties = new ArrayList<>();
    private List<KorrekturDto> korrekturen = new ArrayList<>();

    @Data
    public static class GameModelDto {
        private Long originalId;
        private String gameName;
        private String spielPhase;
        private int spiellaenge;
        private int spiellaengefinale;
        private int pause;
        private Date start;
        private String websiteFixString;
        private String websiteTurnierTitel;
        private boolean websiteInMannschaftslistenmode;
        private boolean websiteEnableDownloadLink;
        private boolean automatischesAufholen;
        private boolean automatischesVorbereiten;
        @Deprecated // Nicht mehr verwendet seit 2026
        private boolean automatischesAnsagen;
        @Deprecated // Nicht mehr verwendet seit 2026
        private boolean gongEinschalten;
        private boolean abbrechenZulassen;
        private int verschnellerungsFaktor;
        private int aufholzeitInSekunden;
        private boolean groesser6AufC;
        private int groesser6AufCMin;
        private int zweiPausenBisKlasse;
        private boolean mobileLinkOn;
        private String mobileLink;
        @Deprecated // Nicht mehr verwendet seit 2026, ersetzt durch MQTT
        private String backportSync;
        @Deprecated // Nicht mehr verwendet seit 2026, ersetzt durch MQTT
        private boolean backportSyncOn;
        private boolean uploadOn;
        private boolean behandleFinaleProKlassebeiZusammengefuehrten;
        private Boolean initialisiert;
    }

    @Data
    public static class MannschaftDto {
        private Long originalId;
        private String nickname;
        private int teamNummer;
        private int klasse;
        private String geschlecht;
        private String captainName;
        private String captainStrasse;
        private String captainPLZOrt;
        private String captainTelefon;
        private String captainEmail;
        private String captain2Name;
        private String begleitpersonName;
        private String begleitpersonStrasse;
        private String begleitpersonPLZOrt;
        private String begleitpersonTelefon;
        private String begleitpersonEmail;
        private String begleitperson2Name;
        private String schulhaus;
        private String farbe;
        private String color;
        private int anzahlSpieler;
        private Integer spielJahr;
        private String notizen;
        private String esr;
        private Boolean disqualifiziert;
        private String klassenBezeichnung;
        private String spielWunschHint;
        private String gr;
    }

    @Data
    public static class SchiriDto {
        private Long originalId;
        private String vorname;
        private String nachname;
        private String name;
        private String telefon;
        private String einteilung;
        private boolean aktiviert;
        private int matchcount;
        private String spielIDs;
        private String passwordHash;
        private String loginName;
    }

    @Data
    public static class KategorieDto {
        private Long originalId;
        private Long gruppeAId;
        private Long gruppeBId;
        private Long kleineFinalId;
        private Long grosserFinalId;
        private Long grosserfinal2Id;
        private Long penaltyAId;
        private Long penaltyBId;
        private String eintrager;
        private String notitzen;
        private String spielwunsch;
    }

    @Data
    public static class GruppeDto {
        private Long originalId;
        private Long kategorieId;
        private String geschlecht;
        private List<Long> mannschaftIds = new ArrayList<>();
        private List<Long> spielIds = new ArrayList<>();
    }

    @Data
    public static class SpielDto {
        private Long originalId;
        private String typ;
        private Long mannschaftAId;
        private Long mannschaftBId;
        private Long schiriId;
        private int toreA;
        private int toreB;
        private int toreABestaetigt;
        private int toreBBestaetigt;
        private boolean fertigGespielt;
        private boolean fertigEingetragen;
        private boolean fertigBestaetigt;
        private boolean zurueckgewiesen;
        private boolean amSpielen;
        private String platz;
        private Date start;
        private String idString;
        private String kategorieName;
        private String klasse;
        private String eintrager;
        private String schiriName;
        private String kontrolle;
        private String realName;
        private String notizen;
        private Boolean changedGrossToKlein;
        private String spielZeilenPhase;
    }

    @Data
    public static class SpielZeileDto {
        private Long originalId;
        private Long aId;
        private Long bId;
        private Long cId;
        private Long dId;
        private Date start;
        private boolean sonntag;
        private boolean pause;
        private boolean finale;
        private String phase;
        private String guid;
        private String gId;
    }

    @Data
    public static class PenaltyDto {
        private Long originalId;
        private Long gruppeId;
        private String reihenfolgeOrig;
        private String reihenfolge;
        private boolean gespielt;
        private boolean bestaetigt;
        private String idString;
        private List<Long> finalListIds = new ArrayList<>();
    }

    @Data
    public static class KorrekturDto {
        private Long originalId;
        private String typ;
        private String wert;
        private long reihenfolge;
    }
}
