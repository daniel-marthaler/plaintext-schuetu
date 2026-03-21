package ch.plaintext.schuetu.model.enums;

public enum SpielEnum {
    GRUPPE("GRUPPE"), GFINAL("GFINAL"), KFINAL("KFINAL");

    private String text;

    SpielEnum(String text) {
        this.text = text;
    }

    public static SpielEnum fromString(String text) {
        if (text != null) {
            for (SpielEnum b : SpielEnum.values()) {
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
