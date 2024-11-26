import java.sql.*;

public class User extends Person {

    public User(String username, String password) {
        super(username, password);
    }

    // Login implementation for User
    @Override
    public boolean login(Connection conn) {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, this.username);
            pstmt.setString(2, this.password);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Registration for a new user
    public static void register(String username, String password, Connection conn) {
        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.executeUpdate();

            // Retrieve the generated user_id
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int userId = rs.getInt(1);
                    System.out.println("User registered successfully! Your User ID is: " + userId);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error registering user: " + e.getMessage());
        }
    }

    public int getUserId(Connection conn) {
        int userId = -1;
        try {
            String query = "SELECT id FROM users WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, this.username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                userId = rs.getInt("id");
            }
        } catch (SQLException e) {
            System.out.println("Error fetching user ID: " + e.getMessage());
        }
        return userId;
    }

}
