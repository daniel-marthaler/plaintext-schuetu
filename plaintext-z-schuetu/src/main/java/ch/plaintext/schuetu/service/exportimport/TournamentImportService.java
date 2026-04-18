package ch.plaintext.schuetu.service.exportimport;

import ch.plaintext.schuetu.entity.*;
import ch.plaintext.schuetu.model.enums.GeschlechtEnum;
import ch.plaintext.schuetu.model.enums.PlatzEnum;
import ch.plaintext.schuetu.model.enums.SpielEnum;
import ch.plaintext.schuetu.model.enums.SpielTageszeit;
import ch.plaintext.schuetu.model.enums.SpielZeilenPhaseEnum;
import ch.plaintext.schuetu.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service fuer den Import eines vollstaendigen Turniers aus einem Export-DTO.
 * Erstellt alle Entitaeten mit neuen IDs und mappt die Beziehungen
 * ueber ID-Zuordnungstabellen.
 */
@Service
@Slf4j
public class TournamentImportService {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private MannschaftRepository mannschaftRepository;

    @Autowired
    private SchiriRepository schiriRepository;

    @Autowired
    private KategorieRepository kategorieRepository;

    @Autowired
    private GruppeRepository gruppeRepository;

    @Autowired
    private SpielRepository spielRepository;

    @Autowired
    private SpielZeilenRepository spielZeilenRepository;

    @Autowired
    private PenaltyRepository penaltyRepository;

    @Autowired
    private KorrekturRepository korrekturRepository;

