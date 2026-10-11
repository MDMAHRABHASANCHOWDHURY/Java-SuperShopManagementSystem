package gui;

import model.User;
import service.AuthenticationService;

import javax.swing.*;
import java.awt.*;

/** Shared top navigation bar: Home | Products | Profile | Logout. */
public class NavBar {

    public static JPanel build(User user, AuthenticationService authService, JFrame currentFrame) {
        JPanel nav = new JPanel(new BorderLayout());
        nav.setBackground(new Color(30, 30, 30));

        JLabel brand = new JLabel("  AMAR DOKAN");
        brand.setForeground(Color.WHITE);
        brand.setFont(new Font("SansSerif", Font.BOLD, 16));
        nav.add(brand, BorderLayout.WEST);

        JPanel links = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        links.setOpaque(false);

        JButton backBtn = flatButton("< Back");
        JButton productsBtn = flatButton("Products");
        JButton profileBtn = flatButton("Profile");
        JButton logoutBtn = flatButton("Logout");

        backBtn.addActionListener(e -> Navigation.back(currentFrame));
        productsBtn.addActionListener(e -> Navigation.open(currentFrame, new ProductFrame(user)));
        profileBtn.setIcon(ProductImages.icon(user.getProfileImagePath(), 32, 32));
        profileBtn.setToolTipText(user.getName());
        profileBtn.addActionListener(e -> Navigation.open(currentFrame, new ProfileFrame(user)));
        logoutBtn.addActionListener(e -> {
            authService.logout();
            Navigation.home(currentFrame);
        });

        links.add(backBtn);
        links.add(productsBtn);
        links.add(profileBtn);
        links.add(logoutBtn);
        nav.add(links, BorderLayout.EAST);

        return nav;
    }

    private static JButton flatButton(String text) {
        JButton b = new JButton(text);
        b.setForeground(Color.WHITE);
        b.setBackground(new Color(30, 30, 30));
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        return b;
    }
}
