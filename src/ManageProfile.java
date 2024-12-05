import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class ManageProfile {
    private static final Scanner scanner = new Scanner(System.in);

    // Manage the user's profile (like changing the password)
    public static void manageProfile(Connection conn, String username) {
        while (true) {
            System.out.println("\nManage Profile:");
            System.out.println();
            System.out.println("1. Update Password");
            System.out.println("2. Return to User Dashboard");
            System.out.println();
            System.out.print("Enter your choice: ");
            System.out.println();
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

            // Hash the entered current password using SHA-256
            String hashedCurrentPassword = hashPassword(currentPassword);

            // Verify current password
            String query = "SELECT password FROM users WHERE username = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    String storedPassword = rs.getString("password").trim(); // Trim to avoid whitespace issues


                    // Compare hashed passwords
                    if (storedPassword.equals(hashedCurrentPassword)) {
                        // Current password is correct; prompt for new password
                        System.out.print("Enter your new password: ");
                        String newPassword = scanner.nextLine();

                        if (newPassword.isEmpty()) {
                            System.out.println("Password cannot be empty. Please try again.");
                        } else {
                            // Hash the new password before updating
                            String hashedNewPassword = hashPassword(newPassword);

                            // Update password in database
                            String updateQuery = "UPDATE users SET password = ? WHERE username = ?";
                            try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                                updateStmt.setString(1, hashedNewPassword);
                                updateStmt.setString(2, username);
                                int rowsUpdated = updateStmt.executeUpdate();

                                if (rowsUpdated > 0) {
                                    System.out.println("Password updated successfully!");
                                } else {
                                    System.out.println("Error updating password. Please try again.");
                                }
                            }
                            return; // Exit the method after successful update
                        }
                    } else {
                        System.out.println("Current password is incorrect. Please try again.");
                    }
                } else {
                    System.out.println("Username not found.");
                    return; // Exit method if username is not found
                }
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("Error verifying password.");
                return; // Exit the method on SQL error
            }
        }
    }

    // Hash a password using SHA-256
    private static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
}
