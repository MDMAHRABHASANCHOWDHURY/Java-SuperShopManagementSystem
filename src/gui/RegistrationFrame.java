package gui;

import model.User;
import service.AuthenticationService;

import javax.swing.*;
import java.awt.*;

public class RegistrationFrame extends JFrame {

    private final AuthenticationService authService = new AuthenticationService();

    private final JTextField nameField = new JTextField(20);
    private final JTextField emailField = new JTextField(20);
    private final JTextField phoneField = new JTextField(20);
    private final JTextField addressField = new JTextField(20);
    private final JTextField usernameField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final JPasswordField confirmPasswordField = new JPasswordField(20);
    // Self-registration excludes Admin for security
    private final JComboBox<String> roleBox = new JComboBox<>(new String[]{"CUSTOMER", "CASHIER", "MANAGER", "SUPPLIER"});
    private final JLabel messageLabel = new JLabel(" ");

    public RegistrationFrame() {
        setTitle("Amar Dokan - Registration");
        setSize(990, 780);
        setMinimumSize(new Dimension(780, 670));
        Navigation.closeGoesBack(this);
        setLocationRelativeTo(null);
        ImagePanel background = new ImagePanel();
        background.setLayout(new GridBagLayout());
        setContentPane(background);
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(new Color(255, 255, 255, 245));
        form.setBorder(BorderFactory.createEmptyBorder(18, 45, 18, 45));
        background.add(form);

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 8, 6, 8);
        gc.gridx = 0;
        gc.gridy = 0;
        gc.gridwidth = 2;
        gc.anchor = GridBagConstraints.CENTER;

        JLabel title = new JLabel("AMAR DOKAN - REGISTRATION", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 25));
        form.add(title, gc);

        gc.gridwidth = 1;
        gc.anchor = GridBagConstraints.WEST;
        addRow(form, gc, "Full Name:", nameField);
        addRow(form, gc, "Email:", emailField);
        addRow(form, gc, "Phone:", phoneField);
        addRow(form, gc, "Address:", addressField);
        addRow(form, gc, "Username:", usernameField);
        addRow(form, gc, "Password:", passwordField);
        addRow(form, gc, "Confirm Password:", confirmPasswordField);
        addRow(form, gc, "Role:", roleBox);

        JButton registerBtn = new JButton("REGISTER");
        gc.gridy++;
        gc.gridx = 0;
        gc.gridwidth = 2;
        form.add(registerBtn, gc);

        messageLabel.setForeground(Color.RED);
        gc.gridy++;
        form.add(messageLabel, gc);

        JButton backLink = new JButton("Already have an account? Login");
        backLink.setBorderPainted(false);
        backLink.setContentAreaFilled(false);
        backLink.setForeground(Color.BLUE.darker());
        gc.gridy++;
        form.add(backLink, gc);

        gc.gridy++;
        form.add(Navigation.backButton(this), gc);

        registerBtn.addActionListener(e -> attemptRegister());
        backLink.addActionListener(e -> {
            Navigation.open(this, new LoginFrame());
        });
    }

    private void addRow(JPanel form, GridBagConstraints gc, String label, Component field) {
        gc.gridy++;
        gc.gridx = 0;
        form.add(new JLabel(label), gc);
        gc.gridx = 1;
        form.add(field, gc);
    }

    private void attemptRegister() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String address = addressField.getText().trim();
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirm = new String(confirmPasswordField.getPassword());
        String role = (String) roleBox.getSelectedItem();

        if (name.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please fill in all required fields.");
            return;
        }
        if (!password.equals(confirm)) {
            messageLabel.setText("Passwords do not match.");
            return;
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPhone(phone);
        user.setAddress(address);
        user.setUsername(username);
        user.setPassword(password);
        user.setRole(role);

        boolean ok;
        try {
            ok = authService.register(user);
        } catch (RuntimeException ex) {
            messageLabel.setText("Database not connected. Check MySQL.");
            return;
        }
        if (ok) {
            JOptionPane.showMessageDialog(this,
                    "Registration submitted. Your account is pending approval by an Admin or Manager.");
            Navigation.open(this, new LoginFrame());
        } else {
            messageLabel.setText("Registration failed. Username or email may already be in use.");
        }
    }
}
