package ch.plaintext.schuetu.service.websiteinfo.model;

public class MannschaftEintrag {
    private ch.plaintext.schuetu.entity.Mannschaft mannschaft;

    public MannschaftEintrag() {}
    public boolean hasMannschaft() { return this.mannschaft != null; }
    public void setMannschaft(ch.plaintext.schuetu.entity.Mannschaft mannschaft) { this.mannschaft = mannschaft; }
    public String getName() { if (mannschaft != null) { return mannschaft.getName(); } return ""; }
    public String getSchulhaus() { if (mannschaft != null) { return mannschaft.getSchulhaus(); } return ""; }
    public String getCaptain() { if (mannschaft != null) { return mannschaft.getCaptain2Vorname() + " " + mannschaft.getCaptain2Name(); } return ""; }
    public String getBegleitperson() { if (mannschaft != null) { return mannschaft.getBegleitperson2Vorname() + " " + mannschaft.getBegleitperson2Name(); } return ""; }
}
