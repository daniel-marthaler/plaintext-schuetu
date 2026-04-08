package ch.plaintext.schuetu.service.exportimport;

import ch.plaintext.schuetu.entity.*;
import ch.plaintext.schuetu.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service fuer den Export eines vollstaendigen Turniers als DTO.
 * Laedt alle Entitaeten eines Spiels und mappt sie auf flache DTOs
 * mit ID-Referenzen statt JPA-Beziehungen.
 */
@Service
@Slf4j
public class TournamentExportService {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private MannschaftRepository mannschaftRepository;

    @Autowired
    private SchiriRepository schiriRepository;

    @Autowired
    private KategorieRepository kategorieRepository;

    @Autowired
    private SpielRepository spielRepository;

    @Autowired
    private SpielZeilenRepository spielZeilenRepository;

    @Autowired
    private PenaltyRepository penaltyRepository;

    @Autowired
    private KorrekturRepository korrekturRepository;

    /**
     * Exportiert ein vollstaendiges Turnier als DTO.
     *
     * @param gameName der Name des Spiels
     * @return das Export-DTO mit allen Turnierdaten
     * @throws IllegalArgumentException wenn das Spiel nicht gefunden wird
     */
    @Transactional(readOnly = true)
    public TournamentExportDto exportTournament(String gameName) {
        GameModel gameModel = gameRepository.findByGameName(gameName);
        if (gameModel == null) {
            throw new IllegalArgumentException("Spiel nicht gefunden: " + gameName);
        }

        TournamentExportDto dto = new TournamentExportDto();
        dto.setExportVersion("1.0");
        dto.setExportDate(Instant.now().toString());
        dto.setOriginalGameName(gameName);

        // GameModel
        dto.setGameModel(mapGameModel(gameModel));

        // Mannschaften
        List<Mannschaft> mannschaften = mannschaftRepository.findByGame(gameName);
        dto.setMannschaften(mannschaften.stream()
                .map(this::mapMannschaft)
                .collect(Collectors.toList()));

        // Schiris
        List<Schiri> schiris = schiriRepository.findByGame(gameName);
        dto.setSchiris(schiris.stream()
                .map(this::mapSchiri)
                .collect(Collectors.toList()));

        // Kategorien und zugehoerige Gruppen/Penalties
        List<Kategorie> kategorien = kategorieRepository.findByGame(gameName);
        Set<Gruppe> alleGruppen = new LinkedHashSet<>();
        Set<Penalty> allePenalties = new LinkedHashSet<>();

        for (Kategorie kategorie : kategorien) {
            if (kategorie.getGruppeA() != null) {
                alleGruppen.add(kategorie.getGruppeA());
            }
            if (kategorie.getGruppeB() != null) {
                alleGruppen.add(kategorie.getGruppeB());
            }
            if (kategorie.getPenaltyA() != null) {
                allePenalties.add(kategorie.getPenaltyA());
            }
            if (kategorie.getPenaltyB() != null) {
                allePenalties.add(kategorie.getPenaltyB());
            }
        }

        // Auch Penalties ueber Repository laden (fuer solche ohne Kategorie-Zuordnung)
        List<Penalty> penaltiesFromRepo = penaltyRepository.findByGame(gameName);
        allePenalties.addAll(penaltiesFromRepo);

        dto.setKategorien(kategorien.stream()
                .map(this::mapKategorie)
                .collect(Collectors.toList()));

        dto.setGruppen(alleGruppen.stream()
                .map(this::mapGruppe)
                .collect(Collectors.toList()));

        // Spiele
        List<Spiel> spiele = spielRepository.findByGame(gameName);
        dto.setSpiele(spiele.stream()
                .map(this::mapSpiel)
                .collect(Collectors.toList()));

        // Spielzeilen
        List<SpielZeile> spielzeilen = spielZeilenRepository.findByGame(gameName);
        dto.setSpielzeilen(spielzeilen.stream()
                .map(this::mapSpielZeile)
                .collect(Collectors.toList()));

        // Penalties
        dto.setPenalties(allePenalties.stream()
                .map(this::mapPenalty)
                .collect(Collectors.toList()));

        // Korrekturen
        List<Korrektur> korrekturen = korrekturRepository.findByGame(gameName);
        dto.setKorrekturen(korrekturen.stream()
                .map(this::mapKorrektur)
                .collect(Collectors.toList()));

        log.info("Turnier '{}' exportiert: {} Mannschaften, {} Schiris, {} Kategorien, {} Gruppen, {} Spiele, {} Spielzeilen, {} Penalties, {} Korrekturen",
                gameName,
                dto.getMannschaften().size(),
                dto.getSchiris().size(),
                dto.getKategorien().size(),
                dto.getGruppen().size(),
                dto.getSpiele().size(),
                dto.getSpielzeilen().size(),
                dto.getPenalties().size(),
                dto.getKorrekturen().size());

        return dto;
    }

