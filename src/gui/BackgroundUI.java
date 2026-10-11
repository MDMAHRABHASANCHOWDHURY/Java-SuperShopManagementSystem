package gui;

import javax.swing.*;
import java.awt.*;

// Applies the common shop background to normal screens and pop-up forms.
public class BackgroundUI {
    public static void apply(JFrame window) {
        ImagePanel panel = new ImagePanel(true);
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        window.setContentPane(panel);
    }

    public static void apply(JDialog dialog) {
        ImagePanel panel = new ImagePanel(true);
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        dialog.setContentPane(panel);
    }
}
