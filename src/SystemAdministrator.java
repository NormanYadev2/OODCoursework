import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.Scanner;

public class SystemAdministrator extends Person {

    private static final Scanner scanner = new Scanner(System.in);

    public SystemAdministrator(String username, String password) {
        super(username, password);
    }

    // Login implementation for Admin
    @Override
    public boolean login(Connection conn) {
        String query = "SELECT * FROM admins WHERE username = ? AND password = ?";
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

    // Admin dashboard where they can add or delete articles
    public static void adminDashboard(Connection conn) {
        while (true) {
            System.out.println("\nAdministrator Dashboard:");
            System.out.println("1. Add Article");
            System.out.println("2. Delete Article");
            System.out.println("3. Return to Homepage");
            System.out.print("Enter your choice: ");
            try {
                int adminChoice = Integer.parseInt(scanner.nextLine());

                switch (adminChoice) {
                    case 1:
                        addArticle(conn);
                        break;

                    case 2:
                        deleteArticle(conn);
                        break;

                    case 3:
                        return; // Return to homepage
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    // Add article from CSV
    private static void addArticle(Connection conn) {
        System.out.println("Adding article from CSV...");

        // Assume the CSV is in the format: "title, content, category"
        String csvFilePath = "path_to_your_csv.csv"; // Adjust this path

        try (BufferedReader reader = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] articleData = line.split(",");
                String title = articleData[0].trim();
                String content = articleData[1].trim();
                String category = articleData[2].trim();

                // Insert into the database
                String query = "INSERT INTO articles (title, content, category) VALUES (?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                    pstmt.setString(1, title);
                    pstmt.setString(2, content);
                    pstmt.setString(3, category);
                    pstmt.executeUpdate();
                    System.out.println("Article '" + title + "' added successfully.");
                } catch (SQLException e) {
                    System.out.println("Error inserting article: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading CSV file: " + e.getMessage());
        }
    }

    // Delete article by selecting its title
    private static void deleteArticle(Connection conn) {
        System.out.print("Enter article title to delete: ");
        String title = scanner.nextLine().trim();

        // Fetch article details
        String query = "SELECT * FROM articles WHERE title = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, title);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Article found: " + title);
                    System.out.print("Are you sure you want to delete this article? (y/n): ");
                    String confirmation = scanner.nextLine().trim().toLowerCase();
                    if (confirmation.equals("y")) {
                        // Delete article
                        String deleteQuery = "DELETE FROM articles WHERE title = ?";
                        try (PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery)) {
                            deleteStmt.setString(1, title);
                            deleteStmt.executeUpdate();
                            System.out.println("Article '" + title + "' deleted successfully.");
                        }
                    }
                } else {
                    System.out.println("Article not found.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving article: " + e.getMessage());
        }
    }
}