    private TournamentExportDto.GameModelDto mapGameModel(GameModel gm) {
        TournamentExportDto.GameModelDto dto = new TournamentExportDto.GameModelDto();
        dto.setOriginalId(gm.getId());
        dto.setGameName(gm.getGameName());
        dto.setSpielPhase(gm.getSpielPhase());
        dto.setSpiellaenge(gm.getSpiellaenge());
        dto.setSpiellaengefinale(gm.getSpiellaengefinale());
        dto.setPause(gm.getPause());
        dto.setStart(gm.getStart());
        dto.setWebsiteFixString(gm.getWebsiteFixString());
        dto.setWebsiteTurnierTitel(gm.getWebsiteTurnierTitel());
        dto.setWebsiteInMannschaftslistenmode(gm.isWebsiteInMannschaftslistenmode());
        dto.setWebsiteEnableDownloadLink(gm.isWebsiteEnableDownloadLink());
        dto.setAutomatischesAufholen(gm.isAutomatischesAufholen());
        dto.setAutomatischesVorbereiten(gm.isAutomatischesVorbereiten());
        dto.setAutomatischesAnsagen(gm.isAutomatischesAnsagen());
        dto.setGongEinschalten(gm.isGongEinschalten());
        dto.setAbbrechenZulassen(gm.isAbbrechenZulassen());
        dto.setVerschnellerungsFaktor(gm.getVerschnellerungsFaktor());
        dto.setAufholzeitInSekunden(gm.getAufholzeitInSekunden());
        dto.setGroesser6AufC(gm.isGroesser6AufC());
        dto.setGroesser6AufCMin(gm.getGroesser6AufCMin());
        dto.setZweiPausenBisKlasse(gm.getZweiPausenBisKlasse());
        dto.setMobileLinkOn(gm.isMobileLinkOn());
        dto.setMobileLink(gm.getMobileLink());
        dto.setBackportSync(gm.getBackportSync());
        dto.setBackportSyncOn(gm.isBackportSyncOn());
        dto.setUploadOn(gm.isUploadOn());
        dto.setBehandleFinaleProKlassebeiZusammengefuehrten(gm.isBehandleFinaleProKlassebeiZusammengefuehrten());
        dto.setInitialisiert(gm.getInitialisiert());
        return dto;
    }

    private TournamentExportDto.MannschaftDto mapMannschaft(Mannschaft m) {
        TournamentExportDto.MannschaftDto dto = new TournamentExportDto.MannschaftDto();
        dto.setOriginalId(m.getId());
        dto.setNickname(m.getNickname());
        dto.setTeamNummer(m.getTeamNummer());
        dto.setKlasse(m.getKlasse());
        dto.setGeschlecht(m.getGeschlecht() != null ? m.getGeschlecht().name() : null);
        dto.setCaptainName(m.getCaptainName());
        dto.setCaptainStrasse(m.getCaptainStrasse());
        dto.setCaptainPLZOrt(m.getCaptainPLZOrt());
        dto.setCaptainTelefon(m.getCaptainTelefon());
        dto.setCaptainEmail(m.getCaptainEmail());
        dto.setCaptain2Name(m.getCaptain2Name());
        dto.setBegleitpersonName(m.getBegleitpersonName());
        dto.setBegleitpersonStrasse(m.getBegleitpersonStrasse());
        dto.setBegleitpersonPLZOrt(m.getBegleitpersonPLZOrt());
        dto.setBegleitpersonTelefon(m.getBegleitpersonTelefon());
        dto.setBegleitpersonEmail(m.getBegleitpersonEmail());
        dto.setBegleitperson2Name(m.getBegleitperson2Name());
        dto.setSchulhaus(m.getSchulhaus());
        dto.setFarbe(m.getFarbe());
        dto.setColor(m.getColor());
        dto.setAnzahlSpieler(m.getAnzahlSpieler());
        dto.setSpielJahr(m.getSpielJahr());
        dto.setNotizen(m.getNotizen());
        dto.setEsr(m.getEsr());
        dto.setDisqualifiziert(m.getDisqualifiziert());
        dto.setKlassenBezeichnung(m.getKlassenBezeichnung());
        dto.setSpielWunschHint(m.getSpielWunschHint());
        dto.setGr(m.getGr());
        return dto;
    }

