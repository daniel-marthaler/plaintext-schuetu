package ch.plaintext.schuetu.service.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IDGeneratorContainerTest {

    @Test
    void testGetNext_ReturnsNonNull() {
        String id = IDGeneratorContainer.getNext();
        assertNotNull(id);
    }

    @Test
    void testGetNext_ReturnsTwoCharacters() {
        String id = IDGeneratorContainer.getNext();
        assertEquals(2, id.length());
    }

    @Test
    void testGetNext_ReturnsUppercaseLetters() {
        String id = IDGeneratorContainer.getNext();
        assertTrue(id.matches("[A-Z]{2}"));
    }

    @Test
    void testGetNext_ReturnsUniqueIds() {
        String id1 = IDGeneratorContainer.getNext();
        String id2 = IDGeneratorContainer.getNext();
        assertNotEquals(id1, id2);
    }

    @Test
    void testGetNext_SequentialCalls() {
        // Call multiple times and verify all are unique
        java.util.Set<String> ids = new java.util.HashSet<>();
        for (int i = 0; i < 20; i++) {
            String id = IDGeneratorContainer.getNext();
            assertNotNull(id);
            assertEquals(2, id.length());
            ids.add(id);
        }
        // All should be unique (with high probability given we're iterating through the alphabet)
        assertEquals(20, ids.size());
    }
}
