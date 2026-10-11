package gui;

import model.User;
import service.AuthenticationService;

import javax.swing.*;
import java.awt.*;

public class CashierDashboard extends JFrame {

    public CashierDashboard(User user, AuthenticationService authService) {
        setTitle("Cashier Dashboard - " + user.getName());
        setSize(1050, 700);
        Navigation.closeGoesBack(this);
        setLocationRelativeTo(null);
        BackgroundUI.apply(this);
        setLayout(new BorderLayout());

        add(NavBar.build(user, authService, this), BorderLayout.NORTH);

        JPanel center = new ImagePanel(true);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        JLabel welcome = new JLabel("Welcome, " + user.getName());
        welcome.setFont(new Font("SansSerif", Font.BOLD, 20));
        welcome.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(welcome);
        center.add(Box.createRigidArea(new Dimension(0, 20)));

        JButton billingBtn = new JButton("Open Billing / POS");
        billingBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        billingBtn.addActionListener(e -> Navigation.open(this, new BillingFrame(user)));
        center.add(billingBtn);

        add(center, BorderLayout.CENTER);
    }
}
