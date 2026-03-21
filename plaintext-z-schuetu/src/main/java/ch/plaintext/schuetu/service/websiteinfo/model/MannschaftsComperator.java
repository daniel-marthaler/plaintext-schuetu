package ch.plaintext.schuetu.service.websiteinfo.model;

import java.io.Serializable;
import java.util.Comparator;

public class MannschaftsComperator implements Comparator<Mannschaft>, Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    public int compare(Mannschaft arg0, Mannschaft arg1) {
        return arg0.getKlasse().compareTo(arg1.getKlasse());
    }
}
