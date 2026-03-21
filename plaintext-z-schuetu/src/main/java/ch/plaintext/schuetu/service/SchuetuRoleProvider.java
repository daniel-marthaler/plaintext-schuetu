package ch.plaintext.schuetu.service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Stellt die verfuegbaren Rollen zur Verfuegung
 *
 * TODO: Replace with plaintext-root role provider if available
 */
public class SchuetuRoleProvider {

    public Set<String> getRoles() {
        return new HashSet<>(Arrays.asList("speaker", "eintrager", "kontrollierer", "beobachter", "root", "admin", "planer"));
    }
}
