import java.sql.*;
import java.util.List;
import java.util.Scanner;

public class ViewArticle {

    private static Scanner scanner = new Scanner(System.in);

    // Fetch categories from the database
    public static void displayCategories(Connection conn) {
        // Get categories from the database (assumes the ArticleManager has a method to fetch them)
        List<String> categories = ArticleManager.getCategories(conn);
        System.out.println("Choose your favourite category:");

        // Display all categories, ensuring we show all available categories
        for (int i = 0; i < categories.size(); i++) {
            System.out.println((i + 1) + ") " + categories.get(i));
        }
    }

    // Fetch and display articles based on the selected category
    public static void displayArticlesByCategory(Connection conn, int categoryId) {
        // Fetch articles for the selected category
        List<String> articles = ArticleManager.getArticlesByCategory(conn, categoryId);
        System.out.println("Select an article to view:");

        // Display the articles for the selected category (limit to 5 articles per category)
        for (int i = 0; i < articles.size(); i++) {
            System.out.println((i + 1) + ") " + articles.get(i));
        }

        // Let the user select an article
        int articleChoice = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        // Fetch and display the selected article content
        String articleContent = ArticleManager.getArticleContent(conn, articleChoice);
        System.out.println("Article Content:\n" + articleContent);
    }

    // Main function to initiate the article viewing process
    public static void viewArticles(Connection conn) {
        displayCategories(conn);

        // Get the user's category choice
        int categoryChoice = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        // Map the selected category choice to the category ID
        int selectedCategoryId = categoryChoice;  // Assuming category IDs map directly to the choice for simplicity
        displayArticlesByCategory(conn, selectedCategoryId);
    }
}
