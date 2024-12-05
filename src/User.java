import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

public class User extends Person {

    public User(String username, String password) {
        super(username, password);
    }

    // Hash a password using SHA-256
    private static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    // Registration for a new user
    public static void register(String username, String password, Connection conn) {
        String hashedPassword = hashPassword(password); // Hash the password
        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword); // Save hashed password
            pstmt.executeUpdate();

            // Retrieve the generated user_id
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int userId = rs.getInt(1);
                    System.out.println("User registered successfully! Your User ID is: " + userId);
                }
            }
        } catch (SQLException e) {
            System.out.println("Username already exists.");
        }
    }

    // Login implementation for User
    @Override
    public boolean login(Connection conn) {
        String query = "SELECT password FROM users WHERE username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, this.username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedHashedPassword = rs.getString("password");
                    return storedHashedPassword.equals(hashPassword(this.password)); // Compare hashed passwords
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
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
