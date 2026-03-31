package ch.plaintext.schuetu.importer;

import ch.plaintext.schuetu.entity.*;
import ch.plaintext.schuetu.model.enums.*;
import ch.plaintext.schuetu.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Service to import data from an HSQLDB .script file into the PostgreSQL database.
 * Parses INSERT INTO statements and creates entities via JPA repositories.
 */
@Service
@Slf4j
public class HsqldbImportService {

    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private GruppeRepository gruppeRepository;
    @Autowired
    private KategorieRepository kategorieRepository;
    @Autowired
    private MannschaftRepository mannschaftRepository;
    @Autowired
    private SpielRepository spielRepository;
    @Autowired
    private SpielZeilenRepository spielZeilenRepository;
    @Autowired
    private PenaltyRepository penaltyRepository;
    @Autowired
    private SchiriRepository schiriRepository;
    @Autowired
    private KorrekturRepository korrekturRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");

    // ID mappings: oldId -> newEntity (with newId set after save)
    private final Map<Long, GameModel> gameModelMap = new HashMap<>();
    private final Map<Long, Gruppe> gruppeMap = new HashMap<>();
    private final Map<Long, Kategorie> kategorieMap = new HashMap<>();
    private final Map<Long, Mannschaft> mannschaftMap = new HashMap<>();
    private final Map<Long, Spiel> spielMap = new HashMap<>();
    private final Map<Long, SpielZeile> spielZeileMap = new HashMap<>();
    private final Map<Long, Penalty> penaltyMap = new HashMap<>();
    private final Map<Long, Schiri> schiriMap = new HashMap<>();
    private final Map<Long, Korrektur> korrekturMap = new HashMap<>();

    // Deferred FK data for join tables and back-references
    private final List<long[]> gruppeMannschaftRows = new ArrayList<>();
    private final List<long[]> gruppeSpielRows = new ArrayList<>();
    private final List<long[]> penaltyMannschaftRows = new ArrayList<>();

    // Deferred: GRUPPE -> KATEGORIE_ID
    private final Map<Long, Long> gruppeKategorieMap = new HashMap<>();

    // Deferred: KATEGORIE FK references
    private final Map<Long, long[]> kategorieFkMap = new HashMap<>();

    // Deferred: MANNSCHAFT FK references (gruppeA_id, gruppeB_id)
    private final Map<Long, long[]> mannschaftGruppeFkMap = new HashMap<>();

    // Deferred: SPIEL FK references (mannschaftA_id, mannschaftB_id, schiri_id)
    private final Map<Long, long[]> spielFkMap = new HashMap<>();

    // Deferred: SPIELZEILE FK references (a_id, b_id, c_id, d_id)
    private final Map<Long, long[]> spielZeileFkMap = new HashMap<>();

    // Deferred: PENALTY FK references (gruppe_id)
    private final Map<Long, Long> penaltyGruppeFkMap = new HashMap<>();

    @Transactional
    public Map<String, Object> importFromScript(String scriptContent) {
        long startTime = System.currentTimeMillis();

        // Clear all maps
        clearMaps();

        // Delete all existing data in correct order (respect FK constraints)
        deleteAllData();

        // Collect all INSERT statements by table
        Map<String, List<String>> insertsByTable = parseInsertStatements(scriptContent);

        // Phase 1: GAMEMODEL, SCHIRI (no FKs to other schuetu tables)
        int gameModelCount = importGameModels(insertsByTable.getOrDefault("GAMEMODEL", Collections.emptyList()));
        int schiriCount = importSchiris(insertsByTable.getOrDefault("SCHIRI", Collections.emptyList()));

        // Phase 2: GRUPPE (FK kategorie_id nullable, set later)
        int gruppeCount = importGruppen(insertsByTable.getOrDefault("GRUPPE", Collections.emptyList()));

        // Phase 3: MANNSCHAFT (FK gruppeA_id, gruppeB_id -> GRUPPE)
        int mannschaftCount = importMannschaften(insertsByTable.getOrDefault("MANNSCHAFT", Collections.emptyList()));

        // Phase 4: SPIEL (FK mannschaftA_id, mannschaftB_id -> MANNSCHAFT, schiri_id -> SCHIRI)
        int spielCount = importSpiele(insertsByTable.getOrDefault("SPIEL", Collections.emptyList()));

        // Phase 5: KATEGORIE (FK gruppeA_id, gruppeB_id, kleineFinal_id, grosserFinal_id, grosserfinal2_id, penaltyA_id, penaltyB_id)
        int kategorieCount = importKategorien(insertsByTable.getOrDefault("KATEGORIE", Collections.emptyList()));

        // Phase 6: SPIELZEILE (FK a, b, c, d -> SPIEL)
        int spielZeileCount = importSpielZeilen(insertsByTable.getOrDefault("SPIELZEILE", Collections.emptyList()));

        // Phase 7: PENALTY (FK gruppe_id -> GRUPPE)
        int penaltyCount = importPenalties(insertsByTable.getOrDefault("PENALTY", Collections.emptyList()));

        // Phase 8: KORREKTUR (no FKs)
        int korrekturCount = importKorrekturen(insertsByTable.getOrDefault("KORREKTUR", Collections.emptyList()));

        // Phase 9: Join tables via native SQL
        int gruppeMannschaftCount = importGruppeMannschaft(insertsByTable.getOrDefault("GRUPPE_MANNSCHAFT", Collections.emptyList()));
        int gruppeSpielCount = importGruppeSpiel(insertsByTable.getOrDefault("GRUPPE_SPIEL", Collections.emptyList()));
        int penaltyMannschaftCount = importPenaltyMannschaft(insertsByTable.getOrDefault("PENALTY_MANNSCHAFT", Collections.emptyList()));

        // Phase 10: Back-update GRUPPE.kategorie_id
        int gruppeKategorieUpdated = backUpdateGruppeKategorie();

        entityManager.flush();

        long duration = System.currentTimeMillis() - startTime;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "OK");
        result.put("durationMs", duration);
        result.put("gameModel", gameModelCount);
        result.put("schiri", schiriCount);
        result.put("gruppe", gruppeCount);
        result.put("mannschaft", mannschaftCount);
        result.put("spiel", spielCount);
        result.put("kategorie", kategorieCount);
        result.put("spielZeile", spielZeileCount);
        result.put("penalty", penaltyCount);
        result.put("korrektur", korrekturCount);
        result.put("gruppeMannschaft", gruppeMannschaftCount);
        result.put("gruppeSpiel", gruppeSpielCount);
        result.put("penaltyMannschaft", penaltyMannschaftCount);
        result.put("gruppeKategorieUpdated", gruppeKategorieUpdated);

