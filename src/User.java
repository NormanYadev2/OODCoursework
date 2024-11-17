import java.util.HashMap;
import java.util.Map;

public class User {
    private String email;
    private String password;

    // Map to store registered users in memory (email -> password)
    private static final Map<String, String> userDatabase = new HashMap<>();

    // Map to store user's ratings for articles (articleName -> rating)
    private final Map<String, Integer> articleRatings = new HashMap<>();

    // Constructor
    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // Method to register a new user
    public static boolean registerUser(String email, String password) {
        if (userDatabase.containsKey(email)) {
            return false; // User already exists
        }
        userDatabase.put(email, password);
        return true;
    }

    // Method to login user
    public static boolean loginUser(String email, String password) {
        return userDatabase.containsKey(email) && userDatabase.get(email).equals(password);
    }

    // Method to add or update the rating for a specific article
    public void addArticleRating(String article, int rating) {
        articleRatings.put(article, rating);
        System.out.println("Rating for article '" + article + "' has been saved.");
    }

    // Method to get the rating for a specific article (if exists)
    public Integer getArticleRating(String article) {
        return articleRatings.get(article);
    }

    // Method to check if a user is already registered
    public static boolean isUserRegistered(String email) {
        return userDatabase.containsKey(email);
    }
}
