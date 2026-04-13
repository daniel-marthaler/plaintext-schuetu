package ch.plaintext.schuetu.service.juriwagen;

import ch.plaintext.schuetu.entity.Penalty;
import ch.plaintext.schuetu.entity.Spiel;
import ch.plaintext.schuetu.entity.SpielZeile;
import ch.plaintext.schuetu.service.Game;
import ch.plaintext.schuetu.service.GameRoot;
import ch.plaintext.schuetu.service.juriwagen.dto.*;
import ch.plaintext.schuetu.service.zeit.Countdown;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class JuriwagenService {

    private final GameRoot gameRoot;

    public Game getGame(String gameName) {
        return gameRoot.selectGame(gameName);
    }

    public JuriwagenStatusDto getStatus(String gameName) {
        Game game = getGame(gameName);
        if (game == null) {
            return null;
        }

        var durchfuehrung = game.getDurchfuehrung();
        var zeit = game.getZeit();

        return new JuriwagenStatusDto(
                toZeitDto(game),
                toCountdownDto(durchfuehrung.getCountdown()),
                toCountdownDto(durchfuehrung.getCountdownToStart()),
                toSpielZeileDtoList(durchfuehrung.getList1Wartend()),
                toSpielZeileDtoList(durchfuehrung.getList2ZumVorbereiten()),
                toSpielZeileDtoList(durchfuehrung.getList3Vorbereitet()),
                toSpielZeileDtoList(durchfuehrung.getList4Spielend()),
                toSpielZeileDtoList(durchfuehrung.getList5Beendet()),
                toPenaltyDtoList(durchfuehrung.getPenaltyAnstehend()),
                durchfuehrung.getReadyToVorbereiten(),
                durchfuehrung.getReadyToSpielen(),
                game.getModel().isAbbrechenZulassen()
        );
    }

    public ZeitDto getZeit(String gameName) {
        Game game = getGame(gameName);
        if (game == null) {
            return null;
        }
        return toZeitDto(game);
    }

    public void vorbereitet(String gameName) {
        Game game = getGame(gameName);
        if (game != null) {
            game.getDurchfuehrung().vorbereitet();
        }
    }

    public void spielen(String gameName) {
        Game game = getGame(gameName);
        if (game != null) {
            game.getDurchfuehrung().spielen();
        }
    }

    public void beenden(String gameName) {
        Game game = getGame(gameName);
        if (game != null) {
            game.getDurchfuehrung().beenden();
        }
    }

    public void enter(String gameName) {
        Game game = getGame(gameName);
        if (game != null) {
            game.getDurchfuehrung().enter();
        }
    }

    public void penaltyGespielt(String gameName, String penaltyId) {
        Game game = getGame(gameName);
        if (game != null) {
            game.getDurchfuehrung().setPenaltyGespielt(penaltyId);
        }
    }

    public void zeitAdd(String gameName, int seconds) {
        Game game = getGame(gameName);
        if (game != null) {
            game.getZeit().setAdd(seconds);
            game.getZeit().add();
        }
    }

    public void zeitEinholen60(String gameName) {
        Game game = getGame(gameName);
        if (game != null) {
            game.spielzeitEinholen60();
        }
    }

    private ZeitDto toZeitDto(Game game) {
        var zeit = game.getZeit();
        return new ZeitDto(
                zeit.getRichtigeZeit(),
                zeit.getSpielZeit(),
                zeit.getVerspaetung(),
                zeit.spielzeitVerspaetung()
        );
    }

    private CountdownDto toCountdownDto(Countdown countdown) {
        if (countdown == null) {
            return new CountdownDto(0, "00:00", true);
        }
        return new CountdownDto(countdown.getSekundenToGo(), countdown.getZeit(), countdown.isFertig());
    }

    private List<SpielZeileDto> toSpielZeileDtoList(List<SpielZeile> zeilen) {
        if (zeilen == null) {
            return Collections.emptyList();
        }
        return zeilen.stream().map(this::toSpielZeileDto).toList();
    }

    private SpielZeileDto toSpielZeileDto(SpielZeile zeile) {
        SimpleDateFormat fmt = new SimpleDateFormat("HH:mm");
        String startStr = zeile.getStart() != null ? fmt.format(zeile.getStart()) : "";
        return new SpielZeileDto(
                startStr,
                toSpielDto(zeile.getA()),
                toSpielDto(zeile.getB()),
                toSpielDto(zeile.getC()),
                toSpielDto(zeile.getD())
        );
    }

    private SpielDto toSpielDto(Spiel spiel) {
        if (spiel == null || spiel.isPlatzhalter()) {
            return new SpielDto(null, "-", "-", "", "white", true);
        }
        return new SpielDto(
                spiel.getId(),
                spiel.getMannschaftAName(),
                spiel.getMannschaftBName(),
                spiel.getKategorieName() != null ? spiel.getKategorieName() : "",
                spiel.getFarbe(),
                false
        );
    }

    private List<PenaltyDto> toPenaltyDtoList(List<Penalty> penalties) {
        if (penalties == null) {
            return Collections.emptyList();
        }
        return penalties.stream().map(this::toPenaltyDto).toList();
    }

    private PenaltyDto toPenaltyDto(Penalty penalty) {
        String kategorie = penalty.getKategorie() != null ? penalty.getKategorie().getName() : "";
        return new PenaltyDto(penalty.getIdString(), penalty.toMannschaftsString(), kategorie);
    }
}
