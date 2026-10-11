package gui;

import javax.swing.*;
import java.awt.event.*;
import java.util.Stack;

// Opens one window at a time and remembers where the Back button should go.
public class Navigation {
    private static final Stack<JFrame> pages = new Stack<>();

    public static void open(JFrame current, JFrame next) {
        pages.push(current);
        current.dispose();
        next.setVisible(true);
    }

    public static void back(JFrame current) {
        current.dispose();
        if (!pages.empty()) {
            JFrame previous = pages.pop();
            if (previous instanceof CartFrame) {
                ((CartFrame) previous).refreshCart();
            }
            previous.setVisible(true);
        } else {
            new PublicHomeFrame().setVisible(true);
        }
    }

    public static JButton backButton(JFrame current) {
        JButton button = new JButton("< Back");
        button.addActionListener(e -> back(current));
        return button;
    }

    // Clicking the window's X also goes back, instead of leaving hidden windows.
    public static void closeGoesBack(JFrame current) {
        current.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        current.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                back(current);
            }
        });
    }

    public static void finishLogin(JFrame login, JFrame dashboard) {
        pages.clear();
        login.dispose();
        dashboard.setVisible(true);
    }

    public static void home(JFrame current) {
        pages.clear();
        current.dispose();
        new PublicHomeFrame().setVisible(true);
    }
}
