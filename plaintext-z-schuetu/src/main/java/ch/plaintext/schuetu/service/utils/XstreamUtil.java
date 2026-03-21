package ch.plaintext.schuetu.service.utils;

import lombok.extern.slf4j.Slf4j;

/**
 * Wrapper fuer Xstream
 *
 * TODO: XStream dependency removed - re-add com.thoughtworks.xstream:xstream if needed,
 * or replace with Jackson XML serialization
 */
@Slf4j
public class XstreamUtil {

    public static void saveObjectToFile(Object obj, String file) {
        // TODO: XStream removed - implement with Jackson or other serializer
        log.warn("XstreamUtil.saveObjectToFile not implemented - XStream dependency removed");
    }

    public static Object loadObjectFromFile(String file) {
        // TODO: XStream removed - implement with Jackson or other serializer
        log.warn("XstreamUtil.loadObjectFromFile not implemented - XStream dependency removed");
        return null;
    }

    public static Object deserializeFromString(String string) {
        // TODO: XStream removed - implement with Jackson or other serializer
        log.warn("XstreamUtil.deserializeFromString not implemented - XStream dependency removed");
        return null;
    }

    public static String serializeToString(Object object) {
        // TODO: XStream removed - implement with Jackson or other serializer
        log.warn("XstreamUtil.serializeToString not implemented - XStream dependency removed");
        return "";
    }

}
