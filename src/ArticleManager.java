import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ArticleManager {


    // Define category keywords for NLP-based categorization
    public static String categorizeArticle(String title, String content) {
        // Define category keywords
        Map<String, List<String>> categoryKeywords = new HashMap<>();
        categoryKeywords.put("Entertainment", Arrays.asList(
                "movies", "gaming", "music", "celebrity", "tv", "theater", "hollywood", "songs",
                "concerts", "film", "drama", "award", "scenes", "k-pop", "streaming", "behind the scenes", "k pop", "kpop"));
        categoryKeywords.put("Health", Arrays.asList(
                "health", "stress", "diet", "fitness", "nutrition", "wellness", "disease", "therapy", "mental well-being"));
        categoryKeywords.put("Science", Arrays.asList(
                "research", "nasa", "theory", "experiment", "discovery", "biology", "physics", "chemistry", "space",
                "environment", "climate", "warming", "ocean", "marine", "ecosystem", "coral", "reef", "sea level",
                "biodiversity", "biodiversity conservation", "conservation", "future", "exploration"));
        categoryKeywords.put("Sports", Arrays.asList(
                "mental side of sports","Training","Pro Athletes","strategies","workout", "youth sports", "community development", "soccer", "sports", "game", "games",
                "training", "olympics", "tournament", "athlete", "cricket", "football", "basketball", "hockey", "winning","eSports","professional leagues","competitive gaming"));
        categoryKeywords.put("Technology", Arrays.asList(
                "ai", "technology", "computing", "smart", "blockchain", "software", "tech", "innovation",
                "gadgets", "computers", "robotics", "internet", "cyber", "streaming"));

        // Combine the title and content for keyword searching
        String combinedText = title + " " + content;
        combinedText = combinedText.toLowerCase();  // Convert to lowercase for case-insensitive comparison

        // Initialize a map to store match counts per category
        Map<String, Integer> categoryMatchCounts = new HashMap<>();

        // Iterate over each category and its keywords
        for (Map.Entry<String, List<String>> entry : categoryKeywords.entrySet()) {
            String category = entry.getKey();
            List<String> keywords = entry.getValue();

            int matchCount = 0;

            // Check for keyword matches in combined article content
            for (String keyword : keywords) {
                if (combinedText.contains(keyword.toLowerCase())) {
                    matchCount++;  // Increment match count if keyword is found
                }
            }

            // Store the match count for the category
            categoryMatchCounts.put(category, matchCount);
        }

        // Find the category with the highest match count
        String mostRelevantCategory = "Uncategorized"; // Default category if no matches are found
        int maxMatches = 0;

        // Logic to select the category with the highest match count
        for (Map.Entry<String, Integer> entry : categoryMatchCounts.entrySet()) {
            if (entry.getValue() > maxMatches) {
                maxMatches = entry.getValue();
                mostRelevantCategory = entry.getKey();
            }
        }

        return mostRelevantCategory;
    }

    // Process uncategorized articles and categorize them using NLP
    public static void processArticles(Connection conn) {
        String getUncategorizedArticlesQuery = "SELECT id, title, content FROM articles WHERE category_id IS NULL";
        String updateArticleCategoryQuery = "UPDATE articles SET category_id = ? WHERE id = ?";

        try (PreparedStatement getArticlesStmt = conn.prepareStatement(getUncategorizedArticlesQuery);
             ResultSet articlesRs = getArticlesStmt.executeQuery()) {

            ExecutorService executor = Executors.newFixedThreadPool(10);  // Limit the number of threads

            while (articlesRs.next()) {
                int articleId = articlesRs.getInt("id");
                String title = articlesRs.getString("title");
                String content = articlesRs.getString("content");

                // Submit the categorization task to the executor
                executor.submit(() -> {
                    String category = categorizeArticle(title, content);

                    // Get or create the category ID
                    int categoryId = getOrCreateCategoryId(conn, category);

                    // Update the article's category
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateArticleCategoryQuery)) {
                        updateStmt.setInt(1, categoryId);
                        updateStmt.setInt(2, articleId);
                        updateStmt.executeUpdate();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });
            }

            executor.shutdown(); // Properly shut down the executor service

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // Ensure a category exists in the database or create it
    private static final Map<String, Integer> categoryCache = new ConcurrentHashMap<>();

    public static int getOrCreateCategoryId(Connection conn, String categoryName) {
        return categoryCache.computeIfAbsent(categoryName, category -> {
            // Database query to get or create the category
            int categoryId = -1;
            String checkCategoryQuery = "SELECT id FROM categories WHERE name = ?";
            String insertCategoryQuery = "INSERT INTO categories (name) VALUES (?)";

            try (PreparedStatement checkStmt = conn.prepareStatement(checkCategoryQuery)) {
                checkStmt.setString(1, categoryName);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        categoryId = rs.getInt("id");
                    } else {
                        try (PreparedStatement insertStmt = conn.prepareStatement(insertCategoryQuery, Statement.RETURN_GENERATED_KEYS)) {
                            insertStmt.setString(1, categoryName);
                            insertStmt.executeUpdate();
                            try (ResultSet generatedKeys = insertStmt.getGeneratedKeys()) {
                                if (generatedKeys.next()) {
                                    categoryId = generatedKeys.getInt(1);
                                }
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return categoryId;
        });
    }


    // Record an article view
    public static int recordArticleView(Connection conn, int articleId, int userId, String username) {
        String query = "INSERT INTO article_views (article_id, user_id, username) VALUES (?, ?, ?)";
        int viewId = -1;

        try (PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, articleId);
            stmt.setInt(2, userId);
            stmt.setString(3, username);

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    viewId = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return viewId;
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

    // Get all category names from the database
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

    // Get articles by category ID
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

    // Get category ID by category name
    public static int getCategoryIdByName(Connection conn, String categoryName) {
        int categoryId = -1;
        String query = "SELECT id FROM categories WHERE name = ?";

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

}