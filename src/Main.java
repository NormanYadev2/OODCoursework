import java.sql.*;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Connection conn = DatabaseConnection.connect();
        if (conn == null) {
            System.out.println("Database connection failed. Exiting...");
            return;
        }

        // Clear previous user data when the program starts
        DatabaseConnection.clearUsersTable(conn);

        // Initialize the Scanner here and use it throughout the program
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\nMain Menu:");
            System.out.println("1. Register for User");
            System.out.println("2. Login as User");
            System.out.println("3. Login as System Administrator");
            System.out.println("4. Logout");
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    // Register a new user
                    System.out.print("Enter username: ");
                    String username = scanner.nextLine();
                    System.out.print("Enter password: ");
                    String password = scanner.nextLine();
                    User.register(username, password, conn); // Pass username, password, and connection
                    break;

                case 2:
                    // User login
                    System.out.print("Enter username: ");
                    String userUsername = scanner.nextLine();
                    System.out.print("Enter password: ");
                    String userPassword = scanner.nextLine();
                    User user = new User(userUsername, userPassword);
                    if (user.login(conn)) {
                        System.out.println("User login successful!");
                        // Display options for the logged-in user
                        System.out.println("1. View Article");
                        System.out.println("2. Return to Homepage");

                        int userChoice = scanner.nextInt();
                        scanner.nextLine(); // Consume newline

                        if (userChoice == 1) {
                            // View article flow
                            ViewArticle.viewArticles(conn); // Call ViewArticle class to display categories and articles
                        } else if (userChoice == 2) {
                            System.out.println("Returning to homepage...");
                        } else {
                            System.out.println("Invalid choice.");
                        }

                    } else {
                        System.out.println("Invalid credentials.");
                    }
                    break;

                case 3:
                    // Admin login
                    System.out.print("Enter admin username: ");
                    String adminUsername = scanner.nextLine();
                    System.out.print("Enter admin password: ");
                    String adminPassword = scanner.nextLine();
                    SystemAdministrator admin = new SystemAdministrator(adminUsername, adminPassword);
                    if (admin.login(conn)) {
                        System.out.println("Admin login successful!");
                        // Add admin-specific actions here
                    } else {
                        System.out.println("Invalid credentials.");
                    }
                    break;

                case 4:
                    // Logout
                    System.out.println("Logging out...");
                    scanner.close(); // Close the scanner on logout
                    return;

                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
}
