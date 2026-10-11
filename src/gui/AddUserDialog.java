package gui;

import model.User;
import service.UserService;

import javax.swing.*;
import java.awt.*;

/** Small dialog letting Admin/Manager add a user directly (already ACTIVE, no approval needed). */
public class AddUserDialog extends JDialog {

    public AddUserDialog(Frame owner, UserService userService, Runnable onSaved) {
        super(owner, "Add User", true);
        setSize(610, 530);
        setLocationRelativeTo(owner);
        BackgroundUI.apply(this);
        setLayout(new GridBagLayout());

        JTextField nameField = new JTextField(16);
        JTextField emailField = new JTextField(16);
        JTextField phoneField = new JTextField(16);
        JTextField usernameField = new JTextField(16);
        JPasswordField passwordField = new JPasswordField(16);
        JComboBox<String> roleBox = new JComboBox<>(new String[]{"ADMIN", "CUSTOMER", "CASHIER", "MANAGER", "SUPPLIER"});

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 8, 6, 8);
        gc.gridx = 0; gc.gridy = 0;
        addRow(gc, "Full Name:", nameField);
        addRow(gc, "Email:", emailField);
        addRow(gc, "Phone:", phoneField);
        addRow(gc, "Username:", usernameField);
        addRow(gc, "Password:", passwordField);
        addRow(gc, "Role:", roleBox);

        JButton saveBtn = new JButton("Add User");
        gc.gridy++;
        gc.gridx = 0;
        gc.gridwidth = 2;
        add(saveBtn, gc);

        JButton back = new JButton("< Back");
        gc.gridy++;
        add(back, gc);
        back.addActionListener(e -> dispose());

        saveBtn.addActionListener(e -> {
            if (nameField.getText().isBlank() || usernameField.getText().isBlank()
                    || passwordField.getPassword().length == 0) {
                JOptionPane.showMessageDialog(this, "Name, username and password are required.");
                return;
            }
            User user = new User();
            user.setName(nameField.getText().trim());
            user.setEmail(emailField.getText().trim());
            user.setPhone(phoneField.getText().trim());
            user.setUsername(usernameField.getText().trim());
            user.setPassword(new String(passwordField.getPassword()));
            user.setRole((String) roleBox.getSelectedItem());

            boolean ok = userService.addUserDirectly(user, "ACTIVE");
            if (ok) {
                onSaved.run();
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add user (username/email may exist).");
            }
        });
    }

    private void addRow(GridBagConstraints gc, String label, Component field) {
        gc.gridwidth = 1;
        gc.gridx = 0;
        add(new JLabel(label), gc);
        gc.gridx = 1;
        add(field, gc);
        gc.gridy++;
    }
}