        return result;
    }

    private void clearMaps() {
        gameModelMap.clear();
        gruppeMap.clear();
        kategorieMap.clear();
        mannschaftMap.clear();
        spielMap.clear();
        spielZeileMap.clear();
        penaltyMap.clear();
        schiriMap.clear();
        korrekturMap.clear();
        gruppeMannschaftRows.clear();
        gruppeSpielRows.clear();
        penaltyMannschaftRows.clear();
        gruppeKategorieMap.clear();
        kategorieFkMap.clear();
        mannschaftGruppeFkMap.clear();
        spielFkMap.clear();
        spielZeileFkMap.clear();
        penaltyGruppeFkMap.clear();
    }

    private void deleteAllData() {
        log.info("Deleting all existing data...");
        // Delete in reverse FK order
        entityManager.createNativeQuery("DELETE FROM penalty_mannschaft").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM gruppe_spiel").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM gruppe_mannschaft").executeUpdate();
        korrekturRepository.deleteAll();
        spielZeilenRepository.deleteAll();
        entityManager.flush();
        // Nullify kategorie FKs before deleting spiel/penalty
        entityManager.createNativeQuery("UPDATE kategorie SET grosserfinal_id = NULL, kleinefinal_id = NULL, grosserfinal2_id = NULL, penaltya_id = NULL, penaltyb_id = NULL").executeUpdate();
        entityManager.flush();
        penaltyRepository.deleteAll();
        entityManager.flush();
        // Nullify spiel FKs before deleting mannschaft
        entityManager.createNativeQuery("UPDATE spiel SET mannschafta_id = NULL, mannschaftb_id = NULL, schiri_id = NULL").executeUpdate();
        entityManager.flush();
        spielRepository.deleteAll();
        entityManager.flush();
        // Nullify mannschaft FKs before deleting gruppe
        entityManager.createNativeQuery("UPDATE mannschaft SET gruppea_id = NULL, gruppeb_id = NULL").executeUpdate();
        entityManager.flush();
        mannschaftRepository.deleteAll();
        entityManager.flush();
        // Nullify gruppe FK before deleting kategorie
        entityManager.createNativeQuery("UPDATE gruppe SET kategorie_id = NULL").executeUpdate();
        entityManager.flush();
        kategorieRepository.deleteAll();
        entityManager.flush();
        gruppeRepository.deleteAll();
        schiriRepository.deleteAll();
        gameRepository.deleteAll();
        entityManager.flush();
        log.info("All existing data deleted.");
    }

    /**
     * Parses the HSQLDB .script content and groups INSERT INTO statements by table name.
     */
    private Map<String, List<String>> parseInsertStatements(String scriptContent) {
        Map<String, List<String>> result = new HashMap<>();
        // Switch to PUBLIC schema context
        boolean inPublicSchema = false;

        for (String line : scriptContent.split("\n")) {
            String trimmed = line.trim();

            if (trimmed.startsWith("SET SCHEMA PUBLIC")) {
                inPublicSchema = true;
                continue;
            }
            if (trimmed.startsWith("SET SCHEMA ") && !trimmed.startsWith("SET SCHEMA PUBLIC")) {
                inPublicSchema = false;
                continue;
            }

            if (inPublicSchema && trimmed.startsWith("INSERT INTO ")) {
                // Extract table name: INSERT INTO TABLENAME VALUES(...)
                int valuesIdx = trimmed.indexOf(" VALUES(");
                if (valuesIdx < 0) continue;
                String tablePart = trimmed.substring("INSERT INTO ".length(), valuesIdx).trim();
                result.computeIfAbsent(tablePart, k -> new ArrayList<>()).add(trimmed);
            }
        }

        log.info("Parsed INSERT statements: {}", result.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, e -> e.getValue().size())));

        return result;
    }

    /**
     * Parses the VALUES(...) part of an INSERT INTO statement.
     * Handles: quoted strings with '' escaping, NULL, TRUE/FALSE, integers, timestamps, CLOB references.
     */
    private List<String> parseValues(String insertStatement) {
        int valuesIdx = insertStatement.indexOf(" VALUES(");
        if (valuesIdx < 0) return Collections.emptyList();

        // Extract content between VALUES( and the closing )
        String valuesPart = insertStatement.substring(valuesIdx + " VALUES(".length());
        // Remove trailing )
        if (valuesPart.endsWith(")")) {
            valuesPart = valuesPart.substring(0, valuesPart.length() - 1);
        }

        List<String> values = new ArrayList<>();
        int i = 0;
        int len = valuesPart.length();

        while (i < len) {
            // Skip leading whitespace
            while (i < len && valuesPart.charAt(i) == ' ') i++;

            if (i >= len) break;

            char c = valuesPart.charAt(i);

            if (c == '\'') {
                // Quoted string - find end, handling '' escaping
                StringBuilder sb = new StringBuilder();
                i++; // skip opening quote
                while (i < len) {
                    char ch = valuesPart.charAt(i);
                    if (ch == '\'') {
                        // Check if it's an escaped quote ''
                        if (i + 1 < len && valuesPart.charAt(i + 1) == '\'') {
                            sb.append('\'');
                            i += 2;
                        } else {
                            // End of string
                            i++; // skip closing quote
                            break;
                        }
                    } else {
                        sb.append(ch);
                        i++;
                    }
                }
                values.add(sb.toString());
            } else {
                // Unquoted value: number, NULL, TRUE, FALSE, or CLOB reference
                StringBuilder sb = new StringBuilder();
                while (i < len && valuesPart.charAt(i) != ',') {
                    sb.append(valuesPart.charAt(i));
                    i++;
                }
                String val = sb.toString().trim();
                values.add(val);
            }

            // Skip comma separator
            if (i < len && valuesPart.charAt(i) == ',') {
                i++;
            }
        }

        return values;
    }

    private String getString(List<String> values, int index) {
        if (index >= values.size()) return null;
        String val = values.get(index);
        if ("NULL".equals(val)) return null;
        return val;
    }

    private Long getLong(List<String> values, int index) {
        if (index >= values.size()) return null;
        String val = values.get(index);
        if ("NULL".equals(val)) return null;
        try {
            return Long.parseLong(val);
        } catch (NumberFormatException e) {
            log.warn("Cannot parse long from '{}' at index {}", val, index);
            return null;
        }
    }

    private Integer getInt(List<String> values, int index) {
        if (index >= values.size()) return null;
        String val = values.get(index);
        if ("NULL".equals(val)) return null;
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            log.warn("Cannot parse int from '{}' at index {}", val, index);
            return null;
        }
    }

    private Boolean getBoolean(List<String> values, int index) {
        if (index >= values.size()) return null;
        String val = values.get(index);
        if ("NULL".equals(val)) return null;
        return "TRUE".equalsIgnoreCase(val);
    }

    private Date getTimestamp(List<String> values, int index) {
        if (index >= values.size()) return null;
        String val = values.get(index);
        if ("NULL".equals(val)) return null;
        try {
            synchronized (TIMESTAMP_FORMAT) {
                return TIMESTAMP_FORMAT.parse(val);
            }
        } catch (Exception e) {
            log.warn("Cannot parse timestamp from '{}' at index {}", val, index);
            return null;
        }
    }

    /**
     * Get a string value, treating CLOB references (e.g. numbers referencing LOB data) as null.
     */
    private String getStringOrClob(List<String> values, int index) {
        if (index >= values.size()) return null;
        String val = values.get(index);
        if ("NULL".equals(val)) return null;
        // CLOB references in HSQLDB are just numeric LOB IDs
        // If the value is purely numeric and we expect a string/clob field, treat as null
        try {
            Long.parseLong(val);
            // It's a CLOB reference (LOB ID) - treat as null
            return null;
        } catch (NumberFormatException e) {
            // It's an actual string value
            return val;
        }
    }

    // ========================
    // Phase 1: GAMEMODEL
    // ========================
    // CREATE TABLE GAMEMODEL: ID(0), ABBRECHENZULASSEN(1), AUFHOLZEITINSEKUNDEN(2), AUTOMATISCHESANSAGEN(3),
    // AUTOMATISCHESAUFHOLEN(4), AUTOMATISCHESVORBEREITEN(5), BEHANDLEFINALEPROKLASSEBEIZUSAMMENGEFUEHRTEN(6),
    // CREATIONDATE(7), GAMENAME(8), GONGEINSCHALTEN(9), GROESSER6AUFC(10), GROESSER6AUFCMIN(11),
    // INITIALISIERT(12), MASTER(13), MOBILELINK(14), MOBILELINKON(15), PAUSE(16), POLLREQUESTSPEAKER(17),
    // SKIPDUMP(18), SPIELPHASE(19), SPIELVERTAUSCHUNGEN(20), SPIELLAENGE(21), SPIELLAENGEFINALE(22),
    // START(23), STARTJETZT(24), STARTTAG(25), STARTTAGSTR(26), TEST(27), VERSCHNELLERUNGSFAKTOR(28),
    // WEBCAMDEMOMODE(29), WEBCAMDEMOMODESCHARF(30), WEBSITEDOWNLOADLINK(31), WEBSITEENABLEDOWNLOADLINK(32),
    // WEBSITEENABLEPROGRAMMDOWNLOADLINK(33), WEBSITEINMANNSCHAFTSLISTENMODE(34), WEBSITEPROGRAMMDOWNLOADLINK(35),
    // WEBSITETURNIERMELDUNG(36), WEBSITETURNIERTITEL(37), ZWEIPAUSENBISKLASSE(38), WEBSITEID(39), WEBSITEURL(40),
    // BACKPORTSYNC(41), BACKPORTSYNCON(42), UPLOADON(43), CREATEDBY(44), LASTMODIFIEDBY(45),
    // CREATEDDATE(46), LASTMODIFIEDDATE(47), MANDAT(48), DELETED(49), TAGS(50), WEBSITEFIXSTRING(51)
    private int importGameModels(List<String> inserts) {
        int count = 0;
        for (String insert : inserts) {
            try {
                List<String> v = parseValues(insert);
                Long oldId = getLong(v, 0);

                GameModel gm = new GameModel();
                gm.setAbbrechenZulassen(Boolean.TRUE.equals(getBoolean(v, 1)));
                gm.setAufholzeitInSekunden(getInt(v, 2) != null ? getInt(v, 2) : 60);
                gm.setAutomatischesAnsagen(Boolean.TRUE.equals(getBoolean(v, 3)));
                gm.setAutomatischesAufholen(Boolean.TRUE.equals(getBoolean(v, 4)));
                gm.setAutomatischesVorbereiten(Boolean.TRUE.equals(getBoolean(v, 5)));
                gm.setBehandleFinaleProKlassebeiZusammengefuehrten(Boolean.TRUE.equals(getBoolean(v, 6)));
                gm.setCreationdate(getTimestamp(v, 7));
                gm.setGameName(getString(v, 8));
                gm.setGongEinschalten(Boolean.TRUE.equals(getBoolean(v, 9)));
                gm.setGroesser6AufC(Boolean.TRUE.equals(getBoolean(v, 10)));
                gm.setGroesser6AufCMin(getInt(v, 11) != null ? getInt(v, 11) : 6);
                gm.setInitialisiert(getBoolean(v, 12));
                gm.setMaster(Boolean.TRUE.equals(getBoolean(v, 13)));
                gm.setMobileLink(getString(v, 14));
                gm.setMobileLinkOn(Boolean.TRUE.equals(getBoolean(v, 15)));
                gm.setPause(getInt(v, 16) != null ? getInt(v, 16) : 2);
                gm.setPollrequestSpeaker(getInt(v, 17) != null ? getInt(v, 17) : 1);
                gm.setSkipDump(Boolean.TRUE.equals(getBoolean(v, 18)));
                gm.setSpielPhase(getString(v, 19) != null ? getString(v, 19) : "anmeldung");
                gm.setSpielVertauschungen(getString(v, 20));
                gm.setSpiellaenge(getInt(v, 21) != null ? getInt(v, 21) : 10);
                gm.setSpiellaengefinale(getInt(v, 22) != null ? getInt(v, 22) : 13);
                gm.setStart(getTimestamp(v, 23));
                gm.setStartJetzt(Boolean.TRUE.equals(getBoolean(v, 24)));
                gm.setStarttag(getTimestamp(v, 25));
                gm.setStarttagstr(getString(v, 26) != null ? getString(v, 26) : "");
                gm.setTest(getString(v, 27));
                gm.setVerschnellerungsFaktor(getInt(v, 28) != null ? getInt(v, 28) : 1);
                gm.setWebcamdemomode(Boolean.TRUE.equals(getBoolean(v, 29)));
                gm.setWebcamdemomodescharf(Boolean.TRUE.equals(getBoolean(v, 30)));
                gm.setWebsiteDownloadLink(getString(v, 31) != null ? getString(v, 31) : "");
                gm.setWebsiteEnableDownloadLink(Boolean.TRUE.equals(getBoolean(v, 32)));
                gm.setWebsiteEnableProgrammDownloadLink(Boolean.TRUE.equals(getBoolean(v, 33)));
                gm.setWebsiteInMannschaftslistenmode(Boolean.TRUE.equals(getBoolean(v, 34)));
                gm.setWebsiteProgrammDownloadLink(getString(v, 35) != null ? getString(v, 35) : "");
                gm.setWebsiteTurnierMeldung(getString(v, 36) != null ? getString(v, 36) : "nichts");
                gm.setWebsiteTurnierTitel(getString(v, 37) != null ? getString(v, 37) : "");
                gm.setZweiPausenBisKlasse(getInt(v, 38) != null ? getInt(v, 38) : 3);
                gm.setWebsiteId(getString(v, 39) != null ? getString(v, 39) : "");
                gm.setWebsiteUrl(getString(v, 40) != null ? getString(v, 40) : "");
                gm.setBackportSync(getString(v, 41) != null ? getString(v, 41) : "");
                gm.setBackportSyncOn(Boolean.TRUE.equals(getBoolean(v, 42)));
                gm.setUploadOn(Boolean.TRUE.equals(getBoolean(v, 43)));
                gm.setCreatedBy(getString(v, 44));
                gm.setLastModifiedBy(getString(v, 45));
                gm.setCreatedDate(getTimestamp(v, 46));
                gm.setLastModifiedDate(getTimestamp(v, 47));
                gm.setMandat(getString(v, 48));
                gm.setDeleted(getBoolean(v, 49));
                // index 50: TAGS
                String tagsStr = getString(v, 50);
                gm.setTags(parseTags(tagsStr));
                gm.setWebsiteFixString(getString(v, 51));

                GameModel saved = gameRepository.save(gm);
                gameModelMap.put(oldId, saved);
                count++;
            } catch (Exception e) {
                log.error("Error importing GAMEMODEL: {}", insert, e);
            }
        }
        log.info("Imported {} GameModels", count);
        return count;
    }

    // ========================
    // Phase 1: SCHIRI
    // ========================
    // CREATE TABLE SCHIRI: ID(0), AKTIVIERT(1), CREATIONDATE(2), GAME(3), MATCHCOUNT(4),
    // NAME(5), SPIELIDS(6), VORNAME(7), NACHNAME(8), CREATEDBY(9), LASTMODIFIEDBY(10),
    // CREATEDDATE(11), LASTMODIFIEDDATE(12), MANDAT(13), DELETED(14), EINTEILUNG(15), TAGS(16)
    private int importSchiris(List<String> inserts) {
        int count = 0;
        for (String insert : inserts) {
            try {
                List<String> v = parseValues(insert);
                Long oldId = getLong(v, 0);

                Schiri s = new Schiri();
                s.setAktiviert(Boolean.TRUE.equals(getBoolean(v, 1)));
                s.setCreationdate(getTimestamp(v, 2));
                s.setGame(getString(v, 3));
                s.setMatchcount(getInt(v, 4) != null ? getInt(v, 4) : 0);
                s.setName(getString(v, 5));
                s.setSpielIDs(getString(v, 6) != null ? getString(v, 6) : "");
                s.setVorname(getString(v, 7));
                s.setNachname(getString(v, 8));
                s.setCreatedBy(getString(v, 9));
                s.setLastModifiedBy(getString(v, 10));
                s.setCreatedDate(getTimestamp(v, 11));
                s.setLastModifiedDate(getTimestamp(v, 12));
                s.setMandat(getString(v, 13));
                s.setDeleted(getBoolean(v, 14));
                s.setEinteilung(getString(v, 15));
                s.setTags(parseTags(getString(v, 16)));

                Schiri saved = schiriRepository.save(s);
                schiriMap.put(oldId, saved);
                count++;
            } catch (Exception e) {
                log.error("Error importing SCHIRI: {}", insert, e);
            }
        }
        log.info("Imported {} Schiris", count);
        return count;
    }

    // ========================
    // Phase 2: GRUPPE
    // ========================
    // CREATE TABLE GRUPPE: ID(0), CREATIONDATE(1), GAME(2), GESCHLECHT(3), KATEGORIE_ID(4),
    // CREATEDBY(5), LASTMODIFIEDBY(6), CREATEDDATE(7), LASTMODIFIEDDATE(8), MANDAT(9), DELETED(10), TAGS(11)
    private int importGruppen(List<String> inserts) {
        int count = 0;
        for (String insert : inserts) {
            try {
                List<String> v = parseValues(insert);
                Long oldId = getLong(v, 0);

                Gruppe g = new Gruppe();
                g.setCreationdate(getTimestamp(v, 1));
                g.setGame(getString(v, 2));
                Integer geschlechtOrdinal = getInt(v, 3);
                if (geschlechtOrdinal != null) {
                    g.setGeschlecht(GeschlechtEnum.values()[geschlechtOrdinal]);
                }
                // kategorie_id(4) -> deferred, will be set in Phase 10
                Long kategorieId = getLong(v, 4);
                if (kategorieId != null) {
                    gruppeKategorieMap.put(oldId, kategorieId);
                }
                g.setCreatedBy(getString(v, 5));
                g.setLastModifiedBy(getString(v, 6));
                g.setCreatedDate(getTimestamp(v, 7));
                g.setLastModifiedDate(getTimestamp(v, 8));
                g.setMandat(getString(v, 9));
                g.setDeleted(getBoolean(v, 10));
                g.setTags(parseTags(getString(v, 11)));

                // Clear default collections to avoid cascading issues
                g.setMannschaften(new ArrayList<>());
                g.setSpiele(new ArrayList<>());

                Gruppe saved = gruppeRepository.save(g);
                gruppeMap.put(oldId, saved);
                count++;
            } catch (Exception e) {
                log.error("Error importing GRUPPE: {}", insert, e);
            }
        }
        log.info("Imported {} Gruppen", count);
        return count;
    }

    // ========================
    // Phase 3: MANNSCHAFT
    // ========================
    // CREATE TABLE MANNSCHAFT: ID(0), ANZAHLSPIELER(1), BEGLEITPERSONANREDE(2), BEGLEITPERSONEMAIL(3),
    // BEGLEITPERSONNAME(4), BEGLEITPERSONPLZORT(5), BEGLEITPERSONSTRASSE(6), BEGLEITPERSONTELEFON(7),
    // CAPTAINEMAIL(8), CAPTAINNAME(9), CAPTAINPLZORT(10), CAPTAINSTRASSE(11), CAPTAINTELEFON(12),
    // COLOR(13), CREATIONDATE(14), ESR(15), FARBE(16), GAME(17), GESCHLECHT(18), KLASSE(19),
    // KLASSENBEZEICHNUNG(20), NICKNAME(21), NOTIZEN(CLOB)(22), SCHULHAUS(23), SPIELJAHR(24),
    // SPIELWUNSCHHINT(25), TEAMNUMMER(26), GRUPPEA_ID(27), GRUPPEB_ID(28), GR(29),
    // CREATEDBY(30), LASTMODIFIEDBY(31), CREATEDDATE(32), LASTMODIFIEDDATE(33), MANDAT(34),
    // DELETED(35), GESPERRT(36), BEGLEITPERSON2NAME(37), BEGLEITPERSON2VORNAME(38),
    // CAPTAIN2NAME(39), CAPTAIN2VORNAME(40), TAGS(41), DISQUALIFIZIERT(42)
    private int importMannschaften(List<String> inserts) {
        int count = 0;
        for (String insert : inserts) {
            try {
                List<String> v = parseValues(insert);
                Long oldId = getLong(v, 0);

                Mannschaft m = new Mannschaft();
                m.setAnzahlSpieler(getInt(v, 1) != null ? getInt(v, 1) : 0);
                m.setBegleitpersonAnrede(getString(v, 2) != null ? getString(v, 2) : "");
                m.setBegleitpersonEmail(getString(v, 3) != null ? getString(v, 3) : "");
                m.setBegleitpersonName(getString(v, 4) != null ? getString(v, 4) : "");
                m.setBegleitpersonPLZOrt(getString(v, 5) != null ? getString(v, 5) : "");
                m.setBegleitpersonStrasse(getString(v, 6) != null ? getString(v, 6) : "");
                m.setBegleitpersonTelefon(getString(v, 7) != null ? getString(v, 7) : "");
                m.setCaptainEmail(getString(v, 8) != null ? getString(v, 8) : "");
                m.setCaptainName(getString(v, 9) != null ? getString(v, 9) : "");
                m.setCaptainPLZOrt(getString(v, 10) != null ? getString(v, 10) : "");
                m.setCaptainStrasse(getString(v, 11) != null ? getString(v, 11) : "");
                m.setCaptainTelefon(getString(v, 12) != null ? getString(v, 12) : "");
                m.setColor(getString(v, 13) != null ? getString(v, 13) : "Blau");
                m.setCreationdate(getTimestamp(v, 14));
                m.setEsr(getString(v, 15) != null ? getString(v, 15) : "");
                m.setFarbe(getString(v, 16) != null ? getString(v, 16) : "");
                m.setGame(getString(v, 17) != null ? getString(v, 17) : "");
                Integer geschlechtOrdinal = getInt(v, 18);
                if (geschlechtOrdinal != null) {
                    m.setGeschlecht(GeschlechtEnum.values()[geschlechtOrdinal]);
                }
                m.setKlasse(getInt(v, 19) != null ? getInt(v, 19) : 0);
                m.setKlassenBezeichnung(getString(v, 20));
                m.setNickname(getString(v, 21) != null ? getString(v, 21) : "");
                // NOTIZEN is CLOB - index 22
                m.setNotizen(getStringOrClob(v, 22) != null ? getStringOrClob(v, 22) : "");
                m.setSchulhaus(getString(v, 23) != null ? getString(v, 23) : "");
                m.setSpielJahr(getInt(v, 24) != null ? getInt(v, 24) : 2000);
                m.setSpielWunschHint(getString(v, 25) != null ? getString(v, 25) : "");
                m.setTeamNummer(getInt(v, 26) != null ? getInt(v, 26) : 0);

                // FK references (27=gruppeA_id, 28=gruppeB_id) -> resolve from gruppeMap
                Long gruppeAId = getLong(v, 27);
                Long gruppeBId = getLong(v, 28);
                if (gruppeAId != null && gruppeMap.containsKey(gruppeAId)) {
                    m.setGruppeA(gruppeMap.get(gruppeAId));
                }
                if (gruppeBId != null && gruppeMap.containsKey(gruppeBId)) {
                    m.setGruppeB(gruppeMap.get(gruppeBId));
                }

                m.setGr(getString(v, 29));
                m.setCreatedBy(getString(v, 30));
                m.setLastModifiedBy(getString(v, 31));
                m.setCreatedDate(getTimestamp(v, 32));
                m.setLastModifiedDate(getTimestamp(v, 33));
                m.setMandat(getString(v, 34));
                m.setDeleted(getBoolean(v, 35));
                // GESPERRT field is in SuperModel? No, it's on Mannschaft entity but not visible
                // The entity doesn't have a gesperrt field - it might be handled by SuperModel or ignored
                // Index 36: GESPERRT - not present in entity, skip
                m.setBegleitperson2Name(getString(v, 37));
                m.setBegleitperson2Vorname(getString(v, 38));
                m.setCaptain2Name(getString(v, 39));
                m.setCaptain2Vorname(getString(v, 40));
                m.setTags(parseTags(getString(v, 41)));
                m.setDisqualifiziert(getBoolean(v, 42));

                Mannschaft saved = mannschaftRepository.save(m);
                mannschaftMap.put(oldId, saved);
                count++;
            } catch (Exception e) {
                log.error("Error importing MANNSCHAFT: {}", insert, e);
            }
        }
        log.info("Imported {} Mannschaften", count);
        return count;
    }

    // ========================
    // Phase 4: SPIEL
    // ========================
    // CREATE TABLE SPIEL: ID(0), AMSPIELEN(1), CREATIONDATE(2), FERTIGBESTAETIGT(3),
    // FERTIGEINGETRAGEN(4), FERTIGGESPIELT(5), GAME(6), IDSTRING(7), KATEGORIENAME(8),
    // NOTIZEN(CLOB)(9), PLATZ(10), REALNAME(11), SCHIRINAME(12), SPIELZEILENPHASE(13),
    // START(14), TOREA(15), TOREABESTAETIGT(16), TOREB(17), TOREBBESTAETIGT(18), TYP(19),
    // ZURUECKGEWIESEN(20), MANNSCHAFTA_ID(21), MANNSCHAFTB_ID(22), SCHIRI_ID(23),
    // EINTRAGER(24), KONTROLLE(25), KLASSE(26), CREATEDBY(27), LASTMODIFIEDBY(28),
    // CREATEDDATE(29), LASTMODIFIEDDATE(30), MANDAT(31), DELETED(32), GESPERRT(33),
    // CHANGEDGROSSTOKLEIN(34), TAGS(35)
    private int importSpiele(List<String> inserts) {
        int count = 0;
        for (String insert : inserts) {
            try {
                List<String> v = parseValues(insert);
                Long oldId = getLong(v, 0);

                Spiel s = new Spiel();
                s.setAmSpielen(Boolean.TRUE.equals(getBoolean(v, 1)));
                s.setCreationdate(getTimestamp(v, 2));
                s.setFertigBestaetigt(Boolean.TRUE.equals(getBoolean(v, 3)));
                s.setFertigEingetragen(Boolean.TRUE.equals(getBoolean(v, 4)));
                s.setFertigGespielt(Boolean.TRUE.equals(getBoolean(v, 5)));
                s.setGame(getString(v, 6));
                s.setIdString(getString(v, 7));
                s.setKategorieName(getString(v, 8));
                // NOTIZEN is CLOB - index 9
                s.setNotizen(getStringOrClob(v, 9) != null ? getStringOrClob(v, 9) : "");

                // PLATZ is stored as integer ordinal in HSQLDB
                Integer platzOrdinal = getInt(v, 10);
                if (platzOrdinal != null) {
                    PlatzEnum[] platzValues = PlatzEnum.values();
                    if (platzOrdinal >= 0 && platzOrdinal < platzValues.length) {
                        s.setPlatz(platzValues[platzOrdinal]);
                    }
                }

                s.setRealName(getString(v, 11) != null ? getString(v, 11) : "");
                s.setSchiriName(getString(v, 12));

                // SPIELZEILENPHASE stored as integer ordinal
                Integer spielZeilenPhaseOrdinal = getInt(v, 13);
                if (spielZeilenPhaseOrdinal != null) {
                    SpielZeilenPhaseEnum[] phaseValues = SpielZeilenPhaseEnum.values();
                    if (spielZeilenPhaseOrdinal >= 0 && spielZeilenPhaseOrdinal < phaseValues.length) {
                        s.setSpielZeilenPhase(phaseValues[spielZeilenPhaseOrdinal]);
                    }
                }

                s.setStart(getTimestamp(v, 14));
                s.setToreA(getInt(v, 15) != null ? getInt(v, 15) : -1);
                s.setToreABestaetigt(getInt(v, 16) != null ? getInt(v, 16) : -1);
                s.setToreB(getInt(v, 17) != null ? getInt(v, 17) : -1);
                s.setToreBBestaetigt(getInt(v, 18) != null ? getInt(v, 18) : -1);

                // TYP stored as integer ordinal
                Integer typOrdinal = getInt(v, 19);
                if (typOrdinal != null) {
                    SpielEnum[] typValues = SpielEnum.values();
                    if (typOrdinal >= 0 && typOrdinal < typValues.length) {
                        s.setTyp(typValues[typOrdinal]);
                    }
                }

                s.setZurueckgewiesen(Boolean.TRUE.equals(getBoolean(v, 20)));

                // FK references
                Long mannschaftAId = getLong(v, 21);
                Long mannschaftBId = getLong(v, 22);
                Long schiriId = getLong(v, 23);

                if (mannschaftAId != null && mannschaftMap.containsKey(mannschaftAId)) {
                    s.setMannschaftA(mannschaftMap.get(mannschaftAId));
                }
                if (mannschaftBId != null && mannschaftMap.containsKey(mannschaftBId)) {
                    s.setMannschaftB(mannschaftMap.get(mannschaftBId));
                }
                if (schiriId != null && schiriMap.containsKey(schiriId)) {
                    s.setSchiri(schiriMap.get(schiriId));
                }

                s.setEintrager(getString(v, 24));
                s.setKontrolle(getString(v, 25));
                s.setKlasse(getString(v, 26) != null ? getString(v, 26) : "");
                s.setCreatedBy(getString(v, 27));
                s.setLastModifiedBy(getString(v, 28));
                s.setCreatedDate(getTimestamp(v, 29));
                s.setLastModifiedDate(getTimestamp(v, 30));
                s.setMandat(getString(v, 31));
                s.setDeleted(getBoolean(v, 32));
                // Index 33: GESPERRT - not in entity as a field, skip
                s.setChangedGrossToKlein(getBoolean(v, 34));
                s.setTags(parseTags(getString(v, 35)));

                Spiel saved = spielRepository.save(s);
                spielMap.put(oldId, saved);
                count++;
            } catch (Exception e) {
                log.error("Error importing SPIEL: {}", insert, e);
            }
        }
        log.info("Imported {} Spiele", count);
        return count;
    }

    // ========================
    // Phase 5: KATEGORIE
    // ========================
    // CREATE TABLE KATEGORIE: ID(0), CREATIONDATE(1), GAME(2), SPIELWUNSCH(3),
    // GROSSERFINAL_ID(4), GRUPPEA_ID(5), GRUPPEB_ID(6), KLEINEFINAL_ID(7),
    // PENALTYA_ID(8), PENALTYB_ID(9), NOTITZEN(10), EINTRAGER(11), NOSAVE(12),
    // GROSSERFINAL2_ID(13), CREATEDBY(14), LASTMODIFIEDBY(15), CREATEDDATE(16),
    // LASTMODIFIEDDATE(17), MANDAT(18), DELETED(19), TAGS(20)
    private int importKategorien(List<String> inserts) {
        int count = 0;
        for (String insert : inserts) {
            try {
                List<String> v = parseValues(insert);
                Long oldId = getLong(v, 0);

                Kategorie k = new Kategorie();
                k.setCreationdate(getTimestamp(v, 1));
                k.setGame(getString(v, 2));

                // SPIELWUNSCH stored as integer ordinal
                Integer spielwunschOrdinal = getInt(v, 3);
                if (spielwunschOrdinal != null) {
                    SpielTageszeit[] tageszeiten = SpielTageszeit.values();
                    if (spielwunschOrdinal >= 0 && spielwunschOrdinal < tageszeiten.length) {
                        k.setSpielwunsch(tageszeiten[spielwunschOrdinal]);
                    }
                }

                // FK references to SPIEL
                Long grosserFinalId = getLong(v, 4);
                if (grosserFinalId != null && spielMap.containsKey(grosserFinalId)) {
                    k.setGrosserFinal(spielMap.get(grosserFinalId));
                }

                Long gruppeAId = getLong(v, 5);
                if (gruppeAId != null && gruppeMap.containsKey(gruppeAId)) {
                    k.setGruppeA(gruppeMap.get(gruppeAId));
                }

                Long gruppeBId = getLong(v, 6);
                if (gruppeBId != null && gruppeMap.containsKey(gruppeBId)) {
                    k.setGruppeB(gruppeMap.get(gruppeBId));
                }

                Long kleineFinalId = getLong(v, 7);
                if (kleineFinalId != null && spielMap.containsKey(kleineFinalId)) {
                    k.setKleineFinal(spielMap.get(kleineFinalId));
                }

                // PenaltyA/B will be set after penalties are imported, but we parse now
                // Actually penalties haven't been imported yet in Phase 5, so we store deferred
                Long penaltyAId = getLong(v, 8);
                Long penaltyBId = getLong(v, 9);
                Long grosserfinal2Id = getLong(v, 13);

                k.setNotitzen(getString(v, 10));
                k.setEintrager(getString(v, 11));
                k.setNosave(getBoolean(v, 12));

                // grosserfinal2
                if (grosserfinal2Id != null && spielMap.containsKey(grosserfinal2Id)) {
                    k.setGrosserfinal2(spielMap.get(grosserfinal2Id));
                }

                k.setCreatedBy(getString(v, 14));
                k.setLastModifiedBy(getString(v, 15));
                k.setCreatedDate(getTimestamp(v, 16));
                k.setLastModifiedDate(getTimestamp(v, 17));
                k.setMandat(getString(v, 18));
                k.setDeleted(getBoolean(v, 19));
                k.setTags(parseTags(getString(v, 20)));

                Kategorie saved = kategorieRepository.save(k);
                kategorieMap.put(oldId, saved);

                // Store deferred penalty FKs
                if (penaltyAId != null || penaltyBId != null) {
                    kategorieFkMap.put(oldId, new long[]{
                            penaltyAId != null ? penaltyAId : -1,
                            penaltyBId != null ? penaltyBId : -1
                    });
                }

                count++;
            } catch (Exception e) {
                log.error("Error importing KATEGORIE: {}", insert, e);
            }
        }
        log.info("Imported {} Kategorien", count);
        return count;
    }

    // ========================
    // Phase 6: SPIELZEILE
    // ========================
    // CREATE TABLE SPIELZEILE: ID(0), CREATIONDATE(1), FINALE(2), GID(3), GAME(4),
    // GUID(5), PAUSE(6), PHASE(7), SONNTAG(8), SPIELTAGESZEIT(9), START(10),
    // A_ID(11), B_ID(12), C_ID(13), CREATEDBY(14), LASTMODIFIEDBY(15), CREATEDDATE(16),
    // LASTMODIFIEDDATE(17), MANDAT(18), DELETED(19), TAGS(20), D_ID(21)
    private int importSpielZeilen(List<String> inserts) {
        int count = 0;
        for (String insert : inserts) {
            try {
                List<String> v = parseValues(insert);
                Long oldId = getLong(v, 0);

                SpielZeile sz = new SpielZeile();
                sz.setCreationdate(getTimestamp(v, 1));
                sz.setFinale(Boolean.TRUE.equals(getBoolean(v, 2)));
                sz.setGId(getString(v, 3));
                sz.setGame(getString(v, 4));
                sz.setGuid(getString(v, 5));
                sz.setPause(Boolean.TRUE.equals(getBoolean(v, 6)));

                // PHASE stored as integer ordinal
                Integer phaseOrdinal = getInt(v, 7);
                if (phaseOrdinal != null) {
                    SpielZeilenPhaseEnum[] phaseValues = SpielZeilenPhaseEnum.values();
                    if (phaseOrdinal >= 0 && phaseOrdinal < phaseValues.length) {
                        sz.setPhase(phaseValues[phaseOrdinal]);
                    }
                }

                sz.setSonntag(Boolean.TRUE.equals(getBoolean(v, 8)));

                // SPIELTAGESZEIT stored as integer ordinal
                Integer tageszeit = getInt(v, 9);
                if (tageszeit != null) {
                    SpielTageszeit[] tzValues = SpielTageszeit.values();
                    if (tageszeit >= 0 && tageszeit < tzValues.length) {
                        sz.setSpieltageszeit(tzValues[tageszeit]);
                    }
                }

                sz.setStart(getTimestamp(v, 10));

                // FK references to SPIEL: A_ID(11), B_ID(12), C_ID(13)
                Long aId = getLong(v, 11);
                Long bId = getLong(v, 12);
                Long cId = getLong(v, 13);
                Long dId = getLong(v, 21);

                if (aId != null && spielMap.containsKey(aId)) {
                    sz.setA(spielMap.get(aId));
                }
                if (bId != null && spielMap.containsKey(bId)) {
                    sz.setB(spielMap.get(bId));
                }
                if (cId != null && spielMap.containsKey(cId)) {
                    sz.setC(spielMap.get(cId));
                }
                if (dId != null && spielMap.containsKey(dId)) {
                    sz.setD(spielMap.get(dId));
                }

                sz.setCreatedBy(getString(v, 14));
                sz.setLastModifiedBy(getString(v, 15));
                sz.setCreatedDate(getTimestamp(v, 16));
                sz.setLastModifiedDate(getTimestamp(v, 17));
                sz.setMandat(getString(v, 18));
                sz.setDeleted(getBoolean(v, 19));
                sz.setTags(parseTags(getString(v, 20)));

                SpielZeile saved = spielZeilenRepository.save(sz);
                spielZeileMap.put(oldId, saved);
                count++;
            } catch (Exception e) {
                log.error("Error importing SPIELZEILE: {}", insert, e);
            }
        }
        log.info("Imported {} SpielZeilen", count);
        return count;
    }

    // ========================
    // Phase 7: PENALTY
    // ========================
    // CREATE TABLE PENALTY: ID(0), BESTAETIGT(1), CREATIONDATE(2), GAME(3), GESPIELT(4),
    // IDSTRING(5), REIHENFOLGE(6), REIHENFOLGEORIG(7), GRUPPE_ID(8),
    // CREATEDBY(9), LASTMODIFIEDBY(10), CREATEDDATE(11), LASTMODIFIEDDATE(12),
    // MANDAT(13), DELETED(14), TAGS(15)
    private int importPenalties(List<String> inserts) {
        int count = 0;
        for (String insert : inserts) {
            try {
                List<String> v = parseValues(insert);
                Long oldId = getLong(v, 0);

                Penalty p = new Penalty();
                p.setBestaetigt(Boolean.TRUE.equals(getBoolean(v, 1)));
                p.setCreationdate(getTimestamp(v, 2));
                p.setGame(getString(v, 3));
                p.setGespielt(Boolean.TRUE.equals(getBoolean(v, 4)));
                p.setIdString(getString(v, 5));
                p.setReihenfolge(getString(v, 6) != null ? getString(v, 6) : Penalty.LEER);
                p.setReihenfolgeOrig(getString(v, 7) != null ? getString(v, 7) : Penalty.LEER);

                // FK: gruppe_id(8) -> GRUPPE
                Long gruppeId = getLong(v, 8);
                if (gruppeId != null && gruppeMap.containsKey(gruppeId)) {
                    p.setGruppe(gruppeMap.get(gruppeId));
                }

                p.setCreatedBy(getString(v, 9));
                p.setLastModifiedBy(getString(v, 10));
                p.setCreatedDate(getTimestamp(v, 11));
                p.setLastModifiedDate(getTimestamp(v, 12));
                p.setMandat(getString(v, 13));
                p.setDeleted(getBoolean(v, 14));
                p.setTags(parseTags(getString(v, 15)));

                Penalty saved = penaltyRepository.save(p);
                penaltyMap.put(oldId, saved);
                count++;
            } catch (Exception e) {
                log.error("Error importing PENALTY: {}", insert, e);
            }
        }

        // Now back-update KATEGORIE penalty references
        for (Map.Entry<Long, long[]> entry : kategorieFkMap.entrySet()) {
            Long kategorieOldId = entry.getKey();
            long[] penaltyIds = entry.getValue();
            Kategorie k = kategorieMap.get(kategorieOldId);
            if (k == null) continue;

            boolean updated = false;
            if (penaltyIds[0] > 0 && penaltyMap.containsKey(penaltyIds[0])) {
                k.setPenaltyA(penaltyMap.get(penaltyIds[0]));
                updated = true;
            }
            if (penaltyIds[1] > 0 && penaltyMap.containsKey(penaltyIds[1])) {
                k.setPenaltyB(penaltyMap.get(penaltyIds[1]));
                updated = true;
            }
            if (updated) {
                kategorieRepository.save(k);
            }
        }

        log.info("Imported {} Penalties", count);
        return count;
    }

    // ========================
    // Phase 8: KORREKTUR
    // ========================
    // CREATE TABLE KORREKTUR: ID(0), CREATIONDATE(1), GAME(2), REIHENFOLGE(3), TYP(4),
    // WERT(5), CREATEDBY(6), LASTMODIFIEDBY(7), CREATEDDATE(8), LASTMODIFIEDDATE(9),
    // MANDAT(10), DELETED(11), TAGS(12)
    private int importKorrekturen(List<String> inserts) {
        int count = 0;
        for (String insert : inserts) {
            try {
                List<String> v = parseValues(insert);
                Long oldId = getLong(v, 0);

                Korrektur k = new Korrektur();
                k.setCreationdate(getTimestamp(v, 1));
                k.setGame(getString(v, 2));
                Long reihenfolge = getLong(v, 3);
                k.setReihenfolge(reihenfolge != null ? reihenfolge : 0);
                k.setTyp(getString(v, 4));
                k.setWert(getString(v, 5));
                k.setCreatedBy(getString(v, 6));
                k.setLastModifiedBy(getString(v, 7));
                k.setCreatedDate(getTimestamp(v, 8));
                k.setLastModifiedDate(getTimestamp(v, 9));
                k.setMandat(getString(v, 10));
                k.setDeleted(getBoolean(v, 11));
                k.setTags(parseTags(getString(v, 12)));

                Korrektur saved = korrekturRepository.save(k);
                korrekturMap.put(oldId, saved);
                count++;
            } catch (Exception e) {
                log.error("Error importing KORREKTUR: {}", insert, e);
            }
        }
        log.info("Imported {} Korrekturen", count);
        return count;
    }

    // ========================
    // Phase 9: Join tables via native SQL
    // ========================

    // GRUPPE_MANNSCHAFT: GRUPPE_ID(0), MANNSCHAFTEN_ID(1)
    private int importGruppeMannschaft(List<String> inserts) {
        int count = 0;
        for (String insert : inserts) {
            try {
                List<String> v = parseValues(insert);
                Long oldGruppeId = getLong(v, 0);
                Long oldMannschaftId = getLong(v, 1);

                Gruppe gruppe = gruppeMap.get(oldGruppeId);
                Mannschaft mannschaft = mannschaftMap.get(oldMannschaftId);

                if (gruppe != null && mannschaft != null) {
                    entityManager.createNativeQuery(
                                    "INSERT INTO gruppe_mannschaft (gruppe_id, mannschaften_id) VALUES (:gid, :mid)")
                            .setParameter("gid", gruppe.getId())
                            .setParameter("mid", mannschaft.getId())
                            .executeUpdate();
                    count++;
                } else {
                    log.warn("GRUPPE_MANNSCHAFT: missing mapping for gruppeId={}, mannschaftId={}",
                            oldGruppeId, oldMannschaftId);
                }
            } catch (Exception e) {
                log.error("Error importing GRUPPE_MANNSCHAFT: {}", insert, e);
            }
        }
        log.info("Imported {} Gruppe_Mannschaft rows", count);
        return count;
    }

    // GRUPPE_SPIEL: GRUPPE_ID(0), SPIELE_ID(1)
    private int importGruppeSpiel(List<String> inserts) {
        int count = 0;
        for (String insert : inserts) {
            try {
                List<String> v = parseValues(insert);
                Long oldGruppeId = getLong(v, 0);
                Long oldSpielId = getLong(v, 1);

                Gruppe gruppe = gruppeMap.get(oldGruppeId);
                Spiel spiel = spielMap.get(oldSpielId);

                if (gruppe != null && spiel != null) {
                    entityManager.createNativeQuery(
                                    "INSERT INTO gruppe_spiel (gruppe_id, spiele_id) VALUES (:gid, :sid)")
                            .setParameter("gid", gruppe.getId())
                            .setParameter("sid", spiel.getId())
                            .executeUpdate();
                    count++;
                } else {
                    log.warn("GRUPPE_SPIEL: missing mapping for gruppeId={}, spielId={}",
                            oldGruppeId, oldSpielId);
                }
            } catch (Exception e) {
                log.error("Error importing GRUPPE_SPIEL: {}", insert, e);
            }
        }
        log.info("Imported {} Gruppe_Spiel rows", count);
        return count;
    }

    // PENALTY_MANNSCHAFT: PENALTY_ID(0), FINALLIST_ID(1)
    private int importPenaltyMannschaft(List<String> inserts) {
        int count = 0;
        for (String insert : inserts) {
            try {
                List<String> v = parseValues(insert);
                Long oldPenaltyId = getLong(v, 0);
                Long oldMannschaftId = getLong(v, 1);

                Penalty penalty = penaltyMap.get(oldPenaltyId);
                Mannschaft mannschaft = mannschaftMap.get(oldMannschaftId);

                if (penalty != null && mannschaft != null) {
                    entityManager.createNativeQuery(
                                    "INSERT INTO penalty_mannschaft (penalty_id, finallist_id) VALUES (:pid, :mid)")
                            .setParameter("pid", penalty.getId())
                            .setParameter("mid", mannschaft.getId())
                            .executeUpdate();
                    count++;
                } else {
                    log.warn("PENALTY_MANNSCHAFT: missing mapping for penaltyId={}, mannschaftId={}",
                            oldPenaltyId, oldMannschaftId);
                }
            } catch (Exception e) {
                log.error("Error importing PENALTY_MANNSCHAFT: {}", insert, e);
            }
        }
        log.info("Imported {} Penalty_Mannschaft rows", count);
        return count;
    }

    // ========================
    // Phase 10: Back-update GRUPPE.kategorie_id
    // ========================
    private int backUpdateGruppeKategorie() {
        int count = 0;
        for (Map.Entry<Long, Long> entry : gruppeKategorieMap.entrySet()) {
            Long oldGruppeId = entry.getKey();
            Long oldKategorieId = entry.getValue();

            Gruppe gruppe = gruppeMap.get(oldGruppeId);
            Kategorie kategorie = kategorieMap.get(oldKategorieId);

            if (gruppe != null && kategorie != null) {
                gruppe.setKategorie(kategorie);
                gruppeRepository.save(gruppe);
                count++;
            } else {
                log.warn("GRUPPE->KATEGORIE back-update: missing mapping for gruppeId={}, kategorieId={}",
                        oldGruppeId, oldKategorieId);
            }
        }
        log.info("Back-updated {} Gruppe->Kategorie references", count);
        return count;
    }

    /**
     * Parses a tags string from HSQLDB format.
     * Tags in SuperModel are stored as List<String> via StringArrayJPAConverter.
     * The HSQLDB typically stores them as a single string like "<list/>" or "<null/>".
     */
    private List<String> parseTags(String tagsStr) {
        if (tagsStr == null || tagsStr.isEmpty() || "<null/>".equals(tagsStr) || "<list/>".equals(tagsStr)) {
            return new ArrayList<>();
        }
        // Try to parse as comma-separated or XML-like format
        // The StringArrayJPAConverter likely uses a specific format
        List<String> result = new ArrayList<>();
        if (tagsStr.contains(",")) {
            for (String tag : tagsStr.split(",")) {
                String trimmed = tag.trim();
                if (!trimmed.isEmpty()) {
                    result.add(trimmed);
                }
            }
        } else {
            result.add(tagsStr);
        }
        return result;
    }
}
