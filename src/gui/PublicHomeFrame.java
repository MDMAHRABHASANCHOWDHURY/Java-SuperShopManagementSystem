package gui;

import javax.swing.*;
import java.awt.*;

public class PublicHomeFrame extends JFrame {

    public PublicHomeFrame() {
        setTitle("Amar Dokan - Home");
        setSize(1060, 690);
        setMinimumSize(new Dimension(820, 540));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        ImagePanel background = new ImagePanel();
        background.setLayout(new GridBagLayout());
        setContentPane(background);

        JPanel menu = new JPanel();
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBackground(new Color(255, 255, 255, 245));
        menu.setBorder(BorderFactory.createEmptyBorder(40, 65, 45, 65));

        JLabel title = new JLabel("AMAR DOKAN");
        title.setFont(new Font("SansSerif", Font.BOLD, 40));
        title.setForeground(new Color(22, 58, 82));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        menu.add(title);
        menu.add(Box.createVerticalStrut(13));

        JLabel welcome = new JLabel("Welcome to Amar Dokan");
        welcome.setFont(new Font("SansSerif", Font.PLAIN, 20));
        welcome.setAlignmentX(Component.CENTER_ALIGNMENT);
        menu.add(welcome);
        menu.add(Box.createVerticalStrut(38));

        JButton login = makeButton("LOGIN", new Color(25, 104, 181));
        JButton register = makeButton("REGISTER", new Color(38, 143, 96));
        JButton products = makeButton("VIEW PRODUCTS", new Color(204, 114, 30));

        menu.add(login);
        menu.add(Box.createVerticalStrut(14));
        menu.add(register);
        menu.add(Box.createVerticalStrut(14));
        menu.add(products);

        background.add(menu);
        login.addActionListener(e -> Navigation.open(this, new LoginFrame()));
        register.addActionListener(e -> Navigation.open(this, new RegistrationFrame()));
        products.addActionListener(e -> Navigation.open(this, new ProductFrame(null)));
    }

    private JButton makeButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 17));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(320, 52));
        button.setPreferredSize(new Dimension(320, 52));
        return button;
    }
}
