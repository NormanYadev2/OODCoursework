import java.util.ArrayList;
import java.util.List;

public class User {
    private String username;
    private String email;
    private List<String> preferences; // User interests/preferences
    private List<RatedArticle> readingHistory; // List of rated articles
    private List<String> recommendedArticles; // List of recommended articles

    // Constructor
    public User(String username, String email) {
        this.username = username;
        this.email = email;
        this.preferences = new ArrayList<>();
        this.readingHistory = new ArrayList<>();
        this.recommendedArticles = new ArrayList<>();
    }

    // Getter and Setter for preferences
    public List<String> getPreferences() {
        return preferences;
    }

    public void setPreferences(List<String> preferences) {
        this.preferences = preferences;
    }

    // Getter and Setter for reading history
    public List<RatedArticle> getReadingHistory() {
        return readingHistory;
    }

    public void setReadingHistory(List<RatedArticle> readingHistory) {
        this.readingHistory = readingHistory;
    }

    // Method to view an article
    public void viewArticle(String articleTitle) {
        System.out.println("User is viewing article: " + articleTitle);
        // Simulate viewing the article (retrieving content and displaying)
        System.out.println("System: Article content for " + articleTitle + " displayed.");
    }

    // Method to rate an article
    public void rateArticle(String articleTitle, int rating) {
        // Add the rated article to the reading history
        RatedArticle ratedArticle = new RatedArticle(articleTitle, rating);
        this.readingHistory.add(ratedArticle);
        System.out.println("System: Rating recorded for article " + articleTitle);
        // Update preferences based on rating
        updatePreferencesBasedOnRating(rating);
    }

    // Method to manage the user's profile (update info like email, password, etc.)
    public void manageProfile(String newEmail, String newPassword) {
        System.out.println("User is updating profile...");
        this.email = newEmail;
        // Assume password change logic here
        System.out.println("System: Profile updated successfully with new email: " + newEmail);
    }

    // Method to request article recommendations based on user data
    public void getRecommendations() {
        System.out.println("User is requesting article recommendations.");
        // Analyze preferences and reading history to recommend articles
        if (readingHistory.isEmpty()) {
            System.out.println("System: No reading history, showing general recommendations.");
        } else {
            System.out.println("System: Generating recommendations based on user data...");
            // Based on the reading history and preferences, recommend articles
            this.recommendedArticles = generateRecommendations();
            System.out.println("System: Recommended articles are: " + recommendedArticles);
        }
    }

    // Private helper method to update preferences based on article ratings
    private void updatePreferencesBasedOnRating(int rating) {
        if (rating >= 4) {
            System.out.println("System: User liked this article, adding to preferences.");
            // Logic to update user preferences based on rated articles can go here
        }
    }

    // Private helper method to generate recommendations
    private List<String> generateRecommendations() {
        List<String> recommendations = new ArrayList<>();
        // For simplicity, adding some dummy recommended articles
        recommendations.add("Tech Innovations in 2024");
        recommendations.add("Sports News Update");
        recommendations.add("Health and Wellness Trends");
        return recommendations;
    }
}