    private TournamentExportDto.SchiriDto mapSchiri(Schiri s) {
        TournamentExportDto.SchiriDto dto = new TournamentExportDto.SchiriDto();
        dto.setOriginalId(s.getId());
        dto.setVorname(s.getVorname());
        dto.setNachname(s.getNachname());
        dto.setName(s.getName());
        dto.setTelefon(s.getTelefon());
        dto.setEinteilung(s.getEinteilung());
        dto.setAktiviert(s.isAktiviert());
        dto.setMatchcount(s.getMatchcount());
        dto.setSpielIDs(s.getSpielIDs());
        dto.setPasswordHash(s.getPasswordHash());
        dto.setLoginName(s.getLoginName());
        return dto;
    }

    private TournamentExportDto.KategorieDto mapKategorie(Kategorie k) {
        TournamentExportDto.KategorieDto dto = new TournamentExportDto.KategorieDto();
        dto.setOriginalId(k.getId());
        dto.setGruppeAId(k.getGruppeA() != null ? k.getGruppeA().getId() : null);
        dto.setGruppeBId(k.getGruppeB() != null ? k.getGruppeB().getId() : null);
        dto.setKleineFinalId(k.getKleineFinal() != null ? k.getKleineFinal().getId() : null);
        dto.setGrosserFinalId(k.getGrosserFinal() != null ? k.getGrosserFinal().getId() : null);
        dto.setGrosserfinal2Id(k.getGrosserfinal2() != null ? k.getGrosserfinal2().getId() : null);
        dto.setPenaltyAId(k.getPenaltyA() != null ? k.getPenaltyA().getId() : null);
        dto.setPenaltyBId(k.getPenaltyB() != null ? k.getPenaltyB().getId() : null);
        dto.setEintrager(k.getEintrager());
        dto.setNotitzen(k.getNotitzen());
        dto.setSpielwunsch(k.getSpielwunsch() != null ? k.getSpielwunsch().name() : null);
        return dto;
    }

