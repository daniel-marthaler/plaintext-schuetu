package ch.plaintext.schuetu.service.html;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class HTMLTagsTest {

    @Test
    void testBR() throws Exception {
        Field field = HTMLTags.class.getDeclaredField("BR");
        field.setAccessible(true);
        assertEquals("<br/>", field.get(null));
    }

    @Test
    void testTR() throws Exception {
        Field field = HTMLTags.class.getDeclaredField("TR");
        field.setAccessible(true);
        assertEquals("<tr>", field.get(null));
    }

    @Test
    void testTR_E() throws Exception {
        Field field = HTMLTags.class.getDeclaredField("TR_E");
        field.setAccessible(true);
        assertEquals("</tr>", field.get(null));
    }

    @Test
    void testTD() throws Exception {
        Field field = HTMLTags.class.getDeclaredField("TD");
        field.setAccessible(true);
        assertEquals("<td>", field.get(null));
    }

    @Test
    void testTD_E() throws Exception {
        Field field = HTMLTags.class.getDeclaredField("TD_E");
        field.setAccessible(true);
        assertEquals("</td>", field.get(null));
    }

    @Test
    void testP() throws Exception {
        Field field = HTMLTags.class.getDeclaredField("P");
        field.setAccessible(true);
        assertEquals("<p>", field.get(null));
    }

    @Test
    void testP_E() throws Exception {
        Field field = HTMLTags.class.getDeclaredField("P_E");
        field.setAccessible(true);
        assertEquals("</p>", field.get(null));
    }

    @Test
    void testPrivateConstructor() throws Exception {
        Constructor<HTMLTags> constructor = HTMLTags.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        HTMLTags instance = constructor.newInstance();
        assertNotNull(instance);
    }
}
