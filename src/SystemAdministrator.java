
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class SystemAdministrator extends Person {

    private static final Scanner scanner = new Scanner(System.in);

    public SystemAdministrator(String username, String password) {
        super(username, password);
    }




    // Login implementation for Admin
    @Override
    public boolean login(Connection conn) {
        return "admin".equals(this.username) && "admin123".equals(this.password);
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

        // Query to fetch all article titles from the addarticles table
        String query = "SELECT title FROM addarticles";

        try (PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            // Store article titles in a list
            List<String> articlesList = new ArrayList<>();

            while (rs.next()) {
                String title = rs.getString("title").trim(); // Get and trim the article title
                articlesList.add(title);
                System.out.println("Title: " + title); // Display the title of each article
            }

            if (articlesList.isEmpty()) {
                System.out.println("No articles found in the database.");
                return;
            }

            // Get the article title choice from the user
            System.out.print("Enter the title of the article to add: ");
            String selectedTitle = scanner.nextLine().trim();

            // Check if the selected title exists in the list
            if (!articlesList.contains(selectedTitle)) {
                System.out.println("Article not found.");
                return;
            }

            // Find the content of the selected article from the addarticles table
            String contentQuery = "SELECT content FROM addarticles WHERE title = ?";
            try (PreparedStatement pstmtContent = conn.prepareStatement(contentQuery)) {
                pstmtContent.setString(1, selectedTitle);
                try (ResultSet contentRs = pstmtContent.executeQuery()) {
                    if (contentRs.next()) {
                        String selectedContent = contentRs.getString("content").trim();

                        // Now, display the available categories for the admin to choose from
                        String categoryQuery = "SELECT id, name FROM categories ORDER BY id ASC";
                        try (PreparedStatement pstmtCategory = conn.prepareStatement(categoryQuery);
                             ResultSet categoryRs = pstmtCategory.executeQuery()) {

                            System.out.println("Select a category for the article:");
                            while (categoryRs.next()) {
                                String categoryName = categoryRs.getString("name");
                                int categoryId = categoryRs.getInt("id");
                                System.out.println(categoryId + ". " + categoryName); // Display categories
                            }

                            // Get the selected category number from the admin
                            System.out.print("Enter category number: ");
                            int categoryChoice = Integer.parseInt(scanner.nextLine());

                            // Insert the selected article into the articles table, linked with the selected category
                            String insertQuery = "INSERT INTO articles (title, content, category_id) VALUES (?, ?, ?)";
                            try (PreparedStatement pstmtInsert = conn.prepareStatement(insertQuery)) {
                                pstmtInsert.setString(1, selectedTitle);
                                pstmtInsert.setString(2, selectedContent);
                                pstmtInsert.setInt(3, categoryChoice); // Linking article with selected category
                                pstmtInsert.executeUpdate();
                                System.out.println("Article '" + selectedTitle + "' added successfully to the category.");
                            } catch (SQLException e) {
                                System.out.println("Error inserting article: " + e.getMessage());
                            }
                        } catch (SQLException e) {
                            System.out.println("Error fetching categories: " + e.getMessage());
                        }
                    }
                } catch (SQLException e) {
                    System.out.println("Error fetching article content: " + e.getMessage());
                }
            } catch (SQLException e) {
                System.out.println("Error fetching article content: " + e.getMessage());
            }

        } catch (SQLException e) {
            System.out.println("Error fetching articles from the database: " + e.getMessage());
        }
    }





    // Delete article by selecting category and then title
    private static void deleteArticle(Connection conn) {
        // Step 1: List categories
        String categoryQuery = "SELECT id, name FROM categories ORDER BY id ASC";
        try (PreparedStatement pstmt = conn.prepareStatement(categoryQuery);
             ResultSet rs = pstmt.executeQuery()) {

            System.out.println("Select a category to view articles:");
            while (rs.next()) {
                String categoryName = rs.getString("name");
                int categoryId = rs.getInt("id");
                System.out.println(categoryId + ". " + categoryName); // Display categories
            }

            // Get the selected category from user input
            System.out.print("Enter category number: ");
            int categoryChoice = Integer.parseInt(scanner.nextLine());

            // Step 2: Fetch and display articles for the selected category
            String articleQuery = "SELECT id, title FROM articles WHERE category_id = ?";
            try (PreparedStatement articleStmt = conn.prepareStatement(articleQuery)) {
                articleStmt.setInt(1, categoryChoice); // Filter by selected category
                try (ResultSet articleRs = articleStmt.executeQuery()) {
                    // Check if there are articles in the selected category
                    boolean hasArticles = false;
                    while (articleRs.next()) {
                        if (!hasArticles) {
                            System.out.println("Select an article to delete:");
                            hasArticles = true;
                        }
                        String title = articleRs.getString("title");
                        int articleId = articleRs.getInt("id");
                        System.out.println(articleId + ". " + title);
                    }

                    // If no articles, handle gracefully
                    if (!hasArticles) {
                        System.out.println("No articles found in the selected category.");
                        return; // Exit method
                    }

                    // Step 3: Get the article to delete
                    System.out.print("Enter article number to delete: ");
                    int articleChoice = Integer.parseInt(scanner.nextLine());

                    // Step 4: Delete the selected article
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

