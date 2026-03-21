package ch.plaintext.schuetu.service.websiteinfo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.*;

@Slf4j
@Component
public class JdbcUpdate {

    @Value("${schuetu.mysql.pw:}")
    String password = "";

    String url = "jdbc:mysql://hanedire.mysql.db.hostpoint.ch:3306/hanedire_schuelerturnier?useSSL=false";
    String user = "hanedire_schuel";

    String statement = "update wbcemod_wysiwyg SET content = ? where col = ?";
    String read = "select content from wbcemod_wysiwyg where col = ?";

    public static String getHeaderFront() {
        return "<img style=\"margin-top: 5px; margin-bottom: 5px;\" src=\"https://schuelerturnierworb.imgix.net/sponsoren2.png\" /> <br />";
    }

    public static String getHeaderSponsoren() {
        return "<h4>Ein herzliches Dankeschoen unseren Sponsoren, Goennern Donatoren und Inserenten</h4>";
    }

    public static String getLines() {
        String res = "<h4>Hauptsponsor</h4>";
        res = res + getLine("vaudoise.jpg", "https://www.vaudoise.ch/");
        return res;
    }

    public static String getLine(String pic, String link) {
        return "<a target=\"_blank\" href=\"${link}\"><img src=\"https://schuelerturnierworb.imgix.net/${pic}?w=350&amp;scale=fit&amp;exp=1\" /></a><br />"
                .replace("${pic}", pic).replace("${link}", link);
    }

    public void update(String update, String id, String col) {
        try {
            Connection conn = DriverManager.getConnection(url, user, password);
            PreparedStatement ps = conn.prepareStatement(statement.replace("col", col));
            ps.setString(1, update);
            ps.setString(2, id);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            log.warn(e.getMessage(), e);
        }
    }

    public String read(String id, String col) {
        String ret = "";
        try {
            Connection conn = DriverManager.getConnection(url, user, password);
            PreparedStatement ps = conn.prepareStatement(read.replace("col", col));
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ret = rs.getString(1);
            }
        } catch (SQLException e) {
            log.warn(e.getMessage(), e);
        }
        return ret;
    }
}
