package service;

import dao.LoginActivityDAO;
import dao.UserDAO;
import model.User;

public class AuthenticationService {

    private final UserDAO userDAO = new UserDAO();
    private final LoginActivityDAO loginActivityDAO = new LoginActivityDAO();

    private int currentActivityId = -1;
    private User currentUser;

    public static class LoginResult {
        public final boolean success;
        public final String message;
        public final User user;

        public LoginResult(boolean success, String message, User user) {
            this.success = success;
            this.message = message;
            this.user = user;
        }
    }

    public LoginResult login(String username, String password) {
        User user = userDAO.findByUsername(username);
        if (user == null) {
            return new LoginResult(false, "No account found with that username.", null);
        }
        if (!user.getPassword().equals(password)) {
            return new LoginResult(false, "Incorrect password.", null);
        }
        if ("PENDING".equals(user.getStatus())) {
            return new LoginResult(false, "Your account is awaiting approval.", null);
        }
        if ("REJECTED".equals(user.getStatus())) {
            return new LoginResult(false, "Your registration request was rejected.", null);
        }
        if ("DISABLED".equals(user.getStatus())) {
            return new LoginResult(false, "Your account has been disabled.", null);
        }

        currentUser = user;
        currentActivityId = loginActivityDAO.recordLogin(user.getUserId());
        return new LoginResult(true, "Login successful.", user);
    }

    public void logout() {
        if (currentActivityId != -1) {
            loginActivityDAO.recordLogout(currentActivityId);
        }
        currentActivityId = -1;
        currentUser = null;
    }

    /** Registers a new account request; account starts PENDING until approved. */
    public boolean register(User user) {
        user.setStatus("PENDING");
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("CUSTOMER");
        }
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            // Self-registration as Admin is never permitted
            return false;
        }
        return userDAO.addUser(user);
    }

    public User getCurrentUser() {
        return currentUser;
    }
}
