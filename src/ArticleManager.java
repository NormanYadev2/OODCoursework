import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArticleManager {

    // Get categories from the database
    public static List<String> getCategories(Connection conn) {
        List<String> categories = new ArrayList<>();
        String query = "SELECT name FROM categories";

        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                categories.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return categories;
    }

    // Get category ID by name
    public static int getCategoryIdByName(Connection conn, String categoryName) {
        String query = "SELECT id FROM categories WHERE name = ?";
        int categoryId = -1;

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, categoryName);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    categoryId = rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return categoryId;
    }

    // Get articles by category
    public static List<Map<String, String>> getArticlesByCategory(Connection conn, int categoryId) {
        List<Map<String, String>> articles = new ArrayList<>();
        String query = "SELECT id, title, content FROM articles WHERE category_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, categoryId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, String> article = new HashMap<>();
                    article.put("id", String.valueOf(rs.getInt("id")));
                    article.put("title", rs.getString("title"));
                    article.put("content", rs.getString("content"));
                    articles.add(article);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return articles;
    }

    // Record an article view in the database
    public static int recordArticleView(Connection conn, int articleId, int userId, String username) {
        String query = "INSERT INTO article_views (article_id, user_id, username) VALUES (?, ?, ?)";
        int viewId = -1;

        try (PreparedStatement stmt = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, articleId);
            stmt.setInt(2, userId); // Set the user ID
            if (username != null) {
                stmt.setString(3, username); // Set the username
            } else {
                stmt.setNull(3, java.sql.Types.VARCHAR); // Handle null username
            }

            stmt.executeUpdate();

            // Retrieve the generated view ID
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    viewId = rs.getInt(1); // Get the generated key
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return viewId; // Return the generated view ID
    }


    // Update article rating for a specific view
    public static void updateArticleRating(Connection conn, int viewId, int rating) {
        String query = "UPDATE article_views SET rating = ? WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, rating);
            stmt.setInt(2, viewId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // Process uncategorized articles (assign a default category)
    public static void processArticles(Connection conn) {
        // Define a default category for uncategorized articles
        final String defaultCategoryName = "Uncategorized";
        int defaultCategoryId = -1;

        try {
            // Check if the default category exists, and create it if it doesn't
            String checkCategoryQuery = "SELECT id FROM categories WHERE name = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkCategoryQuery)) {
                checkStmt.setString(1, defaultCategoryName);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        defaultCategoryId = rs.getInt("id");
                    } else {
                        // Insert the default category
                        String insertCategoryQuery = "INSERT INTO categories (name) VALUES (?)";
                        try (PreparedStatement insertStmt = conn.prepareStatement(insertCategoryQuery, PreparedStatement.RETURN_GENERATED_KEYS)) {
                            insertStmt.setString(1, defaultCategoryName);
                            insertStmt.executeUpdate();
                            try (ResultSet generatedKeys = insertStmt.getGeneratedKeys()) {
                                if (generatedKeys.next()) {
                                    defaultCategoryId = generatedKeys.getInt(1);
                                }
                            }
                        }
                    }
                }
            }

            if (defaultCategoryId == -1) {
                throw new SQLException("Failed to determine or create the default category.");
            }

            // Assign the default category to uncategorized articles
            String updateArticlesQuery = "UPDATE articles SET category_id = ? WHERE category_id IS NULL";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateArticlesQuery)) {
                updateStmt.setInt(1, defaultCategoryId);
                int updatedRows = updateStmt.executeUpdate();
                System.out.println("Processed " + updatedRows + " uncategorized articles.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("An error occurred while processing uncategorized articles.");
        }
    }
}
