import org.junit.jupiter.api.Test;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RecommendationEngineConcurrencyTest {


    static class RecommendationService {
        public String getRecommendations(String user) {
            // Simulate some delay or time-consuming process (like querying a database)
            try {
                Thread.sleep(100); // Simulate delay
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "Recommendations for " + user;
        }
    }

    @Test
    public void testConcurrentRecommendations() throws InterruptedException {
        // Create an instance of the RecommendationService
        RecommendationService service = new RecommendationService();

        // Number of concurrent threads (users)
        int numberOfUsers = 10;

        // Create a thread pool to simulate multiple users making requests concurrently
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfUsers);
        CountDownLatch latch = new CountDownLatch(numberOfUsers);  // Used to wait for all threads to finish

        // Create a list to store the results
        ConcurrentLinkedQueue<String> results = new ConcurrentLinkedQueue<>();

        // Submit tasks (representing users requesting recommendations)
        for (int i = 0; i < numberOfUsers; i++) {
            final int userId = i;
            executorService.submit(() -> {
                try {
                    // Simulate the user requesting recommendations
                    String recommendations = service.getRecommendations("User" + userId);
                    results.add(recommendations);  // Add the result to the concurrent queue
                } finally {
                    latch.countDown();  // Decrement the latch counter when the task is done
                }
            });
        }

        // Wait for all tasks to finish
        latch.await();

        // Verify that all recommendations are generated correctly
        // Check that the number of results is equal to the number of users
        assertTrue(results.size() == numberOfUsers, "Not all recommendations were processed.");

        // Optionally, you can print the results or add further checks
        results.forEach(System.out::println);

        // Shutdown the executor service
        executorService.shutdown();
    }
}
