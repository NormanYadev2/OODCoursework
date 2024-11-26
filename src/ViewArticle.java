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

        System.out.println("Choose your favourite category:");
        for (int i = 0; i < categories.size(); i++) {
            System.out.println((i + 1) + ") " + categories.get(i));
        }
    }

    // Display articles based on the selected category
    public static void displayArticlesByCategory(Connection conn, int categoryId) {
        // Fetch articles for the selected category
        List<Map<String, String>> articles = ArticleManager.getArticlesByCategory(conn, categoryId);

        if (articles.isEmpty()) {
            System.out.println("No articles available in this category.");
            return;
        }

        System.out.println("Select an article to view:");
        for (int i = 0; i < articles.size(); i++) {
            System.out.println((i + 1) + ") " + articles.get(i).get("title"));
        }

        // Validate and process user selection
        try {
            System.out.print("Enter your choice: ");
            int articleChoice = Integer.parseInt(scanner.nextLine());

            if (articleChoice < 1 || articleChoice > articles.size()) {
                System.out.println("Invalid article choice. Please try again.");
                return;
            }

            // Display the selected article's content
            Map<String, String> selectedArticle = articles.get(articleChoice - 1);
            System.out.println("\nArticle Content:\n" + selectedArticle.get("content"));

            // Ask the user if they want to rate the article
            System.out.print("\nDo you wish to rate this article? (yes/no): ");
            String rateResponse = scanner.nextLine().trim().toLowerCase();

            if (rateResponse.equals("yes")) {
                System.out.print("Rate the article out of 5: ");
                try {
                    int rating = Integer.parseInt(scanner.nextLine());

                    if (rating < 1 || rating > 5) {
                        System.out.println("Invalid rating. Please enter a number between 1 and 5.");
                    } else {
                        ArticleManager.rateArticle(conn, Integer.parseInt(selectedArticle.get("id")), rating);
                        System.out.println("Thank you for your feedback!");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter a valid number.");
                }
            }

            // Return to the user dashboard
            System.out.println("Returning to the user dashboard...");
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a valid number.");
        }
    }

    // Process uncategorized articles
    public static void processUncategorizedArticles(Connection conn) {
        System.out.println("Processing uncategorized articles...");
        ArticleManager.processArticles(conn);
        System.out.println("Uncategorized articles have been categorized.");
    }

    // Main function to view articles
    public static void viewArticles(Connection conn) {
        // Process uncategorized articles first
        processUncategorizedArticles(conn);

        // Display categories for user to choose from
        displayCategories(conn);

        System.out.print("Enter the category number: ");
        try {
            int categoryChoice = Integer.parseInt(scanner.nextLine());

            // Validate category selection
            List<String> categories = ArticleManager.getCategories(conn);
            if (categoryChoice < 1 || categoryChoice > categories.size()) {
                System.out.println("Invalid category choice. Please try again.");
                return;
            }

            // Map user's choice to category ID and display articles
            int selectedCategoryId = ArticleManager.getCategoryIdByName(conn, categories.get(categoryChoice - 1));
            displayArticlesByCategory(conn, selectedCategoryId);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a valid number.");
        }
    }
}
