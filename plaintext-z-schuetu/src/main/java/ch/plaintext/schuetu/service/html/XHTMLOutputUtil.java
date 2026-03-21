package ch.plaintext.schuetu.service.html;

import lombok.extern.slf4j.Slf4j;
import org.htmlcleaner.*;
import org.springframework.stereotype.Component;

/**
 * Erstellt XHTML Output
 */
@Component
@Slf4j
public class XHTMLOutputUtil {

    public String cleanup(String input, boolean justbody) {

        CleanerProperties props = new CleanerProperties();

        props.setTranslateSpecialEntities(true);
        props.setTransResCharsToNCR(true);
        props.setOmitComments(true);
        props.setOmitDoctypeDeclaration(true);

        TagNode tagNode = new HtmlCleaner(props).clean(input);

        Object[] o = new Object[1];

        try {

            if (justbody) {
                o = tagNode.evaluateXPath("//body");
            } else {
                o[0] = tagNode;
            }
        } catch (XPatherException e) {
            log.error(e.getMessage(), e);
        }

        TagNode tag = (TagNode) o[0];
        String ret = "";

        SimpleHtmlSerializer serializer = new SimpleHtmlSerializer(props);

        try {
            ret = serializer.getAsString(tag);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return ret;
    }

}