    /**
     * Importiert ein vollstaendiges Turnier aus dem Export-DTO.
     * Alle Entitaeten werden mit neuen IDs erstellt und dem angegebenen
     * Spielnamen zugeordnet.
     *
     * @param dto         das Export-DTO mit allen Turnierdaten
     * @param newGameName der Name fuer das neue Spiel
     * @throws IllegalArgumentException wenn der Spielname bereits existiert
     */
    @Transactional
    public void importTournament(TournamentExportDto dto, String newGameName) {
        // Pruefen ob Spielname bereits existiert
        GameModel existing = gameRepository.findByGameName(newGameName);
        if (existing != null) {
            throw new IllegalArgumentException("Spiel existiert bereits: " + newGameName);
        }

        log.info("Starte Import von Turnier '{}' als '{}'", dto.getOriginalGameName(), newGameName);

        // ID-Zuordnungstabellen: alte ID -> neue ID
        Map<Long, Long> mannschaftIdMap = new HashMap<>();
        Map<Long, Long> schiriIdMap = new HashMap<>();
        Map<Long, Long> spielIdMap = new HashMap<>();
        Map<Long, Long> gruppeIdMap = new HashMap<>();
        Map<Long, Long> penaltyIdMap = new HashMap<>();
        Map<Long, Long> kategorieIdMap = new HashMap<>();

        // Zuordnungstabellen: alte ID -> neue Entitaet
        Map<Long, Mannschaft> mannschaftMap = new HashMap<>();
        Map<Long, Schiri> schiriMap = new HashMap<>();
        Map<Long, Spiel> spielMap = new HashMap<>();
        Map<Long, Gruppe> gruppeMap = new HashMap<>();
        Map<Long, Penalty> penaltyMap = new HashMap<>();

        // 1. GameModel erstellen
        GameModel gameModel = createGameModel(dto.getGameModel(), newGameName);
        gameModel = gameRepository.save(gameModel);
        log.info("GameModel erstellt mit ID {}", gameModel.getId());

        // 2. Mannschaften importieren (keine Abhaengigkeiten)
        for (TournamentExportDto.MannschaftDto mDto : dto.getMannschaften()) {
            Mannschaft mannschaft = createMannschaft(mDto, newGameName);
            mannschaft = mannschaftRepository.save(mannschaft);
            mannschaftIdMap.put(mDto.getOriginalId(), mannschaft.getId());
            mannschaftMap.put(mDto.getOriginalId(), mannschaft);
        }
        log.info("{} Mannschaften importiert", mannschaftIdMap.size());

        // 3. Schiris importieren (keine Abhaengigkeiten)
        for (TournamentExportDto.SchiriDto sDto : dto.getSchiris()) {
            Schiri schiri = createSchiri(sDto, newGameName);
            schiri = schiriRepository.save(schiri);
            schiriIdMap.put(sDto.getOriginalId(), schiri.getId());
            schiriMap.put(sDto.getOriginalId(), schiri);
        }
        log.info("{} Schiris importiert", schiriIdMap.size());

        // 4. Spiele importieren (abhaengig von Mannschaften und Schiris)
        for (TournamentExportDto.SpielDto spDto : dto.getSpiele()) {
            Spiel spiel = createSpiel(spDto, newGameName, mannschaftMap, schiriMap);
            spiel = spielRepository.save(spiel);
            spielIdMap.put(spDto.getOriginalId(), spiel.getId());
            spielMap.put(spDto.getOriginalId(), spiel);
        }
        log.info("{} Spiele importiert", spielIdMap.size());

        // 5. Gruppen importieren (abhaengig von Mannschaften und Spielen)
        for (TournamentExportDto.GruppeDto gDto : dto.getGruppen()) {
            Gruppe gruppe = createGruppe(gDto, newGameName, mannschaftMap, spielMap);
            gruppe = gruppeRepository.save(gruppe);
            gruppeIdMap.put(gDto.getOriginalId(), gruppe.getId());
            gruppeMap.put(gDto.getOriginalId(), gruppe);

            // Bidirektionale Beziehung: Mannschaft.gruppeA setzen
            // Gruppe.mannschaften ist eine unidirektionale @OneToMany (Join-Table),
            // aber Mannschaft.gruppeA ist ein separates @ManyToOne-Feld.
            // Ohne dieses Setzen liefert mannschaft.getGruppe() null und
            // die Spielematrix bleibt leer.
            if (gruppe.getMannschaften() != null) {
                for (Mannschaft m : gruppe.getMannschaften()) {
                    if (m.getGruppe() == null) {
                        m.setGruppe(gruppe);
                        mannschaftRepository.save(m);
                    }
                }
            }
        }
        log.info("{} Gruppen importiert", gruppeIdMap.size());

        // 6. Penalties importieren (abhaengig von Gruppen und Mannschaften)
        for (TournamentExportDto.PenaltyDto pDto : dto.getPenalties()) {
            Penalty penalty = createPenalty(pDto, newGameName, gruppeMap, mannschaftMap);
            penalty = penaltyRepository.save(penalty);
            penaltyIdMap.put(pDto.getOriginalId(), penalty.getId());
            penaltyMap.put(pDto.getOriginalId(), penalty);
        }
        log.info("{} Penalties importiert", penaltyIdMap.size());

        // 7. Kategorien importieren (abhaengig von Gruppen, Spiele, Penalties)
        for (TournamentExportDto.KategorieDto kDto : dto.getKategorien()) {
            Kategorie kategorie = createKategorie(kDto, newGameName, gruppeMap, spielMap, penaltyMap);
            kategorie = kategorieRepository.save(kategorie);
            kategorieIdMap.put(kDto.getOriginalId(), kategorie.getId());
        }
        log.info("{} Kategorien importiert", kategorieIdMap.size());

        // Gruppen mit Kategorie-Referenzen aktualisieren
        updateGruppenKategorien(dto.getGruppen(), gruppeMap, kategorieIdMap, dto.getKategorien());

        // 8. Spielzeilen importieren (abhaengig von Spielen)
        int spielzeilenCount = 0;
        for (TournamentExportDto.SpielZeileDto szDto : dto.getSpielzeilen()) {
            SpielZeile spielZeile = createSpielZeile(szDto, newGameName, spielMap);
            spielZeilenRepository.save(spielZeile);
            spielzeilenCount++;
        }
        log.info("{} Spielzeilen importiert", spielzeilenCount);

        // 9. Korrekturen importieren (keine Abhaengigkeiten)
        int korrekturenCount = 0;
        for (TournamentExportDto.KorrekturDto koDto : dto.getKorrekturen()) {
            Korrektur korrektur = createKorrektur(koDto, newGameName);
            korrekturRepository.save(korrektur);
            korrekturenCount++;
        }
        log.info("{} Korrekturen importiert", korrekturenCount);

        log.info("Import von Turnier '{}' als '{}' abgeschlossen", dto.getOriginalGameName(), newGameName);
    }

