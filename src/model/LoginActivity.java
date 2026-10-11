package model;

import java.time.LocalDateTime;

public class LoginActivity {
    private int activityId;
    private int userId;
    private String employeeName; // convenience, joined field
    private String role;         // convenience, joined field
    private LocalDateTime loginTime;
    private LocalDateTime logoutTime;

    public LoginActivity() {}

    public LoginActivity(int userId, LocalDateTime loginTime) {
        this.userId = userId;
        this.loginTime = loginTime;
    }

    public int getActivityId() { return activityId; }
    public void setActivityId(int activityId) { this.activityId = activityId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public LocalDateTime getLoginTime() { return loginTime; }
    public void setLoginTime(LocalDateTime loginTime) { this.loginTime = loginTime; }

    public LocalDateTime getLogoutTime() { return logoutTime; }
    public void setLogoutTime(LocalDateTime logoutTime) { this.logoutTime = logoutTime; }

    public boolean isActive() { return logoutTime == null; }
}
