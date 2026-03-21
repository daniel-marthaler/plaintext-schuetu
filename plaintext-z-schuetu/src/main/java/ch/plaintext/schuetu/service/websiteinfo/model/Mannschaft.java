package ch.plaintext.schuetu.service.websiteinfo.model;

public class Mannschaft {
    private String gruppe;
    private String nummer;
    private String klasse;
    private String klassenname;
    private String schulhaus;
    private String captain;
    private String begleitperson;
    private int spieler;

    public String getGruppe() { return gruppe; }
    public void setGruppe(String gruppe) { this.gruppe = gruppe; }
    public String getNummer() { return nummer; }
    public void setNummer(String nummer) { this.nummer = nummer; }
    public String getKlasse() { return klasse; }
    public void setKlasse(String klasse) { this.klasse = klasse; }
    public String getKlassenname() { return klassenname; }
    public void setKlassenname(String klassenname) { this.klassenname = klassenname; }
    public String getSchulhaus() { return schulhaus; }
    public void setSchulhaus(String schulhaus) { this.schulhaus = schulhaus; }
    public String getCaptain() { return captain; }
    public void setCaptain(String captain) { this.captain = captain; }
    public String getBegleitperson() { return begleitperson; }
    public void setBegleitperson(String begleitperson) { this.begleitperson = begleitperson; }
    public int getSpieler() { return spieler; }
    public void setSpieler(int spieler) { this.spieler = spieler; }
}
