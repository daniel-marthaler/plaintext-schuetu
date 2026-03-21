package ch.plaintext.schuetu.model.enums;

/**
 * Eine Anrede
 */
@SuppressWarnings("unused")
public enum AnredeEnum {
    FRAU("Frau"), HERR("Herr"), AN("An");

    private String text;

    AnredeEnum(final String text) {
        this.text = text;
    }

    public static AnredeEnum fromString(final String text) {
        if (text != null) {
            for (AnredeEnum b : AnredeEnum.values()) {
                if (text.equalsIgnoreCase(b.text)) {
                    return b;
                }
            }
        }
        return AnredeEnum.AN;
    }

    public String getText() {
        return this.text;
    }
}
