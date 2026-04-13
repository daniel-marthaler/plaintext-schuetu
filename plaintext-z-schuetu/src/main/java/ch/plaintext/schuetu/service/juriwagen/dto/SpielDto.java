package ch.plaintext.schuetu.service.juriwagen.dto;

public record SpielDto(
        Long id,
        String mannschaft1,
        String mannschaft2,
        String kategorie,
        String farbe,
        boolean platzhalter
) {
}
