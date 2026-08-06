package ui;

import model.Admin;
import model.AppUser;
import model.NormalUser;
import service.AuthService;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

/**
 * Login and registration screen.
 */
public class LoginPanel extends JPanel {
    private final MainFrame mainFrame;
    private final AuthService authService;

    private final JTextField loginEmailField;
    private final JPasswordField loginPasswordField;

    private final JTextField registerNameField;
    private final JTextField registerEmailField;
    private final JPasswordField registerPasswordField;
    private final JComboBox<String> roleBox;

    public LoginPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.authService = new AuthService();
        this.loginEmailField = new JTextField();
        this.loginPasswordField = new JPasswordField();
        this.registerNameField = new JTextField();
        this.registerEmailField = new JTextField();
        this.registerPasswordField = new JPasswordField();
        this.roleBox = new JComboBox<>(new String[]{"User", "Admin"});

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(24, 24, 24, 24));
        setBackground(UIStyles.PAGE_BACKGROUND);

        add(createContentPanel(), BorderLayout.CENTER);
    }

    private JPanel createContentPanel() {
        JPanel wrapper = new JPanel(new BorderLayout(20, 20));
        wrapper.setOpaque(false);

        JLabel heading = new JLabel("ASSCET Event Management System");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 28));
        heading.setForeground(UIStyles.TITLE_COLOR);
        wrapper.add(heading, BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(1, 2, 20, 20));
        cards.setOpaque(false);
        cards.add(createLoginCard());
        cards.add(createRegisterCard());
        wrapper.add(cards, BorderLayout.CENTER);

        return wrapper;
    }

    private JPanel createLoginCard() {
        JPanel panel = createCardPanel("Login");
        panel.add(createFieldPanel("Email", loginEmailField));
        panel.add(createFieldPanel("Password", loginPasswordField));
        panel.add(Box.createVerticalStrut(12));

        JButton loginButton = createButton("Login", new Color(37, 99, 235));
        loginButton.addActionListener(event -> handleLogin());
        panel.add(wrapButton(loginButton));

        return panel;
    }

    private JPanel createRegisterCard() {
        JPanel panel = createCardPanel("Register");
        panel.add(createFieldPanel("Name", registerNameField));
        panel.add(createFieldPanel("Email", registerEmailField));
        panel.add(createFieldPanel("Password", registerPasswordField));
        panel.add(createFieldPanel("Role", roleBox));
        panel.add(Box.createVerticalStrut(12));

        JButton registerButton = createButton("Create Account", new Color(22, 163, 74));
        registerButton.addActionListener(event -> handleRegister());
        panel.add(wrapButton(registerButton));

        return panel;
    }

    private JPanel createCardPanel(String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(UIStyles.PANEL_BACKGROUND);
        panel.setBorder(UIStyles.createPanelBorder());

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(UIStyles.TITLE_COLOR);
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(12));
        return panel;
    }

    private JPanel createFieldPanel(String labelText, java.awt.Component field) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setForeground(UIStyles.TEXT_COLOR);
        UIStyles.styleInput((javax.swing.JComponent) field);
        field.setPreferredSize(new Dimension(280, 36));

        panel.add(label, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    private JPanel wrapButton(JButton button) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.setOpaque(false);
        panel.add(button);
        return panel;
    }

    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text);
        UIStyles.styleButton(button, color, 160);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        return button;
    }

    private void handleLogin() {
        String email = loginEmailField.getText().trim();
        String password = new String(loginPasswordField.getPassword());
        String validationMessage = authService.validateLoginInput(email, password);

        if (validationMessage != null) {
            showError(validationMessage);
            return;
        }

        AppUser user = authService.login(email, password);
        if (user == null) {
            showError("Invalid email or password.");
            return;
        }

        JOptionPane.showMessageDialog(this, "Login successful. Welcome, " + user.getName() + ".");
        mainFrame.showDashboard(user);
    }

    private void handleRegister() {
        AppUser user = "Admin".equals(roleBox.getSelectedItem()) ? new Admin() : new NormalUser();
        user.setName(registerNameField.getText().trim());
        user.setEmail(registerEmailField.getText().trim());
        user.setPassword(new String(registerPasswordField.getPassword()));
        user.setRole(roleBox.getSelectedItem().toString());

        String message = authService.register(user);
        if (message == null) {
            JOptionPane.showMessageDialog(this, "Registration successful. You can now log in.");
            registerNameField.setText("");
            registerEmailField.setText("");
            registerPasswordField.setText("");
            roleBox.setSelectedIndex(0);
        } else {
            showError(message);
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Authentication Error", JOptionPane.ERROR_MESSAGE);
    }
}
