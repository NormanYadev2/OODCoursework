import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SystemAdministrator extends Person {

    public SystemAdministrator(String username, String password) {
        super(username, password);
    }

    // Login implementation for Admin
    @Override
    public boolean login(Connection conn) {
        String query = "SELECT * FROM admins WHERE username = ? AND password = ?";
        try {
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, this.username);
            pstmt.setString(2, this.password);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
