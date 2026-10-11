package gui;

import javax.swing.*;
import java.awt.*;

// A light panel so text and tables stay readable over the grocery photo.
public class SoftPanel extends JPanel {
    public SoftPanel() { super(); setOpaque(false); }
    public SoftPanel(LayoutManager layout) { super(layout); setOpaque(false); }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(new Color(255, 255, 255, 195));
        g.fillRect(0, 0, getWidth(), getHeight());
        super.paintComponent(g);
    }
}
