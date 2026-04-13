package ch.plaintext.schuetu.service.juriwagen.dto;

import java.util.List;

public record JuriwagenStatusDto(
        ZeitDto zeit,
        CountdownDto countdown,
        CountdownDto countdownToStart,
        List<SpielZeileDto> wartend,
        List<SpielZeileDto> zumVorbereiten,
        List<SpielZeileDto> vorbereitet,
        List<SpielZeileDto> spielend,
        List<SpielZeileDto> beendet,
        List<PenaltyDto> penalties,
        boolean readyToVorbereiten,
        boolean readyToSpielen,
        boolean abbrechenZulassen
) {
}
