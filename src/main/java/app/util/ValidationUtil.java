package app.util;

import java.util.regex.Pattern;

/**
 * Utility class for input validation
 * Provides validation for common input types like email, text fields, etc.
 */
public class ValidationUtil {

    // Email regex pattern - validates most common email formats
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    /**
     * Validates an email address
     * @param email The email to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Checks if a string is null, empty, or contains only whitespace
     * @param value The string to check
     * @return true if empty, false otherwise
     */
    public static boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * Validates a name field
     * @param name The name to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidName(String name) {
        if (isEmpty(name)) {
            return false;
        }

        String trimmedName = name.trim();

        // Name should be at least 2 characters
        if (trimmedName.length() < 2) {
            return false;
        }

        // Name should not exceed 100 characters
        if (trimmedName.length() > 100) {
            return false;
        }

        // Name should only contain letters, spaces, hyphens, and apostrophes
        return trimmedName.matches("^[a-zA-Z\\s'-]+$");
    }

    /**
     * Sanitizes input to prevent potential injection attacks
     * Removes or escapes potentially dangerous characters
     * @param input The input to sanitize
     * @return Sanitized input
     */
    public static String sanitizeInput(String input) {
        if (input == null) {
            return "";
        }

        // Trim whitespace
        String sanitized = input.trim();

        // Remove potential SQL injection characters
        // Note: This is a basic sanitization. PreparedStatements are the primary defense
        sanitized = sanitized.replaceAll("[;'\"\\\\]", "");

        return sanitized;
    }

    /**
     * Validates that a string is within a specified length range
     * @param value The string to validate
     * @param minLength Minimum length
     * @param maxLength Maximum length
     * @return true if within range, false otherwise
     */
    public static boolean isLengthValid(String value, int minLength, int maxLength) {
        if (value == null) {
            return false;
        }

        int length = value.trim().length();
        return length >= minLength && length <= maxLength;
    }

    /**
     * Validation result class to provide detailed feedback
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String message;

        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * Comprehensive validation for registration data
     * @param name User's full name
     * @param email User's email
     * @param password User's password
     * @return ValidationResult with details
     */
    public static ValidationResult validateRegistration(String name, String email, String password) {
        if (isEmpty(name)) {
            return new ValidationResult(false, "Name is required");
        }

        if (!isValidName(name)) {
            return new ValidationResult(false, "Please enter a valid name (2-100 characters, letters only)");
        }

        if (isEmpty(email)) {
            return new ValidationResult(false, "Email is required");
        }

        if (!isValidEmail(email)) {
            return new ValidationResult(false, "Please enter a valid email address");
        }

        if (isEmpty(password)) {
            return new ValidationResult(false, "Password is required");
        }

        if (!PasswordUtil.isPasswordValid(password)) {
            return new ValidationResult(false, PasswordUtil.getPasswordRequirements());
        }

        return new ValidationResult(true, "Validation successful");
    }

    /**
     * Validates login credentials
     * @param email User's email
     * @param password User's password
     * @return ValidationResult with details
     */
    public static ValidationResult validateLogin(String email, String password) {
        if (isEmpty(email)) {
            return new ValidationResult(false, "Email is required");
        }

        if (!isValidEmail(email)) {
            return new ValidationResult(false, "Please enter a valid email address");
        }

        if (isEmpty(password)) {
            return new ValidationResult(false, "Password is required");
        }

        return new ValidationResult(true, "Validation successful");
    }
}
