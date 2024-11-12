

public class RatedArticle {
    private String title;
    private int rating;

    // Constructor
    public RatedArticle(String title, int rating) {
        this.title = title;
        this.rating = rating;
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }
}
