package ch.plaintext.schuetu.service.simulation;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
public class SimulationModel implements Serializable {

    private static final long serialVersionUID = 1L;

    @Value("${spring.profiles.active:}")
    private String activeProfile;

    private String methode = "aufsteigend";
    private boolean on = false;

    @Setter
    @Getter
    private String gameName;

    public boolean isEnabled() {
        return activeProfile == null || !activeProfile.contains("prod");
    }

    public boolean isOn() { return on; }
    public void setOn(boolean on) { this.on = on; }
    public String getMethode() { return methode; }
    public void setMethode(String methode) { this.methode = methode; }

    public void on(String gameName) {
        this.gameName = gameName;
        on = !on;
    }

}
