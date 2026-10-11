package dao;

import database.DatabaseConnection;
import model.LoginActivity;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LoginActivityDAO {

    public int recordLogin(int userId) {
        String sql = "INSERT INTO login_activity (user_id, login_time) VALUES (?, NOW())";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("recordLogin error: " + e.getMessage());
        }
        return -1;
    }

    public boolean recordLogout(int activityId) {
        String sql = "UPDATE login_activity SET logout_time = NOW() WHERE activity_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, activityId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("recordLogout error: " + e.getMessage());
            return false;
        }
    }

    public List<LoginActivity> getAllActivity() {
        List<LoginActivity> list = new ArrayList<>();
        String sql = "SELECT la.*, u.name, u.role FROM login_activity la " +
                     "JOIN users u ON la.user_id = u.user_id ORDER BY la.login_time DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                LoginActivity a = new LoginActivity();
                a.setActivityId(rs.getInt("activity_id"));
                a.setUserId(rs.getInt("user_id"));
                a.setEmployeeName(rs.getString("name"));
                a.setRole(rs.getString("role"));
                Timestamp login = rs.getTimestamp("login_time");
                a.setLoginTime(login != null ? login.toLocalDateTime() : LocalDateTime.now());
                Timestamp logout = rs.getTimestamp("logout_time");
                a.setLogoutTime(logout != null ? logout.toLocalDateTime() : null);
                list.add(a);
            }
        } catch (SQLException e) {
            System.err.println("getAllActivity error: " + e.getMessage());
        }
        return list;
    }
}
