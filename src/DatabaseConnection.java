import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/UserManagement";
    private static final String USER = "root"; // Replace with your MySQL username
    private static final String PASSWORD = "Normanyadev123"; // Replace with your MySQL password

    public static Connection connect() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.out.println("Connection failed: " + e.getMessage());
            return null;
        }
    }

    public static void clearUsersTable(Connection connection) {
        try (Statement stmt = connection.createStatement()) {
            String clearQuery = "TRUNCATE TABLE users";  // Clears all data in the table without dropping the table
            stmt.executeUpdate(clearQuery);
            System.out.println("Previous user data cleared.");
        } catch (SQLException e) {
            System.out.println("Error clearing user data: " + e.getMessage());
        }
    }
}
