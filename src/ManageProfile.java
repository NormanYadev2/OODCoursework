import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class ManageProfile {

    private static final Scanner scanner = new Scanner(System.in);

    // Manage the user's profile (like changing the password)
    public static void manageProfile(Connection conn, String username) {
        while (true) {
            System.out.println("\nManage Profile:");
            System.out.println("1. Update Password");
            System.out.println("2. Return to User Dashboard");
            System.out.print("Enter your choice: ");
            try {
                int choice = Integer.parseInt(scanner.nextLine());

                switch (choice) {
                    case 1:
                        updateUserPassword(conn, username);
                        break;

                    case 2:
                        return; // Exit back to user dashboard

                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }



    // Update the user's password
    public static void updateUserPassword(Connection conn, String username) {
        while (true) {
            System.out.print("Enter your current password (or type 'cancel' to return): ");
            String currentPassword = scanner.nextLine();

            if (currentPassword.equalsIgnoreCase("cancel")) {
                System.out.println("Password update canceled.");
                return; // Exit the method
            }

            // Verify current password
            String query = "SELECT password FROM users WHERE username = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();

                if (rs.next() && rs.getString("password").equals(currentPassword)) {
                    // Current password is correct; prompt for new password
                    System.out.print("Enter your new password: ");
                    String newPassword = scanner.nextLine();

                    if (newPassword.isEmpty()) {
                        System.out.println("Password cannot be empty. Please try again.");
                    } else {
                        String updateQuery = "UPDATE users SET password = ? WHERE username = ?";
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                            updateStmt.setString(1, newPassword);
                            updateStmt.setString(2, username);
                            updateStmt.executeUpdate();
                            System.out.println("Password updated successfully!");
                            return; // Exit the method
                        }
                    }
                } else {
                    System.out.println("Current password is incorrect. Please try again.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("Error verifying password.");
                return; // Exit the method on SQL error
            }
        }
    }

}