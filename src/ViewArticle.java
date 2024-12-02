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

        int articleChoice = -1;
        while (articleChoice < 1 || articleChoice > articles.size()) {
            try {
                System.out.print("Enter the number of the article to view: ");
                articleChoice = Integer.parseInt(scanner.nextLine());

                if (articleChoice < 1 || articleChoice > articles.size()) {
                    System.out.println("Invalid choice. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            }
        }

        Map<String, String> selectedArticle = articles.get(articleChoice - 1);
        int articleId = Integer.parseInt(selectedArticle.get("id"));
        System.out.println("\n--- Article Content ---");
        System.out.println(selectedArticle.get("content"));

        // Record the article view with the userId and username
        int viewId = ArticleManager.recordArticleView(conn, articleId, userId, username);

        // Optionally rate the article
        promptArticleRating(conn, viewId);
    }

    // Prompt the user to rate the article
    private static void promptArticleRating(Connection conn, int viewId) {
        String response = "";
        while (!response.equals("yes") && !response.equals("no")) {
            System.out.print("\nWould you like to rate this article? (yes/no): ");
            response = scanner.nextLine().trim().toLowerCase();

            if (response.equals("yes")) {
                int rating = -1;
                while (rating < 1 || rating > 5) {
                    try {
                        System.out.print("Rate the article out of 5: ");
                        rating = Integer.parseInt(scanner.nextLine());

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
            } else if (!response.equals("no")) {
                System.out.println("Invalid input. Please enter 'yes' or 'no'.");
            }
        }
    }

    // Main function to view articles
    public static void viewArticles(Connection conn, int userId, String username) {
        // Display categories for user selection
        displayCategories(conn);

        int categoryChoice = -1;
        List<String> categories = ArticleManager.getCategories(conn);
        while (categoryChoice < 1 || categoryChoice > categories.size()) {
            try {
                System.out.print("\nEnter the number of the category: ");
                categoryChoice = Integer.parseInt(scanner.nextLine());

                // Validate category selection
                if (categoryChoice < 1 || categoryChoice > categories.size()) {
                    System.out.println("Invalid category choice. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            }
        }

        // Retrieve the category ID and display articles
        int selectedCategoryId = ArticleManager.getCategoryIdByName(conn, categories.get(categoryChoice - 1));
        displayArticlesByCategory(conn, selectedCategoryId, userId, username);
    }
}
