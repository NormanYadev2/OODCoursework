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
    public static String categorizeArticle(String title, String content) {
        Map<String, List<String>> categoryKeywords = new HashMap<>();
        categoryKeywords.put("Entertainment", Arrays.asList(
                "movies", "gaming", "music", "celebrity", "tv", "theater", "hollywood", "songs",
                "concerts", "film", "drama", "award", "scenes", "k-pop", "streaming", "behind the scenes", "k pop", "kpop"));
        categoryKeywords.put("Health", Arrays.asList(
                "health", "stress", "diet", "fitness", "nutrition", "wellness", "disease", "therapy", "mental well-being"));
        categoryKeywords.put("Science", Arrays.asList(
                "research", "nasa", "theory", "experiment", "discovery", "biology", "physics", "chemistry", "space", "environment", "climate", "warming", "ocean", "marine", "ecosystem", "coral", "reef", "sea level", "biodiversity", "biodiversity conservation", "conservation"));
        categoryKeywords.put("Sports", Arrays.asList(
                "mental side of sports", "youth sports", "community development", "soccer", "sports", "game", "games",
                "training", "olympics", "tournament", "athlete", "cricket", "football", "basketball", "hockey", "winning"));
        categoryKeywords.put("Technology", Arrays.asList(
                "ai", "technology", "computing", "smart", "blockchain", "software", "tech", "innovation",
                "gadgets", "computers", "robotics", "internet", "cyber", "streaming"));

        String normalizedText = (title + " " + content).toLowerCase();

        // Debugging output to track keyword matches
        Map<String, Integer> categoryMatches = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : categoryKeywords.entrySet()) {
            String category = entry.getKey();
            int matches = 0;

            for (String keyword : entry.getValue()) {
                if (normalizedText.contains(keyword.toLowerCase())) {
                    matches += keyword.split(" ").length > 1 ? 5 : 1; // Higher weight for exact phrases
                    System.out.println("Matched keyword '" + keyword + "' in category: " + category);
                }
            }
            categoryMatches.put(category, matches);
        }

        // Display match results
        System.out.println("Category matches: " + categoryMatches);

        // Prioritize sports over health when matches are tied
        List<String> prioritizedCategories = Arrays.asList("Sports", "Health", "Science", "Technology", "Entertainment");
        String bestCategory = "Uncategorized";
        int maxMatches = 0;

        for (String category : prioritizedCategories) {
            int matches = categoryMatches.getOrDefault(category, 0);
            if (matches > maxMatches) {
                maxMatches = matches;
                bestCategory = category;
            }
        }

        return bestCategory;
    }
}