    private GameModel createGameModel(TournamentExportDto.GameModelDto dto, String newGameName) {
        GameModel gm = new GameModel();
        gm.setId(null);
        gm.setGameName(newGameName);
        gm.setCreationdate(new Date());
        gm.setSpielPhase(dto.getSpielPhase());
        gm.setSpiellaenge(dto.getSpiellaenge());
        gm.setSpiellaengefinale(dto.getSpiellaengefinale());
        gm.setPause(dto.getPause());
        gm.setStart(dto.getStart());
        gm.setWebsiteFixString(dto.getWebsiteFixString());
        gm.setWebsiteTurnierTitel(dto.getWebsiteTurnierTitel());
        gm.setWebsiteInMannschaftslistenmode(dto.isWebsiteInMannschaftslistenmode());
        gm.setWebsiteEnableDownloadLink(dto.isWebsiteEnableDownloadLink());
        gm.setAutomatischesAufholen(dto.isAutomatischesAufholen());
        gm.setAutomatischesVorbereiten(dto.isAutomatischesVorbereiten());
        gm.setAutomatischesAnsagen(dto.isAutomatischesAnsagen());
        gm.setGongEinschalten(dto.isGongEinschalten());
        gm.setAbbrechenZulassen(dto.isAbbrechenZulassen());
        gm.setVerschnellerungsFaktor(dto.getVerschnellerungsFaktor());
        gm.setAufholzeitInSekunden(dto.getAufholzeitInSekunden());
        gm.setGroesser6AufC(dto.isGroesser6AufC());
        gm.setGroesser6AufCMin(dto.getGroesser6AufCMin());
        gm.setZweiPausenBisKlasse(dto.getZweiPausenBisKlasse());
        gm.setMobileLinkOn(dto.isMobileLinkOn());
        gm.setMobileLink(dto.getMobileLink());
        gm.setBackportSync(dto.getBackportSync());
        gm.setBackportSyncOn(dto.isBackportSyncOn());
        gm.setUploadOn(dto.isUploadOn());
        gm.setBehandleFinaleProKlassebeiZusammengefuehrten(dto.isBehandleFinaleProKlassebeiZusammengefuehrten());
        gm.setInitialisiert(dto.getInitialisiert());
        return gm;
    }

    private Mannschaft createMannschaft(TournamentExportDto.MannschaftDto dto, String newGameName) {
        Mannschaft m = new Mannschaft();
        m.setId(null);
        m.setGame(newGameName);
        m.setCreationdate(new Date());
        m.setNickname(dto.getNickname());
        m.setTeamNummer(dto.getTeamNummer());
        m.setKlasse(dto.getKlasse());
        m.setGeschlecht(dto.getGeschlecht() != null ? GeschlechtEnum.valueOf(dto.getGeschlecht()) : GeschlechtEnum.M);
        m.setCaptainName(dto.getCaptainName());
        m.setCaptainStrasse(dto.getCaptainStrasse());
        m.setCaptainPLZOrt(dto.getCaptainPLZOrt());
        m.setCaptainTelefon(dto.getCaptainTelefon());
        m.setCaptainEmail(dto.getCaptainEmail());
        m.setCaptain2Name(dto.getCaptain2Name());
        m.setBegleitpersonName(dto.getBegleitpersonName());
        m.setBegleitpersonStrasse(dto.getBegleitpersonStrasse());
        m.setBegleitpersonPLZOrt(dto.getBegleitpersonPLZOrt());
        m.setBegleitpersonTelefon(dto.getBegleitpersonTelefon());
        m.setBegleitpersonEmail(dto.getBegleitpersonEmail());
        m.setBegleitperson2Name(dto.getBegleitperson2Name());
        m.setSchulhaus(dto.getSchulhaus());
        m.setFarbe(dto.getFarbe());
        m.setColor(dto.getColor());
        m.setAnzahlSpieler(dto.getAnzahlSpieler());
        m.setSpielJahr(dto.getSpielJahr());
        m.setNotizen(dto.getNotizen());
        m.setEsr(dto.getEsr());
        m.setDisqualifiziert(dto.getDisqualifiziert());
        m.setKlassenBezeichnung(dto.getKlassenBezeichnung());
        m.setSpielWunschHint(dto.getSpielWunschHint());
        m.setGr(dto.getGr());
        return m;
    }

    private Schiri createSchiri(TournamentExportDto.SchiriDto dto, String newGameName) {
        Schiri s = new Schiri();
        s.setId(null);
        s.setGame(newGameName);
        s.setCreationdate(new Date());
        s.setVorname(dto.getVorname());
        s.setNachname(dto.getNachname());
        s.setName(dto.getName());
        s.setTelefon(dto.getTelefon());
        s.setEinteilung(dto.getEinteilung());
        s.setAktiviert(dto.isAktiviert());
        s.setMatchcount(dto.getMatchcount());
        s.setSpielIDs(dto.getSpielIDs());
        s.setPasswordHash(dto.getPasswordHash());
        s.setLoginName(dto.getLoginName());
        return s;
    }

