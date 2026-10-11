package gui;

import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;

// Draws the same grocery picture on every screen.
public class ImagePanel extends JPanel {
    private static Image background;
    private final boolean light;

    public ImagePanel() {
        this(false);
    }

    public ImagePanel(boolean light) {
        this.light = light;
        if (background == null) {
            try {
                background = ImageIO.read(new File("src/images/store-background.jpg"));
            } catch (Exception e) {
                setBackground(new Color(224, 238, 239));
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (background != null) {
            g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
        }
        if (light) {
            g.setColor(new Color(255, 255, 255, 75));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}
