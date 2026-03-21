package ch.plaintext.schuetu.service.spielkorrekturen;

import ch.plaintext.schuetu.entity.Spiel;
import lombok.Data;

import java.util.Date;

@Data
public class SpielKorrektur {

    private String namesP;
    private String idP;
    private Date startP;
    private String eintragerP;
    private boolean eingetragenP;
    private boolean bemerkungP;
    private String kontrolleP;

    private Spiel spiel;

    public void setSpiel(Spiel spiel) {
        this.spiel = spiel;

        if (spiel.getMannschaftA() != null) {
            this.namesP = spiel.getMannschaftA().getName() + " : " + spiel.getMannschaftB().getName();
        }

        this.idP = spiel.getIdString();
        this.startP = spiel.getStart();
        this.eintragerP = spiel.getEintrager();
        this.eingetragenP = spiel.isFertigEingetragen();
        this.bemerkungP = !spiel.getNotizen().isEmpty();
        this.kontrolleP = spiel.getKontrolle();
    }

    public String getId() {
        return "" + spiel.getId();
    }

    public void setId(String id) {
        spiel.setId(Long.parseLong(id));
    }

    public Date getStart() {
        return spiel.getStart();
    }

    public String getString() {
        return spiel.getIdString();
    }

    public String getNames() {
        return namesP;
    }

    public String getTore() {
        if (!spiel.isFertigBestaetigt()) {
            return "-";
        }
        return spiel.getToreABestaetigt() + " : " + spiel.getToreBBestaetigt();
    }

    public int getA() {
        return spiel.getToreABestaetigt();
    }

    public void setA(int a) {
        spiel.setToreABestaetigt(a);
    }

    public int getB() {
        return spiel.getToreBBestaetigt();
    }

    public void setB(int b) {
        spiel.setToreBBestaetigt(b);
    }

    public String getNotitzen() {
        return spiel.getNotizen();
    }

    public void setNotitzen(String notitzen) {
        spiel.setNotizen(notitzen);
    }

}
