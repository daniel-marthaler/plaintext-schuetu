package ch.plaintext.schuetu.service.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class XstreamUtilTest {

    @Test
    void testLoadObjectFromFile_ReturnsNull() {
        Object result = XstreamUtil.loadObjectFromFile("test.xml");
        assertNull(result);
    }

    @Test
    void testSerializeToString_ReturnsEmpty() {
        String result = XstreamUtil.serializeToString(new Object());
        assertEquals("", result);
    }

    @Test
    void testDeserializeFromString_ReturnsNull() {
        Object result = XstreamUtil.deserializeFromString("<test/>");
        assertNull(result);
    }

    @Test
    void testSaveObjectToFile_DoesNotThrow() {
        assertDoesNotThrow(() -> XstreamUtil.saveObjectToFile(new Object(), "test.xml"));
    }
}
