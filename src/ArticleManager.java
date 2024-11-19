import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ArticleManager {

    // Get all categories from the database
    public static List<String> getCategories(Connection conn) {
        List<String> categories = new ArrayList<>();
        String query = "SELECT name FROM categories";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                categories.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }

    // Get articles for a specific category
    public static List<String> getArticlesByCategory(Connection conn, int categoryId) {
        List<String> articles = new ArrayList<>();
        String query = "SELECT title FROM articles WHERE category_id = ? LIMIT 5"; // Only 5 articles per category
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, categoryId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    articles.add(rs.getString("title"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return articles;
    }

    // Get the content of a specific article
    public static String getArticleContent(Connection conn, int articleId) {
        String content = "";
        String query = "SELECT content FROM articles WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, articleId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    content = rs.getString("content");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return content;
    }
}
