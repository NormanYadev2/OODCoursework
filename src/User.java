import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class User extends Person {

    public User(String username, String password) {
        super(username, password);
    }

    // Login implementation for User
    @Override
    public boolean login(Connection conn) {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
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

    // Registration for a new user
    public static boolean register(String username, String password, Connection conn) {
        String checkQuery = "SELECT * FROM users WHERE username = ?";
        String insertQuery = "INSERT INTO users (username, password) VALUES (?, ?)";

        try {
            // Check if username exists
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                System.out.println("Username already exists.");
                return false;
            }

            // Insert new user
            PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
            insertStmt.setString(1, username);
            insertStmt.setString(2, password);
            insertStmt.executeUpdate();
            System.out.println("Registration successful!");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
