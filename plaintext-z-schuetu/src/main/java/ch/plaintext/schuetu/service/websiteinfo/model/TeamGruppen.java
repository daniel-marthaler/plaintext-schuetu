package ch.plaintext.schuetu.service.websiteinfo.model;

import java.util.ArrayList;
import java.util.List;

public class TeamGruppen {
    private String name;
    private int total;
    private List<Mannschaft> mannschaften = new ArrayList<>();

    public List<Mannschaft> getMannschaften() { return mannschaften; }
    public void addMannschaft(Mannschaft mannschaften) { this.mannschaften.add(mannschaften); }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }
}
