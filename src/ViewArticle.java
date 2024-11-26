import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class ViewArticle {

    private static final Scanner scanner = new Scanner(System.in);

    // Display all categories available in the database
    public static void displayCategories(Connection conn) {
        List<String> categories = ArticleManager.getCategories(conn);

        if (categories.isEmpty()) {
            System.out.println("No categories available at the moment.");
            return;
        }

        System.out.println("Available categories:");
        for (int i = 0; i < categories.size(); i++) {
            System.out.println((i + 1) + ") " + categories.get(i));
        }
    }

    // Display articles based on the selected category and allow users to view and rate them
    public static void displayArticlesByCategory(Connection conn, int categoryId, int userId, String username) {
        List<Map<String, String>> articles = ArticleManager.getArticlesByCategory(conn, categoryId);

        if (articles.isEmpty()) {
            System.out.println("No articles available in this category.");
            return;
        }

        System.out.println("Articles in the selected category:");
        for (int i = 0; i < articles.size(); i++) {
            System.out.println((i + 1) + ") " + articles.get(i).get("title"));
        }

        try {
            System.out.print("Enter the number of the article to view: ");
            int articleChoice = Integer.parseInt(scanner.nextLine());

            if (articleChoice < 1 || articleChoice > articles.size()) {
                System.out.println("Invalid choice. Please try again.");
                return;
            }

            Map<String, String> selectedArticle = articles.get(articleChoice - 1);
            int articleId = Integer.parseInt(selectedArticle.get("id"));
            System.out.println("\n--- Article Content ---");
            System.out.println(selectedArticle.get("content"));

            // Record the article view with the userId and username
            int viewId = ArticleManager.recordArticleView(conn, articleId, userId, username);

            // Optionally rate the article
            promptArticleRating(conn, viewId);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a valid number.");
        }
    }

    // Prompt the user to rate the article
    private static void promptArticleRating(Connection conn, int viewId) {
        System.out.print("\nWould you like to rate this article? (yes/no): ");
        String response = scanner.nextLine().trim().toLowerCase();

        if (response.equals("yes")) {
            System.out.print("Rate the article out of 5: ");
            try {
                int rating = Integer.parseInt(scanner.nextLine());

                if (rating < 1 || rating > 5) {
                    System.out.println("Invalid rating. Please enter a number between 1 and 5.");
                } else {
                    ArticleManager.updateArticleRating(conn, viewId, rating);
                    System.out.println("Thank you for your feedback!");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number between 1 and 5.");
            }
        }
    }

    // Main function to view articles
    public static void viewArticles(Connection conn, int userId, String username) {
        // Display categories for user selection
        displayCategories(conn);

        System.out.print("\nEnter the number of the category: ");
        try {
            int categoryChoice = Integer.parseInt(scanner.nextLine());

            // Validate category selection
            List<String> categories = ArticleManager.getCategories(conn);
            if (categories.isEmpty() || categoryChoice < 1 || categoryChoice > categories.size()) {
                System.out.println("Invalid category choice. Please try again.");
                return;
            }

            // Retrieve the category ID and display articles
            int selectedCategoryId = ArticleManager.getCategoryIdByName(conn, categories.get(categoryChoice - 1));
            displayArticlesByCategory(conn, selectedCategoryId, userId, username);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a valid number.");
        }
    }
}
