package ui;

import model.AppUser;
import model.Event;
import service.EventService;
import service.AdminReportService;
import service.RegistrationService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.JComboBox;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

/**
 * View-only event screen for normal users.
 */
public class UserEventPanel extends JPanel {
    private final EventService eventService;
    private final RegistrationService registrationService;
    private final AdminReportService adminReportService;
    private final AppUser currentUser;
    private final DefaultTableModel allEventsTableModel;
    private final DefaultTableModel registeredEventsTableModel;
    private final JTextField searchField;
    private final TableRowSorter<DefaultTableModel> sorter;
    private final JTable allEventsTable;
    private final JTable registeredEventsTable;

    public UserEventPanel(MainFrame mainFrame, AppUser user) {
        this.eventService = new EventService();
        this.registrationService = new RegistrationService();
        this.adminReportService = new AdminReportService();
        this.currentUser = user;
        this.allEventsTableModel = new DefaultTableModel(
            new Object[]{"ID", "Name", "Date", "Venue", "Category", "Available Seats", "Fee", "Status"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.registeredEventsTableModel = new DefaultTableModel(
            new Object[]{"ID", "Name", "Date", "Venue", "Category", "Fee"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.searchField = new JTextField(22);
        this.sorter = new TableRowSorter<>(allEventsTableModel);
        this.allEventsTable = new JTable(allEventsTableModel);
        this.registeredEventsTable = new JTable(registeredEventsTableModel);

        setLayout(new BorderLayout(16, 16));
        setBackground(UIStyles.PAGE_BACKGROUND);
        setBorder(new EmptyBorder(18, 18, 18, 18));

        add(createHeader(mainFrame, user), BorderLayout.NORTH);
        add(createTablesSection(), BorderLayout.CENTER);

        refreshData();
    }

    private JPanel createHeader(MainFrame mainFrame, AppUser user) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        JLabel title = new JLabel("Welcome, " + user.getName() + " | User Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(UIStyles.TITLE_COLOR);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        JButton searchButton = createButton("Search", new Color(37, 99, 235));
        JButton refreshButton = createButton("Refresh", new Color(22, 163, 74));
        JButton feedbackButton = createButton("Feedback", new Color(99, 102, 241));
        JButton logoutButton = createButton("Logout", new Color(220, 38, 38));

        searchButton.addActionListener(event -> filterTable());
        refreshButton.addActionListener(event -> {
            searchField.setText("");
            sorter.setRowFilter((RowFilter<? super DefaultTableModel, ? super Integer>) null);
            refreshData();
        });
        feedbackButton.addActionListener(event -> openFeedbackDialog());
        logoutButton.addActionListener(event -> mainFrame.showLogin());

        actions.add(new JLabel("Search"));
        searchField.setPreferredSize(new Dimension(220, 34));
        UIStyles.styleInput(searchField);
        actions.add(searchField);
        actions.add(searchButton);
        actions.add(refreshButton);
        actions.add(feedbackButton);
        actions.add(logoutButton);

        panel.add(title, BorderLayout.NORTH);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private JSplitPane createTablesSection() {
        JPanel allEventsPanel = new JPanel(new BorderLayout(0, 10));
        allEventsPanel.setBackground(UIStyles.PANEL_BACKGROUND);
        allEventsPanel.setBorder(UIStyles.createPanelBorder());

        JLabel allEventsLabel = new JLabel("Available Events");
        allEventsLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        allEventsLabel.setForeground(UIStyles.TITLE_COLOR);

        JButton registerButton = createButton("Register", new Color(37, 99, 235));
        JButton cancelButton = createButton("Cancel Registration", new Color(220, 38, 38));
        registerButton.setPreferredSize(new Dimension(120, 36));
        cancelButton.setPreferredSize(new Dimension(170, 36));
        registerButton.addActionListener(event -> registerSelectedEvent());
        cancelButton.addActionListener(event -> cancelSelectedEvent());

        JPanel allEventTop = new JPanel(new BorderLayout());
        allEventTop.setOpaque(false);
        allEventTop.add(allEventsLabel, BorderLayout.WEST);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setOpaque(false);
        actionPanel.add(registerButton);
        actionPanel.add(cancelButton);
        allEventTop.add(actionPanel, BorderLayout.EAST);

        UIStyles.styleTable(allEventsTable);
        allEventsTable.setRowSorter(sorter);

        allEventsPanel.add(allEventTop, BorderLayout.NORTH);
        allEventsPanel.add(new JScrollPane(allEventsTable), BorderLayout.CENTER);

        JPanel registeredPanel = new JPanel(new BorderLayout(0, 10));
        registeredPanel.setBackground(UIStyles.PANEL_BACKGROUND);
        registeredPanel.setBorder(UIStyles.createPanelBorder());

        JLabel registeredLabel = new JLabel("My Registrations");
        registeredLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        registeredLabel.setForeground(UIStyles.TITLE_COLOR);

        UIStyles.styleTable(registeredEventsTable);

        registeredPanel.add(registeredLabel, BorderLayout.NORTH);
        registeredPanel.add(new JScrollPane(registeredEventsTable), BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, allEventsPanel, registeredPanel);
        splitPane.setResizeWeight(0.65);
        splitPane.setBorder(null);
        return splitPane;
    }

    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text);
        UIStyles.styleButton(button, color, 100);
        return button;
    }

    private void loadAllEvents(List<Event> events, Set<Integer> registeredEventIds) {
        allEventsTableModel.setRowCount(0);
        for (Event event : events) {
            allEventsTableModel.addRow(new Object[]{
                event.getEventId(),
                event.getName(),
                event.getEventDate(),
                event.getVenue(),
                event.getCategory(),
                event.getAvailableSeats(),
                event.getFee(),
                registeredEventIds.contains(event.getEventId()) ? "Registered" : "Open"
            });
        }
    }

    private void loadRegisteredEvents(List<Event> events) {
        registeredEventsTableModel.setRowCount(0);
        for (Event event : events) {
            registeredEventsTableModel.addRow(new Object[]{
                event.getEventId(),
                event.getName(),
                event.getEventDate(),
                event.getVenue(),
                event.getCategory(),
                event.getFee()
            });
        }
    }

    private void refreshData() {
        List<Event> allEvents = eventService.getSafeEvents();
        List<Integer> registeredIdsList = registrationService.getRegisteredEventIds(currentUser.getUserId());
        Set<Integer> registeredIds = new HashSet<>(registeredIdsList);
        loadAllEvents(allEvents, registeredIds);
        loadRegisteredEvents(registrationService.getRegisteredEvents(currentUser.getUserId()));
    }

    private void filterTable() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            sorter.setRowFilter(null);
            return;
        }
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + keyword));
    }

    private void registerSelectedEvent() {
        int selectedRow = allEventsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select an event first.");
            return;
        }

        int modelRow = allEventsTable.convertRowIndexToModel(selectedRow);
        int eventId = Integer.parseInt(allEventsTableModel.getValueAt(modelRow, 0).toString());
        int availableSeats = Integer.parseInt(allEventsTableModel.getValueAt(modelRow, 5).toString());
        boolean alreadyRegistered = "Registered".equals(allEventsTableModel.getValueAt(modelRow, 7).toString());

        String message = registrationService.registerForEvent(currentUser.getUserId(), eventId, availableSeats, alreadyRegistered);
        if (message == null) {
            JOptionPane.showMessageDialog(this, "Registration successful.");
            refreshData();
        } else {
            JOptionPane.showMessageDialog(this, message, "Registration", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void cancelSelectedEvent() {
        int selectedRow = registeredEventsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select an event from My Registrations first.");
            return;
        }

        int modelRow = registeredEventsTable.convertRowIndexToModel(selectedRow);
        int eventId = Integer.parseInt(registeredEventsTableModel.getValueAt(modelRow, 0).toString());
        boolean alreadyRegistered = true;

        String message = registrationService.cancelRegistration(currentUser.getUserId(), eventId, alreadyRegistered);
        if (message == null) {
            JOptionPane.showMessageDialog(this, "Registration cancelled successfully.");
            refreshData();
        } else {
            JOptionPane.showMessageDialog(this, message, "Registration", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void openFeedbackDialog() {
        int selectedRow = registeredEventsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select an event from My Registrations first.");
            return;
        }

        int modelRow = registeredEventsTable.convertRowIndexToModel(selectedRow);
        int eventId = Integer.parseInt(registeredEventsTableModel.getValueAt(modelRow, 0).toString());
        String eventName = registeredEventsTableModel.getValueAt(modelRow, 1).toString();

        JComboBox<Integer> ratingBox = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5});
        JTextArea commentArea = new JTextArea(4, 22);
        UIStyles.styleTextArea(commentArea);

        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.add(new JLabel("Event: " + eventName), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 10));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        top.add(new JLabel("Rating"));
        top.add(ratingBox);
        center.add(top, BorderLayout.NORTH);
        center.add(new JScrollPane(commentArea), BorderLayout.CENTER);
        panel.add(center, BorderLayout.CENTER);

        int choice = JOptionPane.showConfirmDialog(this, panel, "Submit Feedback", JOptionPane.OK_CANCEL_OPTION);
        if (choice != JOptionPane.OK_OPTION) {
            return;
        }

        int rating = (Integer) ratingBox.getSelectedItem();
        String comment = commentArea.getText().trim();

        boolean saved = adminReportService.addFeedback(currentUser.getUserId(), eventId, rating, comment);
        if (saved) {
            JOptionPane.showMessageDialog(this, "Feedback submitted.");
        } else {
            JOptionPane.showMessageDialog(this, "Unable to submit feedback (maybe already submitted).");
        }
    }
}