    private Spiel createSpiel(TournamentExportDto.SpielDto dto, String newGameName,
                              Map<Long, Mannschaft> mannschaftMap, Map<Long, Schiri> schiriMap) {
        Spiel s = new Spiel();
        s.setId(null);
        s.setGame(newGameName);
        s.setCreationdate(new Date());
        s.setTyp(dto.getTyp() != null ? SpielEnum.valueOf(dto.getTyp()) : SpielEnum.GRUPPE);
        s.setMannschaftA(dto.getMannschaftAId() != null ? mannschaftMap.get(dto.getMannschaftAId()) : null);
        s.setMannschaftB(dto.getMannschaftBId() != null ? mannschaftMap.get(dto.getMannschaftBId()) : null);
        s.setSchiri(dto.getSchiriId() != null ? schiriMap.get(dto.getSchiriId()) : null);
        s.setToreA(dto.getToreA());
        s.setToreB(dto.getToreB());
        s.setToreABestaetigt(dto.getToreABestaetigt());
        s.setToreBBestaetigt(dto.getToreBBestaetigt());
        s.setFertigGespielt(dto.isFertigGespielt());
        s.setFertigEingetragen(dto.isFertigEingetragen());
        s.setFertigBestaetigt(dto.isFertigBestaetigt());
        s.setZurueckgewiesen(dto.isZurueckgewiesen());
        s.setAmSpielen(dto.isAmSpielen());
        s.setPlatz(dto.getPlatz() != null ? PlatzEnum.valueOf(dto.getPlatz()) : null);
        s.setStart(dto.getStart());
        s.setIdString(dto.getIdString());
        s.setKategorieName(dto.getKategorieName());
        s.setKlasse(dto.getKlasse());
        s.setEintrager(dto.getEintrager());
        s.setSchiriName(dto.getSchiriName());
        s.setKontrolle(dto.getKontrolle());
        s.setRealName(dto.getRealName());
        s.setNotizen(dto.getNotizen());
        s.setChangedGrossToKlein(dto.getChangedGrossToKlein());
        s.setSpielZeilenPhase(dto.getSpielZeilenPhase() != null ? SpielZeilenPhaseEnum.valueOf(dto.getSpielZeilenPhase()) : SpielZeilenPhaseEnum.A_ANSTEHEND);
        return s;
    }

