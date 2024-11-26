import java.sql.*;
import java.util.*;

public class ArticleManager {

    // Fetch all categories from the database
    public static List<String> getCategories(Connection conn) {
        List<String> categories = new ArrayList<>();
        String query = "SELECT name FROM categories";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                categories.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }

    // Fetch all uncategorized articles from the database
    public static List<Map<String, String>> getUncategorizedArticles(Connection conn) {
        List<Map<String, String>> articles = new ArrayList<>();
        String query = "SELECT id, title, content FROM articles WHERE category_id IS NULL";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Map<String, String> article = new HashMap<>();
                article.put("id", String.valueOf(rs.getInt("id")));
                article.put("title", rs.getString("title"));
                article.put("content", rs.getString("content"));
                articles.add(article);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return articles;
    }

    // Fetch articles for a specific category
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

    // Assign a category to an article and save it in the database
    public static void assignCategoryToArticle(Connection conn, int articleId, int categoryId) {
        String query = "UPDATE articles SET category_id = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, categoryId);
            stmt.setInt(2, articleId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Fetch category ID by category name
    public static int getCategoryIdByName(Connection conn, String category) {
        int categoryId = -1;
        String query = "SELECT id FROM categories WHERE name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, category);
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

    // Process all uncategorized articles and assign categories
    public static void processArticles(Connection conn) {
        List<Map<String, String>> articles = getUncategorizedArticles(conn);
        for (Map<String, String> article : articles) {
            int articleId = Integer.parseInt(article.get("id"));
            String title = article.get("title");
            String content = article.get("content");

            String category = categorizeArticle(title, content);
            int categoryId = getCategoryIdByName(conn, category);
            if (categoryId != -1) {
                assignCategoryToArticle(conn, articleId, categoryId);
            } else {
                System.out.println("Article ID " + articleId + " could not be categorized.");
            }
        }
    }

    // Categorize article using title and content
    // Categorize article using title and content
    public static String categorizeArticle(String title, String content) {
        Map<String, List<String>> categoryKeywords = new HashMap<>();
        categoryKeywords.put("Entertainment", Arrays.asList(
                "movies", "gaming", "music", "celebrity", "tv", "hollywood", "concert", "k-pop", "drama"));
        categoryKeywords.put("Health", Arrays.asList(
                "health", "stress", "diet", "fitness", "wellness", "therapy", "mental well-being"));
        categoryKeywords.put("Science", Arrays.asList(
                "research", "nasa", "biology", "physics", "chemistry", "climate", "ocean", "biodiversity"));
        categoryKeywords.put("Sports", Arrays.asList(
                "soccer", "sports", "cricket", "football", "basketball", "hockey", "athlete", "olympics"));
        categoryKeywords.put("Technology", Arrays.asList(
                "ai", "technology", "computing", "blockchain", "robotics", "gadgets", "cyber", "internet"));

        String normalizedText = (title + " " + content).toLowerCase();

        String bestCategory = "Uncategorized";
        int maxMatches = 0;

        for (Map.Entry<String, List<String>> entry : categoryKeywords.entrySet()) {
            String category = entry.getKey();
            int matches = 0;

            for (String keyword : entry.getValue()) {
                if (normalizedText.contains(keyword.toLowerCase())) {
                    matches++;
                }
            }

            if (matches > maxMatches) {
                maxMatches = matches;
                bestCategory = category;
            }
        }

        return bestCategory;
    }
    // Save article rating to the database
    public static void rateArticle(Connection conn, int articleId, int rating) {
        String query = "INSERT INTO article_ratings (article_id, rating) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, articleId);
            stmt.setInt(2, rating);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