    private TournamentExportDto.GruppeDto mapGruppe(Gruppe g) {
        TournamentExportDto.GruppeDto dto = new TournamentExportDto.GruppeDto();
        dto.setOriginalId(g.getId());
        dto.setKategorieId(g.getKategorie() != null ? g.getKategorie().getId() : null);
        dto.setGeschlecht(g.getGeschlecht() != null ? g.getGeschlecht().name() : null);

        if (g.getMannschaften() != null) {
            dto.setMannschaftIds(g.getMannschaften().stream()
                    .filter(Objects::nonNull)
                    .map(Mannschaft::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()));
        }

        if (g.getSpiele() != null) {
            dto.setSpielIds(g.getSpiele().stream()
                    .filter(Objects::nonNull)
                    .map(Spiel::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    private TournamentExportDto.SpielDto mapSpiel(Spiel s) {
        TournamentExportDto.SpielDto dto = new TournamentExportDto.SpielDto();
        dto.setOriginalId(s.getId());
        dto.setTyp(s.getTyp() != null ? s.getTyp().name() : null);
        dto.setMannschaftAId(s.getMannschaftA() != null ? s.getMannschaftA().getId() : null);
        dto.setMannschaftBId(s.getMannschaftB() != null ? s.getMannschaftB().getId() : null);
        dto.setSchiriId(s.getSchiri() != null ? s.getSchiri().getId() : null);
        dto.setToreA(s.getToreA());
        dto.setToreB(s.getToreB());
        dto.setToreABestaetigt(s.getToreABestaetigt());
        dto.setToreBBestaetigt(s.getToreBBestaetigt());
        dto.setFertigGespielt(s.getFertiggespielt());
        dto.setFertigEingetragen(s.isFertigEingetragen());
        dto.setFertigBestaetigt(s.isFertigBestaetigt());
        dto.setZurueckgewiesen(s.isZurueckgewiesen());
        dto.setAmSpielen(s.isAmSpielen());
        dto.setPlatz(s.getPlatz() != null ? s.getPlatz().name() : null);
        dto.setStart(s.getStart());
        dto.setIdString(s.getIdString());
        dto.setKategorieName(s.getKategorieName());
        dto.setKlasse(s.getKlasse());
        dto.setEintrager(s.getEintrager());
        dto.setSchiriName(s.getSchiriName());
        dto.setKontrolle(s.getKontrolle());
        dto.setRealName(s.getRealName());
        dto.setNotizen(s.getNotizen());
        dto.setChangedGrossToKlein(s.getChangedGrossToKlein());
        dto.setSpielZeilenPhase(s.getSpielZeilenPhase() != null ? s.getSpielZeilenPhase().name() : null);
        return dto;
    }

    private TournamentExportDto.SpielZeileDto mapSpielZeile(SpielZeile sz) {
        TournamentExportDto.SpielZeileDto dto = new TournamentExportDto.SpielZeileDto();
        dto.setOriginalId(sz.getId());
        // Direkt auf die Felder zugreifen, nicht ueber Getter (Getter erzeugt Platzhalter-Spiel)
        dto.setAId(getSpielIdDirect(sz, "a"));
        dto.setBId(getSpielIdDirect(sz, "b"));
        dto.setCId(getSpielIdDirect(sz, "c"));
        dto.setDId(getSpielIdDirect(sz, "d"));
        dto.setStart(sz.getStart());
        dto.setSonntag(sz.isSonntag());
        dto.setPause(sz.isPause());
        dto.setFinale(sz.isFinale());
        dto.setPhase(sz.getPhase() != null ? sz.getPhase().name() : null);
        dto.setGuid(sz.getGuid());
        dto.setGId(sz.getGId());
        return dto;
    }

    /**
     * Zugriff auf die Spiel-Felder der SpielZeile ueber Reflection,
     * da die Getter Platzhalter-Spiele erzeugen wenn das Feld null ist.
     */
    private Long getSpielIdDirect(SpielZeile sz, String fieldName) {
        try {
            java.lang.reflect.Field field = SpielZeile.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            Spiel spiel = (Spiel) field.get(sz);
            return spiel != null ? spiel.getId() : null;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            log.warn("Konnte Feld '{}' nicht lesen: {}", fieldName, e.getMessage());
            return null;
        }
    }

    private TournamentExportDto.PenaltyDto mapPenalty(Penalty p) {
        TournamentExportDto.PenaltyDto dto = new TournamentExportDto.PenaltyDto();
        dto.setOriginalId(p.getId());
        dto.setGruppeId(p.getGruppe() != null ? p.getGruppe().getId() : null);
        dto.setReihenfolgeOrig(p.getReihenfolgeOrig());
        dto.setReihenfolge(p.getReihenfolge());
        dto.setGespielt(p.isGespielt());
        dto.setBestaetigt(p.isBestaetigt());
        dto.setIdString(p.getIdString());

        if (p.getRealFinalList() != null) {
            dto.setFinalListIds(p.getRealFinalList().stream()
                    .filter(Objects::nonNull)
                    .map(Mannschaft::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    private TournamentExportDto.KorrekturDto mapKorrektur(Korrektur k) {
        TournamentExportDto.KorrekturDto dto = new TournamentExportDto.KorrekturDto();
        dto.setOriginalId(k.getId());
        dto.setTyp(k.getTyp());
        dto.setWert(k.getWert());
        dto.setReihenfolge(k.getReihenfolge());
        return dto;
    }
}
