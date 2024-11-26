import java.sql.*;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        // Create a connection to the database
        Connection conn = DatabaseConnection.connect();
        if (conn == null) {
            System.out.println("Database connection failed. Exiting...");
            return;
        }

        // Process articles and assign categories initially
        ArticleManager.processArticles(conn);

        // Create a scanner object for user input
        Scanner scanner = new Scanner(System.in);

        // Main loop for menu navigation
        while (true) {
            System.out.println("\nMain Menu:");
            System.out.println("1. Register as User");
            System.out.println("2. Login as User");
            System.out.println("3. Login as System Administrator");
            System.out.println("4. Logout");
            System.out.print("Enter your choice: ");
            try {
                int choice = Integer.parseInt(scanner.nextLine());

                switch (choice) {
                    case 1:
                        System.out.print("Enter username: ");
                        String username = scanner.nextLine();
                        System.out.print("Enter password: ");
                        String password = scanner.nextLine();
                        User.register(username, password, conn);
                        break;

                    case 2:
                        System.out.print("Enter username: ");
                        String userUsername = scanner.nextLine();
                        System.out.print("Enter password: ");
                        String userPassword = scanner.nextLine();
                        User user = new User(userUsername, userPassword);
                        if (user.login(conn)) {
                            System.out.println("User login successful!");

                            // Retrieve userId from the database based on username
                            int userId = user.getUserId(conn);  // Assuming getUserId() method is implemented

                            // Now pass userId along with the username to the dashboard
                            userDashboard(scanner, conn, userId, userUsername);
                        } else {
                            System.out.println("Invalid credentials.");
                        }
                        break;

                    case 3:
                        System.out.print("Enter admin username: ");
                        String adminUsername = scanner.nextLine();
                        System.out.print("Enter admin password: ");
                        String adminPassword = scanner.nextLine();
                        SystemAdministrator admin = new SystemAdministrator(adminUsername, adminPassword);
                        if (admin.login(conn)) {
                            System.out.println("Admin login successful!");
                            // Add admin dashboard functionality here if needed
                        } else {
                            System.out.println("Invalid credentials.");
                        }
                        break;

                    case 4:
                        System.out.println("Logging out...");
                        closeConnection(conn); // Close the connection when logging out
                        scanner.close(); // Close the scanner when exiting the program
                        return; // Exit the main loop

                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    // User dashboard menu
    private static void userDashboard(Scanner scanner, Connection conn, int userId, String username) {
        while (true) {
            System.out.println("\nUser Dashboard:");
            System.out.println("1. View Articles");
            System.out.println("2. Manage Profile");
            System.out.println("3. Return to Homepage");
            System.out.print("Enter your choice: ");
            try {
                int userChoice = Integer.parseInt(scanner.nextLine());

                switch (userChoice) {
                    case 1:
                        ViewArticle.viewArticles(conn, userId, username); // Pass userId here along with the username
                        break;

                    case 2:
                        ManageProfile.manageProfile(conn, username); // Add manage profile option here
                        break;

                    case 3:
                        System.out.println("Returning to homepage...");
                        return; // Exit the dashboard loop and return to the main menu

                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    // Close the database connection gracefully
    private static void closeConnection(Connection conn) {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            System.out.println("Error closing database connection: " + e.getMessage());
        }
    }
}
