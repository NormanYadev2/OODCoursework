import java.sql.*;
import java.util.*;

public class RecommendationEngine {

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
                List<Map<String, String>> recommendedArticles = getRecommendations(userId, username, conn);

                // Convert recommendations to a list of article titles
                for (Map<String, String> article : recommendedArticles) {
                    recommendations.add(article.get("title"));
                }
            }
        }

        return recommendations;
    }

    // Logic to get recommendations based on the user's reading history
    private List<Map<String, String>> getRecommendations(int userId, String username, Connection conn) {
        List<Map<String, String>> recommendations = new ArrayList<>();
        try {
            // Step 1: Get the articles viewed by the user
            List<Map<String, String>> viewedArticles = getViewedArticles(conn, username);

            if (!viewedArticles.isEmpty()) {
                // Check if the user has rated articles
                if (hasRatedArticles(conn, username)) {
                    // Recommendations based on rated articles
                    recommendations = getRecommendationsBasedOnRatedArticles(conn, username);
                } else {
                    // Recommend articles from the same categories as viewed
                    recommendations = getRecommendationsByCategory(conn, viewedArticles);
                }
            } else {
                // Provide default recommendations
                recommendations = getDefaultRecommendations(conn);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching recommendations: " + e.getMessage());
        }
        return recommendations;
    }

    // Get recommendations based on the user's article ratings
    private List<Map<String, String>> getRecommendationsBasedOnRatedArticles(Connection conn, String username) throws SQLException {
        String query = """
                SELECT a.id, a.title, a.category_id, av.rating
                FROM articles a
                JOIN article_views av ON a.id = av.article_id
                WHERE av.username = ?
                AND av.rating IS NOT NULL
                ORDER BY av.rating DESC, av.view_date DESC;
            """;

        List<Map<String, String>> recommendations = new ArrayList<>();
        Set<Integer> ratedCategories = new HashSet<>();

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, String> article = new HashMap<>();
                article.put("id", String.valueOf(rs.getInt("id")));
                article.put("title", rs.getString("title"));
                article.put("category_id", String.valueOf(rs.getInt("category_id")));
                recommendations.add(article);

                ratedCategories.add(rs.getInt("category_id"));
            }
        }

        // Add more recommendations from rated categories
        if (!ratedCategories.isEmpty()) {
            recommendations.addAll(getArticlesFromCategories(conn, ratedCategories));
        }

        return recommendations;
    }

    // Fetch articles from specific categories
    private List<Map<String, String>> getArticlesFromCategories(Connection conn, Set<Integer> categoryIds) throws SQLException {
        StringBuilder query = new StringBuilder("SELECT id, title, category_id FROM articles WHERE category_id IN (");
        for (Integer id : categoryIds) {
            query.append(id).append(",");
        }
        query.deleteCharAt(query.length() - 1).append(")");

        List<Map<String, String>> articles = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query.toString())) {
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

    // Check if the user has rated any articles
    private boolean hasRatedArticles(Connection conn, String username) throws SQLException {
        String query = "SELECT 1 FROM article_views WHERE username = ? AND rating IS NOT NULL LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    // Fetch the articles viewed by the user
    private List<Map<String, String>> getViewedArticles(Connection conn, String username) throws SQLException {
        String query = """
                SELECT av.article_id, a.category_id
                FROM article_views av
                JOIN articles a ON av.article_id = a.id
                WHERE av.username = ?
                """;

        List<Map<String, String>> articles = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, String> article = new HashMap<>();
                article.put("article_id", String.valueOf(rs.getInt("article_id")));
                article.put("category_id", String.valueOf(rs.getInt("category_id")));
                articles.add(article);
            }
        }

        return articles;
    }

    // Fetch default recommendations (one article per category)
    private List<Map<String, String>> getDefaultRecommendations(Connection conn) throws SQLException {
        String query = """
                SELECT a.id, a.title, a.category_id
                FROM articles a
                INNER JOIN (
                    SELECT category_id, MIN(id) AS min_id
                    FROM articles
                    GROUP BY category_id
                ) grouped_articles
                ON a.category_id = grouped_articles.category_id AND a.id = grouped_articles.min_id
                """;

        List<Map<String, String>> recommendations = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Map<String, String> article = new HashMap<>();
                article.put("id", String.valueOf(rs.getInt("id")));
                article.put("title", rs.getString("title"));
                article.put("category_id", String.valueOf(rs.getInt("category_id")));
                recommendations.add(article);
            }
        }

        return recommendations;
    }

    // Fetch recommendations based on categories of viewed articles
    private List<Map<String, String>> getRecommendationsByCategory(Connection conn, List<Map<String, String>> viewedArticles) throws SQLException {
        Set<Integer> categoryIds = new HashSet<>();
        for (Map<String, String> article : viewedArticles) {
            categoryIds.add(Integer.parseInt(article.get("category_id")));
        }

        return getArticlesFromCategories(conn, categoryIds);
    }
}
