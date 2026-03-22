package ch.plaintext.schuetu.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TurnierExceptionTest {

    @Test
    void testConstructor_Message() {
        TurnierException ex = new TurnierException("test error");
        assertEquals("test error", ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    void testConstructor_MessageAndCause() {
        RuntimeException cause = new RuntimeException("root cause");
        TurnierException ex = new TurnierException("wrapped", cause);
        assertEquals("wrapped", ex.getMessage());
        assertEquals(cause, ex.getCause());
    }

    @Test
    void testConstructor_CauseOnly() {
        RuntimeException cause = new RuntimeException("root cause");
        TurnierException ex = new TurnierException(cause);
        assertEquals(cause, ex.getCause());
    }
}
