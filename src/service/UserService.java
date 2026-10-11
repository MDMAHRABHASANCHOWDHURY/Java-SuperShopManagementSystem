package service;

import dao.LoginActivityDAO;
import dao.UserDAO;
import model.LoginActivity;
import model.User;

import java.util.List;

public class UserService {

    private final UserDAO userDAO = new UserDAO();
    private final LoginActivityDAO loginActivityDAO = new LoginActivityDAO();

    public boolean approveUser(int userId) {
        return userDAO.updateUserStatus(userId, "ACTIVE");
    }

    public boolean rejectUser(int userId) {
        return userDAO.updateUserStatus(userId, "REJECTED");
    }

    public boolean removeUser(int userId) {
        return userDAO.removeUser(userId);
    }

    public boolean disableUser(int userId) {
        return userDAO.updateUserStatus(userId, "DISABLED");
    }

    public boolean changeRole(int userId, String role) {
        return userDAO.updateUserRole(userId, role);
    }

    public boolean addUserDirectly(User user, String status) {
        user.setStatus(status);
        return userDAO.addUser(user);
    }

    public boolean updateProfile(User user) {
        return userDAO.updateProfile(user);
    }

    public boolean changePassword(int userId, String newPassword) {
        return userDAO.updatePassword(userId, newPassword);
    }

    public List<User> getAllUsers() {
        return userDAO.getAllUsers();
    }

    public List<User> getPendingRequests() {
        return userDAO.getPendingUsers();
    }

    public List<User> getUsersByRole(String role) {
        return userDAO.getUsersByRole(role);
    }

    public List<LoginActivity> getLoginActivity() {
        return loginActivityDAO.getAllActivity();
    }
}
