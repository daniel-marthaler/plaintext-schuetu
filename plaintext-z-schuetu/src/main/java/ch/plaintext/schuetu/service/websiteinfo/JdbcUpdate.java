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

    public void update(String update, String id, String col) {
        try {
            Connection conn = DriverManager.getConnection(url, user, password);
            PreparedStatement ps = conn.prepareStatement(statement.replace("col", col));
            ps.setString(1, update); ps.setString(2, id);
            ps.executeUpdate(); ps.close();
        } catch (SQLException e) { log.warn(e.getMessage(), e); }
    }

    public String read(String id, String col) {
        String ret = "";
        try {
            Connection conn = DriverManager.getConnection(url, user, password);
            PreparedStatement ps = conn.prepareStatement(read.replace("col", col));
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) { ret = rs.getString(1); }
        } catch (SQLException e) { log.warn(e.getMessage(), e); }
        return ret;
    }
}
