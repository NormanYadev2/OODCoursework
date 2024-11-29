import java.sql.*;
import java.util.*;

public class RecommendationEngine {

    private Scanner scanner = new Scanner(System.in);

    // Main method to get article recommendations for a user
    public List<String> recommendArticles(String username, Connection conn) throws SQLException {
        List<String> recommendations = new ArrayList<>();

        // Retrieve user ID based on username
        String query = "SELECT id FROM users WHERE username = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int userId = rs.getInt("id");

                // Fetch recommendations based on the user ID
                List<Map<String, String>> recommendedArticles = getRecommendations(userId, conn);

                // Display recommendations to the user
                if (!recommendedArticles.isEmpty()) {
                    System.out.println("Recommended Articles:");
                    int index = 1;
                    for (Map<String, String> article : recommendedArticles) {
                        System.out.println(index + ". " + article.get("title"));
                        recommendations.add(article.get("title"));
                        index++;
                    }

                    // Prompt the user to view an article
                    handleArticleSelection(recommendedArticles, conn);
                } else {
                    System.out.println(" ");
                }
            }
        }

        return recommendations;
    }

    // Get recommendations for the user
    // Get recommendations for the user
    private List<Map<String, String>> getRecommendations(int userId, Connection conn) throws SQLException {
        // Step 1: Get average ratings for categories
        String categoryRatingQuery = """
        SELECT a.category_id, AVG(av.rating) AS avg_rating
        FROM articles a
        JOIN article_views av ON a.id = av.article_id
        WHERE av.user_id = ? AND av.rating IS NOT NULL
        GROUP BY a.category_id
        ORDER BY avg_rating DESC;
    """;

        Map<Integer, Double> ratedCategories = new LinkedHashMap<>();
        try (PreparedStatement stmt = conn.prepareStatement(categoryRatingQuery)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ratedCategories.put(rs.getInt("category_id"), rs.getDouble("avg_rating"));
            }
        }

        // Step 2: Get recently viewed categories (not rated)
        String categoryViewQuery = """
        SELECT DISTINCT a.category_id, MAX(av.view_date) AS last_viewed
        FROM articles a
        JOIN article_views av ON a.id = av.article_id
        WHERE av.user_id = ? AND av.rating IS NULL
        GROUP BY a.category_id
        ORDER BY last_viewed DESC;
    """;

        List<Integer> recentCategoryIds = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(categoryViewQuery)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int categoryId = rs.getInt("category_id");
                // Include only if not already in rated categories
                if (!ratedCategories.containsKey(categoryId)) {
                    recentCategoryIds.add(categoryId);
                }
            }
        }

        // Combine rated categories (priority) and recent categories
        List<Integer> prioritizedCategoryIds = new ArrayList<>(ratedCategories.keySet());
        prioritizedCategoryIds.addAll(recentCategoryIds);

        // Step 3: Fetch articles from these categories in order
        List<Map<String, String>> recommendations = new ArrayList<>();
        for (Integer categoryId : prioritizedCategoryIds) {
            String articlesQuery = """
            SELECT id, title, category_id
            FROM articles
            WHERE category_id = ?
            ORDER BY id;
        """;

            try (PreparedStatement stmt = conn.prepareStatement(articlesQuery)) {
                stmt.setInt(1, categoryId);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    Map<String, String> article = new HashMap<>();
                    article.put("id", String.valueOf(rs.getInt("id")));
                    article.put("title", rs.getString("title"));
                    article.put("category_id", String.valueOf(rs.getInt("category_id")));
                    recommendations.add(article);
                }
            }
        }

        return recommendations;
    }


    // Prompt the user to view articles or return to the dashboard
    private void handleArticleSelection(List<Map<String, String>> recommendedArticles, Connection conn) throws SQLException {
        System.out.println("Do you wish to view any of these articles? (yes/no)");
        String response = scanner.nextLine();

        if ("yes".equalsIgnoreCase(response)) {
            while (true) {
                System.out.println("Enter the article number to view its content:");
                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                if (choice > 0 && choice <= recommendedArticles.size()) {
                    Map<String, String> selectedArticle = recommendedArticles.get(choice - 1);
                    displayArticleContent(Integer.parseInt(selectedArticle.get("id")), conn);

                    System.out.println("Would you like to rate this article? (yes/no)");
                    response = scanner.nextLine();

                    if ("no".equalsIgnoreCase(response)) {
                        // Continue showing other recommendations if user doesn't want to rate
                        System.out.println("No rating given. Continuing to other articles...");
                        break;
                    }

                    // If 'yes', we proceed with rating (if needed)
                    System.out.println("Please rate the article (1 to 5):");
                    int rating = scanner.nextInt();
                    scanner.nextLine(); // Consume newline

                    // Update the rating in the database
                    updateArticleRating(conn, selectedArticle.get("id"), rating);

                    System.out.println("Would you like to see another article? (yes/no)");
                    response = scanner.nextLine();

                    if (!"yes".equalsIgnoreCase(response)) {
                        System.out.println("Returning to the dashboard...");
                        break;
                    }
                } else {
                    System.out.println("Invalid choice. Try again.");
                }
            }
        } else {
            System.out.println("Returning to the dashboard...");
        }
    }

    // Display the content of the selected article
    private void displayArticleContent(int articleId, Connection conn) {
        String query = "SELECT content FROM articles WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, articleId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                System.out.println("Article Content:");
                System.out.println(rs.getString("content"));
            } else {
                System.out.println("Article not found.");
            }
        } catch (SQLException e) {
            System.err.println("Error fetching article content: " + e.getMessage());
        }
    }

    // Update article rating in the database
    private void updateArticleRating(Connection conn, String articleId, int rating) throws SQLException {
        String query = "UPDATE article_views SET rating = ? WHERE article_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, rating);
            stmt.setString(2, articleId);
            stmt.executeUpdate();
        }
    }
}
