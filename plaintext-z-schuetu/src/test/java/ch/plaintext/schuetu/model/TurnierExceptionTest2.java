package ch.plaintext.schuetu.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TurnierExceptionTest2 {

    @Test
    void testExceptionMessage() {
        TurnierException ex = new TurnierException("test message");
        assertEquals("test message", ex.getMessage());
    }

    @Test
    void testExceptionIsException() {
        TurnierException ex = new TurnierException("test");
        assertInstanceOf(Exception.class, ex);
    }

    @Test
    void testExceptionCanBeThrown() {
        assertThrows(TurnierException.class, () -> {
            throw new TurnierException("thrown");
        });
    }
}
