package ch.plaintext.schuetu.model.enums;

/**
 * Spielplatz A, B, C oder D
 */
public enum PlatzEnum {
    A("A"), B("B"), C("C"), D("D");

    private String text;

    PlatzEnum(String text) {
        this.text = text;
    }

    public static PlatzEnum fromString(String text) {
        if (text != null) {
            for (PlatzEnum b : PlatzEnum.values()) {
                if (text.equalsIgnoreCase(b.text)) {
                    return b;
                }
            }
        }
        return null;
    }

    public String getText() {
        return this.text;
    }
}