    private Gruppe createGruppe(TournamentExportDto.GruppeDto dto, String newGameName,
                                Map<Long, Mannschaft> mannschaftMap, Map<Long, Spiel> spielMap) {
        Gruppe g = new Gruppe();
        g.setId(null);
        g.setGame(newGameName);
        g.setCreationdate(new Date());
        g.setGeschlecht(dto.getGeschlecht() != null ? GeschlechtEnum.valueOf(dto.getGeschlecht()) : null);

        // Mannschaften zuordnen
        if (dto.getMannschaftIds() != null) {
            List<Mannschaft> mannschaften = dto.getMannschaftIds().stream()
                    .map(mannschaftMap::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            g.setMannschaften(mannschaften);
        }

        // Spiele zuordnen
        if (dto.getSpielIds() != null) {
            List<Spiel> spiele = dto.getSpielIds().stream()
                    .map(spielMap::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            g.setSpiele(spiele);
        }

        return g;
    }

    private Penalty createPenalty(TournamentExportDto.PenaltyDto dto, String newGameName,
                                  Map<Long, Gruppe> gruppeMap, Map<Long, Mannschaft> mannschaftMap) {
        Penalty p = new Penalty();
        p.setId(null);
        p.setGame(newGameName);
        p.setCreationdate(new Date());
        p.setGruppe(dto.getGruppeId() != null ? gruppeMap.get(dto.getGruppeId()) : null);
        p.setReihenfolgeOrig(dto.getReihenfolgeOrig());
        p.setReihenfolge(dto.getReihenfolge());
        p.setGespielt(dto.isGespielt());
        p.setBestaetigt(dto.isBestaetigt());
        p.setIdString(dto.getIdString());

        // FinalList direkt ueber das interne Feld setzen (nicht ueber setFinalList, das die Reihenfolge neu berechnet)
        if (dto.getFinalListIds() != null) {
            List<Mannschaft> finalList = dto.getFinalListIds().stream()
                    .map(mannschaftMap::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            p.getRealFinalList().addAll(finalList);
        }

        return p;
    }

    private Kategorie createKategorie(TournamentExportDto.KategorieDto dto, String newGameName,
                                       Map<Long, Gruppe> gruppeMap, Map<Long, Spiel> spielMap,
                                       Map<Long, Penalty> penaltyMap) {
        Kategorie k = new Kategorie();
        k.setId(null);
        k.setGame(newGameName);
        k.setCreationdate(new Date());
        k.setGruppeA(dto.getGruppeAId() != null ? gruppeMap.get(dto.getGruppeAId()) : null);
        k.setGruppeB(dto.getGruppeBId() != null ? gruppeMap.get(dto.getGruppeBId()) : null);
        k.setKleineFinal(dto.getKleineFinalId() != null ? spielMap.get(dto.getKleineFinalId()) : null);
        k.setGrosserFinal(dto.getGrosserFinalId() != null ? spielMap.get(dto.getGrosserFinalId()) : null);
        k.setGrosserfinal2(dto.getGrosserfinal2Id() != null ? spielMap.get(dto.getGrosserfinal2Id()) : null);
        k.setPenaltyA(dto.getPenaltyAId() != null ? penaltyMap.get(dto.getPenaltyAId()) : null);
        k.setPenaltyB(dto.getPenaltyBId() != null ? penaltyMap.get(dto.getPenaltyBId()) : null);
        k.setEintrager(dto.getEintrager());
        k.setNotitzen(dto.getNotitzen());
        k.setSpielwunsch(dto.getSpielwunsch() != null ? SpielTageszeit.valueOf(dto.getSpielwunsch()) : SpielTageszeit.EGAL);
        return k;
    }

    /**
     * Aktualisiert die Kategorie-Referenzen in den Gruppen,
     * nachdem die Kategorien erstellt wurden.
     */
    private void updateGruppenKategorien(List<TournamentExportDto.GruppeDto> gruppeDtos,
                                          Map<Long, Gruppe> gruppeMap,
                                          Map<Long, Long> kategorieIdMap,
                                          List<TournamentExportDto.KategorieDto> kategorieDtos) {
        // Erstelle Zuordnung: alte GruppeId -> alte KategorieId
        Map<Long, Long> gruppeToKategorie = new HashMap<>();
        for (TournamentExportDto.KategorieDto kDto : kategorieDtos) {
            if (kDto.getGruppeAId() != null) {
                gruppeToKategorie.put(kDto.getGruppeAId(), kDto.getOriginalId());
            }
            if (kDto.getGruppeBId() != null) {
                gruppeToKategorie.put(kDto.getGruppeBId(), kDto.getOriginalId());
            }
        }

        // Setze Kategorie-Referenzen in den neuen Gruppen
        for (TournamentExportDto.GruppeDto gDto : gruppeDtos) {
            Gruppe gruppe = gruppeMap.get(gDto.getOriginalId());
            if (gruppe != null) {
                Long alteKategorieId = gruppeToKategorie.get(gDto.getOriginalId());
                if (alteKategorieId != null) {
                    Long neueKategorieId = kategorieIdMap.get(alteKategorieId);
                    if (neueKategorieId != null) {
                        kategorieRepository.findById(neueKategorieId).ifPresent(gruppe::setKategorie);
                        gruppeRepository.save(gruppe);
                    }
                }
            }
        }
    }

    private SpielZeile createSpielZeile(TournamentExportDto.SpielZeileDto dto, String newGameName,
                                         Map<Long, Spiel> spielMap) {
        SpielZeile sz = new SpielZeile();
        sz.setId(null);
        sz.setGame(newGameName);
        sz.setCreationdate(new Date());
        // Direkt auf Felder setzen - die Setter der SpielZeile sind normal
        sz.setA(dto.getAId() != null ? spielMap.get(dto.getAId()) : null);
        sz.setB(dto.getBId() != null ? spielMap.get(dto.getBId()) : null);
        sz.setC(dto.getCId() != null ? spielMap.get(dto.getCId()) : null);
        sz.setD(dto.getDId() != null ? spielMap.get(dto.getDId()) : null);
        sz.setStart(dto.getStart());
        sz.setSonntag(dto.isSonntag());
        sz.setPause(dto.isPause());
        sz.setFinale(dto.isFinale());
        sz.setPhase(dto.getPhase() != null ? SpielZeilenPhaseEnum.valueOf(dto.getPhase()) : SpielZeilenPhaseEnum.A_ANSTEHEND);
        sz.setGuid(dto.getGuid());
        sz.setGId(dto.getGId());
        return sz;
    }

    private Korrektur createKorrektur(TournamentExportDto.KorrekturDto dto, String newGameName) {
        Korrektur k = new Korrektur();
        k.setId(null);
        k.setGame(newGameName);
        k.setCreationdate(new Date());
        k.setTyp(dto.getTyp());
        k.setWert(dto.getWert());
        k.setReihenfolge(dto.getReihenfolge());
        return k;
    }
}
