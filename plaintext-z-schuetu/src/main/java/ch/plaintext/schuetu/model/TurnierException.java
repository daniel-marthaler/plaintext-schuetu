package ch.plaintext.schuetu.model;

/**
 * Generelle Schuelerturnier Exception
 */
public class TurnierException extends Exception {

    private static final long serialVersionUID = 1L;

    public TurnierException(String text, Exception e) {
        super(text, e);
    }

    public TurnierException(String text) {
        super(text);
    }

    public TurnierException(Exception e) {
        super(e);
    }

}
