package ch.plaintext.schuetu.web;

import jakarta.faces.context.FacesContext;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Session-scoped bean for managing the user's language preference.
 * Supports: German (default), English, French, Italian, Chinese, Japanese.
 *
 * @author info@emad.ch
 * @since 1.59.0
 */
@Component
@Scope("session")
@Slf4j
public class LanguageBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Getter
    private Locale locale = Locale.GERMAN;

    private static final Map<String, Locale> AVAILABLE_LANGUAGES = new LinkedHashMap<>();

    static {
        AVAILABLE_LANGUAGES.put("de", Locale.GERMAN);
        AVAILABLE_LANGUAGES.put("en", Locale.ENGLISH);
        AVAILABLE_LANGUAGES.put("fr", Locale.FRENCH);
        AVAILABLE_LANGUAGES.put("it", Locale.ITALIAN);
        AVAILABLE_LANGUAGES.put("zh", Locale.CHINESE);
        AVAILABLE_LANGUAGES.put("ja", Locale.JAPANESE);
    }

    /**
     * Changes the current locale and updates the JSF view root.
     *
     * @param language the language code (de, en, fr, it, zh, ja)
     */
    public String changeLanguage(String language) {
        Locale newLocale = AVAILABLE_LANGUAGES.get(language);
        if (newLocale != null) {
            this.locale = newLocale;
            FacesContext context = FacesContext.getCurrentInstance();
            if (context != null && context.getViewRoot() != null) {
                context.getViewRoot().setLocale(this.locale);
            }
            log.info("Language changed to: {}", language);
        } else {
            log.warn("Unknown language code: {}", language);
        }
        return "sprache.htm";
    }

    /**
     * Returns the current language code (e.g. "de", "en").
     */
    public String getLanguageCode() {
        return locale.getLanguage();
    }

    /**
     * Returns the available languages as a map of code to Locale.
     */
    public Map<String, Locale> getAvailableLanguages() {
        return AVAILABLE_LANGUAGES;
    }
}
