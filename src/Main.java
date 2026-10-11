import gui.PublicHomeFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // fall back to default look and feel
        }
        SwingUtilities.invokeLater(() -> new PublicHomeFrame().setVisible(true));
    }
}
