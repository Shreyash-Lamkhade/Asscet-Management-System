package util;

import model.AppUser;

import java.util.regex.Pattern;

/**
 * Validates registration and login input.
 */
public final class UserValidator {
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private UserValidator() {
    }

    public static String validateRegistration(AppUser user) {
        if (user == null) {
            return "User data is missing.";
        }
        if (isBlank(user.getName())) {
            return "Name is required.";
        }
        if (isBlank(user.getEmail()) || !EMAIL_PATTERN.matcher(user.getEmail()).matches()) {
            return "Enter a valid email address.";
        }
        if (isBlank(user.getPassword()) || user.getPassword().length() < 6) {
            return "Password must be at least 6 characters.";
        }
        if (isBlank(user.getRole())) {
            return "Role is required.";
        }
        return null;
    }

    public static String validateLogin(String email, String password) {
        if (isBlank(email) || !EMAIL_PATTERN.matcher(email).matches()) {
            return "Enter a valid email address.";
        }
        if (isBlank(password)) {
            return "Password is required.";
        }
        return null;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
