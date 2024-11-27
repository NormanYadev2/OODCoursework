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
            System.out.println("3. LogOut");
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
                        System.out.println("Logging out...");
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
        String csvFilePath = "C:\\Users\\Muralish\\Desktop\\OODCoursework\\MyOODProject\\Articles\\AddArticle.csv"; // Adjust this path

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
    // Delete article by selecting category and then title
    private static void deleteArticle(Connection conn) {
        // Step 1: List categories
        // Fetch and display categories with a custom or default order
        String categoryQuery = "SELECT id, name FROM categories ORDER BY id ASC"; // or custom order with CASE
        try (PreparedStatement pstmt = conn.prepareStatement(categoryQuery);
             ResultSet rs = pstmt.executeQuery()) {

            System.out.println("Select a category to view articles:");
            while (rs.next()) {
                String categoryName = rs.getString("name");
                int categoryId = rs.getInt("id");
                System.out.println(categoryId + ". " + categoryName); // Display categories in order
            }

            // Get the selected category from user input
            System.out.print("Enter category number: ");
            int categoryChoice = Integer.parseInt(scanner.nextLine());

            // Fetch and display articles for the selected category
            String articleQuery = "SELECT id, title FROM articles WHERE category_id = ?";
            try (PreparedStatement articleStmt = conn.prepareStatement(articleQuery)) {
                articleStmt.setInt(1, categoryChoice); // Filter by selected category
                try (ResultSet articleRs = articleStmt.executeQuery()) {
                    System.out.println("Select an article to delete:");
                    while (articleRs.next()) {
                        String title = articleRs.getString("title");
                        int articleId = articleRs.getInt("id");
                        System.out.println(articleId + ". " + title);
                    }

                    // Get the article to delete
                    System.out.print("Enter article number to delete: ");
                    int articleChoice = Integer.parseInt(scanner.nextLine());

                    // Delete the selected article
                    String deleteQuery = "DELETE FROM articles WHERE id = ?";
                    try (PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery)) {
                        deleteStmt.setInt(1, articleChoice);
                        int rowsAffected = deleteStmt.executeUpdate();
                        if (rowsAffected > 0) {
                            System.out.println("Article deleted successfully.");
                        } else {
                            System.out.println("Error deleting article.");
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error handling categories or articles: " + e.getMessage());
        }


    }
}
