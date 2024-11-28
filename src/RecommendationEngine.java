import java.sql.*;
import java.util.*;
import java.util.concurrent.*;

public class RecommendationEngine {

    private static final ExecutorService executor = Executors.newFixedThreadPool(10);

    /**
     * Recommend articles based on user reading history and ratings.
     *
     * @param username The username of the user for whom recommendations are generated.
     * @param conn     The database connection.
     * @return List of recommended article titles.
     */
    public List<String> recommendArticles(String username, Connection conn) {
        List<String> recommendedArticles = new ArrayList<>();

        try {
            // Fetch user's viewed and rated articles
            Map<Integer, Integer> userRatings = fetchUserRatings(username, conn);
            if (userRatings.isEmpty()) {
                System.out.println("No rating or viewing history found for user. Providing default recommendations.");
                return fetchDefaultRecommendations(conn);
            }

            // Fetch similar articles based on collaborative filtering
            Set<Integer> similarArticles = findSimilarArticles(userRatings, conn);

            // Fetch additional recommendations using content-based filtering
            Set<Integer> contentBasedArticles = fetchContentBasedRecommendations(userRatings, conn);

            // Combine and filter results
            Set<Integer> finalRecommendations = new HashSet<>(similarArticles);
            finalRecommendations.addAll(contentBasedArticles);

            // Fetch article titles for the recommendations
            recommendedArticles = fetchArticleTitles(finalRecommendations, conn);

        } catch (SQLException e) {
            System.out.println("Error generating recommendations: " + e.getMessage());
        }

        return recommendedArticles;
    }

    /**
     * Fetch user's rated articles with their ratings.
     */
    private Map<Integer, Integer> fetchUserRatings(String username, Connection conn) throws SQLException {
        String query = "SELECT av.article_id, av.rating " +
                "FROM article_views av WHERE av.username = ? AND av.rating IS NOT NULL";

        Map<Integer, Integer> userRatings = new HashMap<>();

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int articleId = rs.getInt("article_id");
                    int rating = rs.getInt("rating");
                    userRatings.put(articleId, rating);
                }
            }
        }

        return userRatings;
    }

    /**
     * Find similar articles using collaborative filtering.
     */
    private Set<Integer> findSimilarArticles(Map<Integer, Integer> userRatings, Connection conn) throws SQLException {
        Set<Integer> similarArticles = new HashSet<>();

        // Query to fetch articles rated similarly by other users
        String query = "SELECT av2.article_id " +
                "FROM article_views av1 " +
                "JOIN article_views av2 ON av1.username != av2.username " +
                "WHERE av1.article_id = ? AND av1.rating = av2.rating";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            for (int articleId : userRatings.keySet()) {
                stmt.setInt(1, articleId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        int similarArticleId = rs.getInt("article_id");
                        if (!userRatings.containsKey(similarArticleId)) {
                            similarArticles.add(similarArticleId);
                        }
                    }
                }
            }
        }

        return similarArticles;
    }

    /**
     * Fetch additional recommendations using content-based filtering.
     */
    private Set<Integer> fetchContentBasedRecommendations(Map<Integer, Integer> userRatings, Connection conn) throws SQLException {
        Set<Integer> recommendedArticles = new HashSet<>();

        // Query to fetch articles from similar categories
        String query = "SELECT a.id FROM articles a " +
                "JOIN categories c ON a.category_id = c.id " +
                "WHERE c.id IN (SELECT DISTINCT category_id " +
                "               FROM articles WHERE id = ?) " +
                "AND a.id NOT IN (SELECT article_id FROM article_views WHERE username = ?)";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            for (int articleId : userRatings.keySet()) {
                stmt.setInt(1, articleId);
                stmt.setString(2, userRatings.get(articleId).toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        recommendedArticles.add(rs.getInt("id"));
                    }
                }
            }
        }

        return recommendedArticles;
    }

    /**
     * Fetch default recommendations for users without history.
     */
    private List<String> fetchDefaultRecommendations(Connection conn) throws SQLException {
        List<String> defaultRecommendations = new ArrayList<>();
        String query = "SELECT title FROM articles ORDER BY RAND() LIMIT 5";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                defaultRecommendations.add(rs.getString("title"));
            }
        }

        return defaultRecommendations;
    }

    /**
     * Fetch article titles for a set of article IDs.
     */
    private List<String> fetchArticleTitles(Set<Integer> articleIds, Connection conn) throws SQLException {
        List<String> titles = new ArrayList<>();
        String query = "SELECT title FROM articles WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            for (int articleId : articleIds) {
                stmt.setInt(1, articleId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        titles.add(rs.getString("title"));
                    }
                }
            }
        }

        return titles;
    }

    /**
     * Shutdown the executor service gracefully.
     */
    public static void shutdownExecutor() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }
}
