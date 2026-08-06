package ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

import model.AppUser;

/**
 * Main application window.
 */
public class MainFrame extends JFrame {
    private final CardLayout cardLayout;
    private final JPanel container;

    public MainFrame() {
        setTitle("ASSCET Event Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        cardLayout = new CardLayout();
        container = new JPanel(cardLayout);
        container.add(new LoginPanel(this), "LOGIN");

        add(container, BorderLayout.CENTER);
        showLogin();
    }

    public void showLogin() {
        container.add(new LoginPanel(this), "LOGIN");
        cardLayout.show(container, "LOGIN");
        revalidate();
        repaint();
    }

    public void showDashboard(AppUser user) {
        if ("Admin".equalsIgnoreCase(user.getRole())) {
            container.add(new EventManagementPanel(this, user), "DASHBOARD");
        } else {
            container.add(new UserEventPanel(this, user), "DASHBOARD");
        }
        cardLayout.show(container, "DASHBOARD");
        revalidate();
        repaint();
    }
}
