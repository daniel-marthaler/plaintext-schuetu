package ch.plaintext.schuetu.model.ranglistensortierung;

import ch.plaintext.schuetu.entity.Mannschaft;
import ch.plaintext.schuetu.model.enums.RangierungsgrundEnum;

/**
 * Eine Zeile in der Rangliste
 */
public class RanglisteneintragZeile {

    private Mannschaft mannschaft = null;
    private RangierungsgrundEnum rangierungsgrund = null;
    private int toreErziehlt = 0;
    private int toreKassiert = 0;
    private int spieleVorbei = 0;
    private int spieleAnstehend = 0;

    private int punkte = 0;

    public RanglisteneintragZeile() {
        rangierungsgrund = RangierungsgrundEnum.KEINSPIEL;
    }

    public Mannschaft getMannschaft() {
        return this.mannschaft;
    }

    public void setMannschaft(final Mannschaft mannschaft) {
        this.mannschaft = mannschaft;
    }

    public RangierungsgrundEnum getRangierungsgrund() {
        return this.rangierungsgrund;
    }

    public void setRangierungsgrund(final RangierungsgrundEnum rangierungsgrund) {
        this.rangierungsgrund = rangierungsgrund;
    }

    public int getToreErziehlt() {
        return this.toreErziehlt;
    }

    public void setToreErziehlt(final int toreErziehlt) {
        this.toreErziehlt = toreErziehlt;
    }

    public int getToreKassiert() {
        return this.toreKassiert;
    }

    public void setToreKassiert(final int toreKassiert) {
        this.toreKassiert = toreKassiert;
    }

    public int getSpieleVorbei() {
        return this.spieleVorbei;
    }

    public void setSpieleVorbei(final int spieleVorbei) {
        this.spieleVorbei = spieleVorbei;
    }

    public int getSpieleAnstehend() {
        return this.spieleAnstehend;
    }

    public void setSpieleAnstehend(final int spieleAnstehend) {
        this.spieleAnstehend = spieleAnstehend;
    }

    public int getTordifferenz() {
        return toreErziehlt - toreKassiert;
    }

    public int getPunkte() {
        return this.punkte;
    }

    public void setPunkte(final int punkte) {
        this.punkte = punkte;
    }

    public String print() {
        return this.mannschaft.getName() + " (" + this.spieleAnstehend + "|" + this.spieleVorbei + ")" + " P:" + String.format("%02d", this.punkte) + " TV:"
                + String.format("%02d", this.getTordifferenz()) + " Te:" + String.format("%02d", this.toreErziehlt) + "  " + this.rangierungsgrund;
    }

    @Override
    public String toString() {
        return this.mannschaft.getName() + "[ p:" + this.punkte + " " + "tv:" + this.getTordifferenz() + " te:" + this.toreErziehlt + " " + this.rangierungsgrund + "]";
    }

}
