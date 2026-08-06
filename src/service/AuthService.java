package service;

import dao.UserDAO;
import model.AppUser;
import util.UserValidator;

/**
 * Business rules for registration and login.
 */
public class AuthService {
    private final UserDAO userDAO;

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    public String register(AppUser user) {
        String validationMessage = UserValidator.validateRegistration(user);
        if (validationMessage != null) {
            return validationMessage;
        }
        if (userDAO.emailExists(user.getEmail().trim())) {
            return "Email already exists.";
        }
        user.setEmail(user.getEmail().trim());
        return userDAO.registerUser(user) ? null : "Unable to register user.";
    }

    public AppUser login(String email, String password) {
        String validationMessage = UserValidator.validateLogin(email, password);
        if (validationMessage != null) {
            return null;
        }
        return userDAO.login(email.trim(), password);
    }

    public String validateLoginInput(String email, String password) {
        return UserValidator.validateLogin(email, password);
    }
}
