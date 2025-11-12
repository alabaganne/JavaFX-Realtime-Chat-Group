package app.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility class for password hashing and verification using BCrypt
 * BCrypt automatically handles salting and is resistant to rainbow table attacks
 */
public class PasswordUtil {

    // BCrypt work factor (higher = more secure but slower)
    // 12 is a good balance between security and performance
    private static final int WORK_FACTOR = 12;

    /**
     * Hashes a plain text password using BCrypt
     * @param plainTextPassword The password to hash
     * @return The hashed password
     */
    public static String hashPassword(String plainTextPassword) {
        if (plainTextPassword == null || plainTextPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt(WORK_FACTOR));
    }

    /**
     * Verifies a plain text password against a hashed password
     * @param plainTextPassword The plain text password to check
     * @param hashedPassword The hashed password to verify against
     * @return true if passwords match, false otherwise
     */
    public static boolean verifyPassword(String plainTextPassword, String hashedPassword) {
        if (plainTextPassword == null || hashedPassword == null) {
            return false;
        }

        try {
            return BCrypt.checkpw(plainTextPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            // Invalid hash format
            return false;
        }
    }

    /**
     * Checks if a password meets minimum security requirements
     * @param password The password to check
     * @return true if password is valid, false otherwise
     */
    public static boolean isPasswordValid(String password) {
        if (password == null) {
            return false;
        }

        // Minimum 8 characters
        if (password.length() < 8) {
            return false;
        }

        // Should contain at least one letter and one number for better security
        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        boolean hasDigit = password.matches(".*[0-9].*");

        return hasLetter && hasDigit;
    }

    /**
     * Gets a user-friendly message explaining password requirements
     * @return Password requirements message
     */
    public static String getPasswordRequirements() {
        return "Password must be at least 8 characters long and contain both letters and numbers.";
    }
}
