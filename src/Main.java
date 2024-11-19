import java.sql.Connection;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Connection conn = DatabaseConnection.connect();
        if (conn == null) {
            System.out.println("Database connection failed. Exiting...");
            return;
        }

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
                        // Add more user-specific options here
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
                        // Add admin-specific actions here
                    } else {
                        System.out.println("Invalid credentials.");
                    }
                    break;

                case 4:
                    System.out.println("Logging out...");
                    scanner.close();
                    return;

                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
}
