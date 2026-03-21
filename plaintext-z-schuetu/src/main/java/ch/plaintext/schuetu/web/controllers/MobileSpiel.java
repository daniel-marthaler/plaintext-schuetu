package ch.plaintext.schuetu.web.controllers;

import ch.plaintext.schuetu.entity.Spiel;
import lombok.Data;

@Data
public class MobileSpiel {

    public boolean stehtBevor = true;
    private String zeile = "";
    private String color;
    private String start;
    private String platz;
    private String gegner;
    private boolean verloren;
    private String resultat;
    private boolean amSpielen = false;
    private Spiel spiel;

}
