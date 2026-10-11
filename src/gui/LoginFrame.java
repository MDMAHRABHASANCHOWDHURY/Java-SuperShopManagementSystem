package gui;

import model.User;
import service.AuthenticationService;
import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private final AuthenticationService authService = new AuthenticationService();
    private final JTextField usernameField = new JTextField(22);
    private final JPasswordField passwordField = new JPasswordField(22);
    private final JLabel messageLabel = new JLabel(" ");

    public LoginFrame() {
        setTitle("Amar Dokan - Login");
        setSize(920, 620);
        setMinimumSize(new Dimension(760, 500));
        setLocationRelativeTo(null);
        Navigation.closeGoesBack(this);

        ImagePanel background = new ImagePanel();
        background.setLayout(new GridBagLayout());
        setContentPane(background);
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(new Color(255, 255, 255, 245));
        form.setBorder(BorderFactory.createEmptyBorder(35, 50, 35, 50));
        background.add(form);

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(12, 12, 12, 12);
        gc.gridx = 0; gc.gridy = 0; gc.gridwidth = 2;
        JLabel title = new JLabel("AMAR DOKAN - LOGIN");
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        title.setForeground(new Color(22, 58, 82));
        form.add(title, gc);

        gc.gridy++; gc.gridx = 0; gc.gridwidth = 1;
        form.add(new JLabel("Username:"), gc);
        gc.gridx = 1; form.add(usernameField, gc);
        gc.gridy++; gc.gridx = 0;
        form.add(new JLabel("Password:"), gc);
        gc.gridx = 1; form.add(passwordField, gc);

        JButton login = new JButton("LOGIN");
        gc.gridy++; gc.gridx = 0; gc.gridwidth = 2;
        login.setPreferredSize(new Dimension(210, 42));
        form.add(login, gc);

        messageLabel.setForeground(Color.RED.darker());
        gc.gridy++; form.add(messageLabel, gc);
        JButton register = new JButton("Register New Account");
        gc.gridy++; form.add(register, gc);
        gc.gridy++;
        form.add(Navigation.backButton(this), gc);

        login.addActionListener(e -> attemptLogin());
        passwordField.addActionListener(e -> attemptLogin());
        register.addActionListener(e -> Navigation.open(this, new RegistrationFrame()));
    }

    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter username and password.");
            return;
        }

        AuthenticationService.LoginResult result;
        try {
            result = authService.login(username, password);
        } catch (Exception e) {
            messageLabel.setText("Database connection failed. Check MySQL settings.");
            return;
        }
        if (!result.success) {
            messageLabel.setText(result.message);
            return;
        }

        User user = result.user;
        JFrame dashboard;
        switch (user.getRole()) {
            case "ADMIN": dashboard = new AdminDashboard(user, authService); break;
            case "CUSTOMER": dashboard = new CustomerDashboard(user, authService); break;
            case "CASHIER": dashboard = new CashierDashboard(user, authService); break;
            case "MANAGER": dashboard = new ManagerDashboard(user, authService); break;
            case "SUPPLIER": dashboard = new SupplierDashboard(user, authService); break;
            default:
                JOptionPane.showMessageDialog(this, "Unknown role: " + user.getRole());
                return;
        }
        passwordField.setText("");
        Navigation.finishLogin(this, dashboard);
    }
}
