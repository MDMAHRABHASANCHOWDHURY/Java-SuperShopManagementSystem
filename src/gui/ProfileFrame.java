package gui;

import model.User;
import service.UserService;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

public class ProfileFrame extends JFrame {
    private final User user;
    private final UserService userService = new UserService();
    private final Runnable onSaved;
    private final JTextField nameField;
    private final JTextField emailField;
    private final JTextField phoneField;
    private final JTextField addressField;
    private final JLabel photo = new JLabel();
    private final JLabel pictureStatus = new JLabel("Changes are applied when you save.");
    private String picturePath;
    private File selectedPicture;

    public ProfileFrame(User user) { this(user, () -> {}); }

    public ProfileFrame(User user, Runnable onSaved) {
        this.user = user;
        this.onSaved = onSaved;
        this.picturePath = user.getProfileImagePath();
        setTitle("Amar Dokan - My Profile");
        setSize(960, 820);
        Navigation.closeGoesBack(this);
        setLocationRelativeTo(null);
        BackgroundUI.apply(this);
        setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(7, 8, 7, 8);
        gc.gridx = 0; gc.gridy = 0; gc.gridwidth = 2;
        JLabel title = new JLabel("MY PROFILE", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        add(title, gc);
        gc.gridy++;
        add(Navigation.backButton(this), gc);
        gc.gridy++;
        photo.setIcon(ProductImages.icon(user.getProfileImagePath(), 140, 140));
        if (picturePath == null) photo.setText("No Picture");
        photo.setPreferredSize(new Dimension(140, 140));
        add(photo, gc);
        JPanel pictureActions = new SoftPanel(new FlowLayout());
        JButton upload = new JButton("Upload / Change Picture");
        JButton remove = new JButton("Remove Picture");
        pictureActions.add(upload); pictureActions.add(remove);
        gc.gridy++; add(pictureActions, gc);
        gc.gridy++; add(pictureStatus, gc);
        nameField = new JTextField(user.getName(), 22);
        emailField = new JTextField(user.getEmail(), 22);
        phoneField = new JTextField(user.getPhone(), 22);
        addressField = new JTextField(user.getAddress(), 22);
        upload.addActionListener(e -> choosePicture());
        remove.addActionListener(e -> {
            selectedPicture = null;
            picturePath = null;
            photo.setText("No Picture");
            photo.setIcon(ProductImages.icon(null, 140, 140));
            pictureStatus.setText("Picture removed. Click Save Profile to apply.");
        });
        JTextField usernameField = new JTextField(user.getUsername(), 22);
        usernameField.setEditable(false);
        JTextField roleField = new JTextField(user.getRole(), 22);
        roleField.setEditable(false);
        JTextField statusField = new JTextField(user.getStatus(), 22);
        statusField.setEditable(false);
        gc.gridwidth = 1;
        addRow(gc, "Full Name:", nameField);
        addRow(gc, "Username:", usernameField);
        addRow(gc, "Email:", emailField);
        addRow(gc, "Phone:", phoneField);
        addRow(gc, "Address:", addressField);
        addRow(gc, "Role:", roleField);
        addRow(gc, "Account Status:", statusField);
        JButton save = new JButton("Save Profile");
        gc.gridy++; gc.gridx = 0; gc.gridwidth = 2;
        add(save, gc);
        JButton password = new JButton("Change Password");
        gc.gridy++; add(password, gc);
        save.addActionListener(e -> saveProfile());
        password.addActionListener(e -> changePassword());
    }

    private void choosePicture() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Profile pictures (PNG, JPG)", "png", "jpg", "jpeg"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File file = chooser.getSelectedFile();
        if (ProductImages.read(file.getPath()) == null) {
            JOptionPane.showMessageDialog(this, "Choose a valid PNG or JPEG picture."); return;
        }
        photo.setText("");
        selectedPicture = file;
        picturePath = file.getPath();
        photo.setIcon(ProductImages.icon(picturePath, 140, 140));
        pictureStatus.setText("Picture selected. Click Save Profile to apply.");
    }

    private void addRow(GridBagConstraints gc, String label, Component field) {
        gc.gridy++; gc.gridx = 0;
        add(new JLabel(label), gc);
        gc.gridx = 1;
        add(field, gc);
    }

    private void saveProfile() {
        try {
            if (nameField.getText().isBlank() || emailField.getText().isBlank()) {
                JOptionPane.showMessageDialog(this, "Name and email are required."); return;
            }
            String path = picturePath;
            if (selectedPicture != null) {
                path = ProductImages.importProfileImage(selectedPicture);
            }
            User updated = new User();
            updated.setUserId(user.getUserId());
            updated.setName(nameField.getText().trim());
            updated.setEmail(emailField.getText().trim());
            updated.setPhone(phoneField.getText().trim());
            updated.setAddress(addressField.getText().trim());
            updated.setProfileImagePath(path);
            if (userService.updateProfile(updated)) {
                user.setName(updated.getName());
                user.setEmail(updated.getEmail());
                user.setPhone(updated.getPhone());
                user.setAddress(updated.getAddress());
                user.setProfileImagePath(path);
                picturePath = path;
                selectedPicture = null;
                photo.setIcon(ProductImages.icon(user.getProfileImagePath(), 140, 140));
                pictureStatus.setText("Profile picture and details saved.");
                onSaved.run();
                JOptionPane.showMessageDialog(this, "Profile updated.");
            } else JOptionPane.showMessageDialog(this, "Failed to update profile.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not save profile: " + ex.getMessage());
        }
    }

    private void changePassword() {
        JPasswordField newPass = new JPasswordField();
        JPasswordField confirmPass = new JPasswordField();
        Object[] message = {"New Password:", newPass, "Confirm Password:", confirmPass};
        int option = JOptionPane.showConfirmDialog(this, message, "Change Password", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String p1 = new String(newPass.getPassword());
            String p2 = new String(confirmPass.getPassword());
            if (p1.isEmpty() || !p1.equals(p2)) { JOptionPane.showMessageDialog(this, "Passwords empty or do not match."); return; }
            boolean ok = userService.changePassword(user.getUserId(), p1);
            JOptionPane.showMessageDialog(this, ok ? "Password changed." : "Failed to change password.");
        }
    }
}
