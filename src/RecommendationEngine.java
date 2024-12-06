import java.sql.*;
import java.util.*;
import java.util.concurrent.*;

public class RecommendationEngine {

    private final Scanner scanner = new Scanner(System.in);
    private final ExecutorService executorService = Executors.newCachedThreadPool();

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

                // Fetch recommendations concurrently
                Future<List<Map<String, String>>> recommendationsFuture = executorService.submit(() -> getRecommendations(userId, conn));
                List<Map<String, String>> recommendedArticles = recommendationsFuture.get(); // Wait for completion

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
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error fetching recommendations: " + e.getMessage());
        }

        return recommendations;
    }

    // Concurrent method to get recommendations for the user
    private List<Map<String, String>> getRecommendations(int userId, Connection conn) throws SQLException {
        Map<Integer, Double> ratedCategories = fetchRatedCategories(userId, conn);
        List<Integer> recentCategoryIds = fetchRecentCategories(userId, conn, ratedCategories);

        // Combine rated and recent categories
        List<Integer> prioritizedCategoryIds = new ArrayList<>(ratedCategories.keySet());
        prioritizedCategoryIds.addAll(recentCategoryIds);

        // Fetch articles concurrently
        List<Future<List<Map<String, String>>>> articleFetchFutures = new ArrayList<>();
        for (Integer categoryId : prioritizedCategoryIds) {
            articleFetchFutures.add(executorService.submit(() -> fetchArticlesByCategory(categoryId, conn)));
        }

        List<Map<String, String>> recommendations = new ArrayList<>();
        for (Future<List<Map<String, String>>> future : articleFetchFutures) {
            try {
                recommendations.addAll(future.get());
            } catch (InterruptedException | ExecutionException e) {
                System.err.println("Error fetching articles: " + e.getMessage());
            }
        }

        return recommendations;
    }

    // Fetch rated categories and their average ratings
    private Map<Integer, Double> fetchRatedCategories(int userId, Connection conn) throws SQLException {
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
        return ratedCategories;
    }

    // Fetch recently viewed but unrated categories
    private List<Integer> fetchRecentCategories(int userId, Connection conn, Map<Integer, Double> ratedCategories) throws SQLException {
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
                if (!ratedCategories.containsKey(categoryId)) {
                    recentCategoryIds.add(categoryId);
                }
            }
        }
        return recentCategoryIds;
    }

    // Fetch articles for a given category
    private List<Map<String, String>> fetchArticlesByCategory(int categoryId, Connection conn) throws SQLException {
        String articlesQuery = """
            SELECT id, title, category_id
            FROM articles
            WHERE category_id = ?
            ORDER BY id;
        """;

        List<Map<String, String>> articles = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(articlesQuery)) {
            stmt.setInt(1, categoryId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, String> article = new HashMap<>();
                article.put("id", String.valueOf(rs.getInt("id")));
                article.put("title", rs.getString("title"));
                article.put("category_id", String.valueOf(rs.getInt("category_id")));
                articles.add(article);
            }
        }
        return articles;
    }

    // Handle article selection with rating and viewing
    private void handleArticleSelection(List<Map<String, String>> recommendedArticles, Connection conn) throws SQLException {
        System.out.println("Do you wish to view any of these articles? (yes/no)");
        String response;

        // Validate initial response for "yes/no"
        while (true) {
            response = scanner.nextLine().trim(); // Remove leading/trailing spaces
            if ("yes".equalsIgnoreCase(response) || "no".equalsIgnoreCase(response)) {
                break;
            } else {
                System.out.println("Invalid input. Please enter 'yes' or 'no':");
            }
        }

        // If the user wants to view articles
        if ("yes".equalsIgnoreCase(response)) {
            while (true) {
                System.out.println("Enter the article number to view its content:");

                int choice;
                // Validate article number input
                while (true) {
                    if (scanner.hasNextInt()) {
                        choice = scanner.nextInt();
                        scanner.nextLine(); // Consume newline
                        if (choice > 0 && choice <= recommendedArticles.size()) {
                            break; // Valid article number
                        } else {
                            System.out.println("Invalid choice. Enter a number between 1 and " + recommendedArticles.size() + ":");
                        }
                    } else {
                        System.out.println("Invalid input. Please enter a valid number:");
                        scanner.nextLine(); // Clear invalid input
                    }
                }

                // Display the selected article content
                Map<String, String> selectedArticle = recommendedArticles.get(choice - 1);
                displayArticleContent(Integer.parseInt(selectedArticle.get("id")), conn);

                System.out.println("Would you like to rate this article? (yes/no)");
                // Validate response for rating
                while (true) {
                    response = scanner.nextLine().trim();
                    if ("yes".equalsIgnoreCase(response) || "no".equalsIgnoreCase(response)) {
                        break;
                    } else {
                        System.out.println("Invalid input. Please enter 'yes' or 'no':");
                    }
                }

                if ("yes".equalsIgnoreCase(response)) {
                    System.out.println("Please rate the article (1 to 5):");

                    int rating;
                    // Validate rating input
                    while (true) {
                        if (scanner.hasNextInt()) {
                            rating = scanner.nextInt();
                            scanner.nextLine(); // Consume newline
                            if (rating >= 1 && rating <= 5) {
                                break; // Valid rating
                            } else {
                                System.out.println("Invalid rating. Please enter a number between 1 and 5:");
                            }
                        } else {
                            System.out.println("Invalid input. Please enter a number:");
                            scanner.nextLine(); // Clear invalid input
                        }
                    }

                    // Update the article rating in the database
                    updateArticleRating(conn, selectedArticle.get("id"), rating);
                }

                System.out.println("Would you like to see another article? (yes/no)");
                // Validate response for continuing
                while (true) {
                    response = scanner.nextLine().trim();
                    if ("yes".equalsIgnoreCase(response) || "no".equalsIgnoreCase(response)) {
                        break;
                    } else {
                        System.out.println("Invalid input. Please enter 'yes' or 'no':");
                    }
                }

                if (!"yes".equalsIgnoreCase(response)) {
                    break; // Exit the loop if the user doesn't want to view another article
                }
            }
        }
    }


    // Display article content
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

    // Shutdown the executor service
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }
}