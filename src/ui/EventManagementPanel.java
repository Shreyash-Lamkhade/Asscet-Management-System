package ui;

import model.AppUser;
import model.Event;
import model.EventFeedbackSummary;
import model.EventReport;
import model.ExpenseItem;
import model.RegistrationRecord;
import service.EventService;
import service.AdminReportService;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JTabbedPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Date;

/**
 * Main CRUD panel for event management.
 */
public class EventManagementPanel extends JPanel {
    private final EventService eventService;
    private final AdminReportService adminReportService;
    private final MainFrame mainFrame;
    private final AppUser currentUser;

    private final JTextField searchField;
    private final JLabel selectedEventValue;

    private final DefaultTableModel tableModel;
    private final DefaultTableModel reportTableModel;
    private final DefaultTableModel registrationsTableModel;
    private final DefaultTableModel feedbackTableModel;
    private final DefaultTableModel expenseTableModel;
    private final DefaultTableModel longEventsTableModel;
    private final DefaultTableModel windowEventsTableModel;
    private final JTable eventTable;
    private final JTable reportTable;
    private final JTable registrationsTable;
    private final JTable feedbackTable;
    private final JTable expenseTable;
    private final JTable longEventsTable;
    private final JTable windowEventsTable;
    private final TableRowSorter<DefaultTableModel> sorter;

    private final JLabel totalEventsValue;
    private final JLabel totalParticipantsValue;
    private final JLabel totalSeatsValue;
    private final JLabel revenueValue;
    private final JLabel remainingBudgetValue;

    private int selectedEventId = -1;

    public EventManagementPanel(MainFrame mainFrame, AppUser currentUser) {
        this.eventService = new EventService();
        this.adminReportService = new AdminReportService();
        this.mainFrame = mainFrame;
        this.currentUser = currentUser;
        this.searchField = new JTextField(24);
        this.selectedEventValue = new JLabel("No event selected");

        this.tableModel = new DefaultTableModel(
            new Object[]{"ID", "Name", "Start", "End", "Venue", "Category", "Description", "Total Seats", "Booked", "Available", "Fee", "Budget", "Expenses", "Remaining Budget"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.reportTableModel = new DefaultTableModel(
            new Object[]{"Event", "Participants", "Remaining Seats", "Revenue", "Budget", "Expenses", "Remaining Budget"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.registrationsTableModel = new DefaultTableModel(
            new Object[]{"Reg ID", "User", "Email", "Event", "Fee", "Check-In", "Check-In Time"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.feedbackTableModel = new DefaultTableModel(
            new Object[]{"Event", "Feedback Count", "Avg Rating"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.expenseTableModel = new DefaultTableModel(
            new Object[]{"Expense ID", "Event", "Title", "Amount", "Spent On"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.longEventsTableModel = new DefaultTableModel(
            new Object[]{"Event ID", "Event", "Start", "End", "Days"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.windowEventsTableModel = new DefaultTableModel(
            new Object[]{"Day", "Event", "Venue", "Category"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        this.eventTable = new JTable(tableModel);
        this.reportTable = new JTable(reportTableModel);
        this.registrationsTable = new JTable(registrationsTableModel);
        this.feedbackTable = new JTable(feedbackTableModel);
        this.expenseTable = new JTable(expenseTableModel);
        this.longEventsTable = new JTable(longEventsTableModel);
        this.windowEventsTable = new JTable(windowEventsTableModel);
        this.sorter = new TableRowSorter<>(tableModel);
        this.totalEventsValue = createMetricValueLabel();
        this.totalParticipantsValue = createMetricValueLabel();
        this.totalSeatsValue = createMetricValueLabel();
        this.revenueValue = createMetricValueLabel();
        this.remainingBudgetValue = createMetricValueLabel();

        setLayout(new BorderLayout(18, 18));
        setBorder(new EmptyBorder(18, 18, 18, 18));
        setBackground(UIStyles.PAGE_BACKGROUND);

        JPanel contentPanel = new JPanel(new BorderLayout(18, 18));
        contentPanel.setOpaque(false);
        contentPanel.add(createHeaderPanel(), BorderLayout.NORTH);
        contentPanel.add(createCenterPanel(), BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(UIStyles.PAGE_BACKGROUND);

        add(scrollPane, BorderLayout.CENTER);

        loadTableData(eventService.getSafeEvents());
        updateDashboardCards();
        loadReportData();
        loadAdvancedData();
    }

    private JPanel createHeaderPanel() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 14));
        wrapper.setOpaque(false);

        JLabel title = new JLabel("Welcome, " + currentUser.getName() + " | Admin Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(UIStyles.TITLE_COLOR);
        wrapper.add(title, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 12));
        centerPanel.setOpaque(false);
        centerPanel.add(createDashboardPanel(), BorderLayout.CENTER);

        JButton logoutButton = createActionButton("Logout", new Color(220, 38, 38));
        logoutButton.addActionListener(event -> mainFrame.showLogin());

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        actionPanel.setOpaque(false);
        actionPanel.add(logoutButton);
        centerPanel.add(actionPanel, BorderLayout.SOUTH);

        wrapper.add(centerPanel, BorderLayout.CENTER);

        return wrapper;
    }

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 5, 14, 14));
        panel.setOpaque(false);
        panel.add(createMetricCard("Total Events", totalEventsValue, new Color(33, 150, 243)));
        panel.add(createMetricCard("Participants", totalParticipantsValue, new Color(156, 39, 176)));
        panel.add(createMetricCard("Available Seats", totalSeatsValue, new Color(76, 175, 80)));
        panel.add(createMetricCard("Potential Revenue", revenueValue, new Color(255, 152, 0)));
        panel.add(createMetricCard("Remaining Budget", remainingBudgetValue, new Color(0, 121, 107)));
        return panel;
    }

    private JPanel createMetricCard(String title, JLabel valueLabel, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 224, 230)),
            new EmptyBorder(16, 16, 16, 16)
        ));
        card.setBackground(UIStyles.PANEL_BACKGROUND);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleLabel.setForeground(accent.darker());
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private JLabel createMetricValueLabel() {
        JLabel label = new JLabel("0", SwingConstants.LEFT);
        label.setFont(new Font("Segoe UI", Font.BOLD, 24));
        label.setForeground(UIStyles.TITLE_COLOR);
        return label;
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(createFormPanel());
        panel.add(Box.createVerticalStrut(16));
        panel.add(createTablePanel());
        panel.add(Box.createVerticalStrut(16));
        panel.add(createReportPanel());
        panel.add(Box.createVerticalStrut(16));
        panel.add(createAdvancedPanel());
        return panel;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(UIStyles.PANEL_BACKGROUND);
        panel.setBorder(UIStyles.createPanelBorder());

        JLabel title = new JLabel("Admin Actions");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        panel.add(title, BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setOpaque(false);

        JPanel infoCard = new JPanel(new BorderLayout(0, 8));
        infoCard.setOpaque(false);
        infoCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(223, 227, 235)),
            new EmptyBorder(16, 16, 16, 16)
        ));

        JLabel infoTitle = new JLabel("Selected Event");
        infoTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        selectedEventValue.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        selectedEventValue.setForeground(UIStyles.TEXT_COLOR);

        infoCard.add(infoTitle, BorderLayout.NORTH);
        infoCard.add(selectedEventValue, BorderLayout.CENTER);

        content.add(infoCard, BorderLayout.NORTH);
        content.add(createQuickStatsPanel(), BorderLayout.CENTER);

        panel.add(content, BorderLayout.CENTER);
        panel.add(createButtonPanel(), BorderLayout.SOUTH);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        return panel;
    }

    private JPanel createQuickStatsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 12, 12);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(createMiniCard("Create"), gbc);

        gbc.gridx = 1;
        panel.add(createMiniCard("Modify"), gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(createMiniCard("Remove"), gbc);

        gbc.gridx = 1;
        panel.add(createMiniCard("Track"), gbc);
        return panel;
    }

    private JPanel createMiniCard(String title) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(new Color(248, 250, 252));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(223, 227, 235)),
            new EmptyBorder(14, 14, 14, 14)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLabel.setForeground(UIStyles.TITLE_COLOR);

        card.add(titleLabel, BorderLayout.NORTH);
        return card;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panel.setOpaque(false);

        JButton addButton = createActionButton("Add Event", new Color(33, 150, 243));
        JButton updateButton = createActionButton("Update Event", new Color(76, 175, 80));
        JButton deleteButton = createActionButton("Delete Event", new Color(244, 67, 54));
        JButton clearButton = createActionButton("Clear Form", new Color(96, 125, 139));

        addButton.addActionListener(event -> saveEvent());
        updateButton.addActionListener(event -> updateEvent());
        deleteButton.addActionListener(event -> deleteEvent());
        clearButton.addActionListener(event -> clearForm());

        panel.add(addButton);
        panel.add(updateButton);
        panel.add(deleteButton);
        panel.add(clearButton);
        return panel;
    }

    private JButton createActionButton(String text, Color color) {
        JButton button = new JButton(text);
        UIStyles.styleButton(button, color, 130);
        return button;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(UIStyles.PANEL_BACKGROUND);
        panel.setBorder(UIStyles.createPanelBorder());
        panel.setPreferredSize(new Dimension(0, 320));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 340));

        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        topPanel.setOpaque(false);

        JLabel title = new JLabel("Event List");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));

        JButton searchButton = createActionButton("Search", new Color(63, 81, 181));
        JButton refreshButton = createActionButton("Refresh", new Color(0, 150, 136));
        searchButton.setPreferredSize(new Dimension(100, 36));
        refreshButton.setPreferredSize(new Dimension(100, 36));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchPanel.setOpaque(false);
        searchField.setPreferredSize(new Dimension(220, 34));
        UIStyles.styleInput(searchField);
        searchPanel.add(new JLabel("Search"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(refreshButton);

        topPanel.add(title, BorderLayout.NORTH);
        topPanel.add(searchPanel, BorderLayout.SOUTH);

        UIStyles.styleTable(eventTable);
        eventTable.setRowSorter(sorter);
        eventTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        eventTable.getSelectionModel().addListSelectionListener(event -> populateSelectedEvent());

        searchButton.addActionListener(event -> searchEvents());
        refreshButton.addActionListener(event -> refreshTable());

        JScrollPane scrollPane = new JScrollPane(eventTable);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createReportPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(UIStyles.PANEL_BACKGROUND);
        panel.setBorder(UIStyles.createPanelBorder());
        panel.setPreferredSize(new Dimension(0, 260));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));

        JLabel title = new JLabel("Analytics And Reports");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));

        UIStyles.styleTable(reportTable);
        reportTable.setFillsViewportHeight(true);

        JLabel helper = new JLabel("Participants, revenue, seats, and budget values refresh from the database.");
        helper.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        helper.setForeground(UIStyles.TEXT_COLOR);

        JPanel top = new JPanel(new BorderLayout(0, 4));
        top.setOpaque(false);
        top.add(title, BorderLayout.NORTH);
        top.add(helper, BorderLayout.SOUTH);

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(reportTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createAdvancedPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(UIStyles.PANEL_BACKGROUND);
        panel.setBorder(UIStyles.createPanelBorder());
        panel.setPreferredSize(new Dimension(0, 420));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 460));

        JLabel title = new JLabel("Admin Tools");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabs.addTab("Attendance", createAttendanceTab());
        tabs.addTab("Feedback", createFeedbackTab());
        tabs.addTab("Expenses", createExpensesTab());
        tabs.addTab("Multi-day", createMultiDayTab());

        panel.add(title, BorderLayout.NORTH);
        panel.add(tabs, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createMultiDayTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        UIStyles.styleTable(longEventsTable);
        UIStyles.styleTable(windowEventsTable);

        JLabel leftTitle = new JLabel("Long Events");
        leftTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JLabel rightTitle = new JLabel("3-Day Schedule");
        rightTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JPanel left = new JPanel(new BorderLayout(0, 6));
        left.setOpaque(false);
        left.add(leftTitle, BorderLayout.NORTH);
        left.add(new JScrollPane(longEventsTable), BorderLayout.CENTER);

        JPanel right = new JPanel(new BorderLayout(0, 6));
        right.setOpaque(false);
        right.add(rightTitle, BorderLayout.NORTH);
        right.add(new JScrollPane(windowEventsTable), BorderLayout.CENTER);

        JPanel center = new JPanel(new GridLayout(1, 2, 12, 0));
        center.setOpaque(false);
        center.add(left);
        center.add(right);

        JButton refresh = createActionButton("Refresh", new Color(0, 150, 136));
        refresh.addActionListener(e -> loadAdvancedData());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(refresh);

        longEventsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadThreeDayWindowForSelectedLongEvent();
            }
        });

        panel.add(actions, BorderLayout.NORTH);
        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createAttendanceTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        UIStyles.styleTable(registrationsTable);
        registrationsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JButton checkIn = createActionButton("Check-In", new Color(76, 175, 80));
        JButton undo = createActionButton("Undo", new Color(96, 125, 139));
        JButton refresh = createActionButton("Refresh", new Color(0, 150, 136));

        checkIn.addActionListener(e -> checkInSelectedRegistration());
        undo.addActionListener(e -> undoCheckInSelectedRegistration());
        refresh.addActionListener(e -> loadAdvancedData());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(refresh);
        actions.add(undo);
        actions.add(checkIn);

        panel.add(actions, BorderLayout.NORTH);
        panel.add(new JScrollPane(registrationsTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createFeedbackTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        UIStyles.styleTable(feedbackTable);
        JButton refresh = createActionButton("Refresh", new Color(0, 150, 136));
        refresh.addActionListener(e -> loadAdvancedData());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(refresh);

        panel.add(actions, BorderLayout.NORTH);
        panel.add(new JScrollPane(feedbackTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createExpensesTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        UIStyles.styleTable(expenseTable);

        JButton add = createActionButton("Add Expense", new Color(33, 150, 243));
        JButton delete = createActionButton("Delete", new Color(244, 67, 54));
        JButton refresh = createActionButton("Refresh", new Color(0, 150, 136));

        add.addActionListener(e -> openAddExpenseDialog());
        delete.addActionListener(e -> deleteSelectedExpense());
        refresh.addActionListener(e -> loadAdvancedData());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(refresh);
        actions.add(delete);
        actions.add(add);

        panel.add(actions, BorderLayout.NORTH);
        panel.add(new JScrollPane(expenseTable), BorderLayout.CENTER);
        return panel;
    }

    private void saveEvent() {
        openEventDialog(null);
    }

    private void updateEvent() {
        if (selectedEventId == -1) {
            showError("Select an event from the table first.");
            return;
        }
        openEventDialog(getSelectedEventFromTable());
    }

    private void deleteEvent() {
        if (selectedEventId == -1) {
            showError("Select an event from the table first.");
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
            this,
            "Delete the selected event?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION
        );

        if (choice == JOptionPane.YES_OPTION) {
            boolean deleted = eventService.deleteEvent(selectedEventId);
            if (deleted) {
                JOptionPane.showMessageDialog(this, "Event deleted successfully.");
                refreshTable();
                clearForm();
            } else {
                showError("Unable to delete event.");
            }
        }
    }

    private void searchEvents() {
        String keyword = searchField.getText().trim();
        List<Event> events = eventService.searchEvents(keyword);
        loadTableData(events);
    }

    private void refreshTable() {
        loadTableData(eventService.getSafeEvents());
        updateDashboardCards();
        loadReportData();
        loadAdvancedData();
    }

    private void loadTableData(List<Event> events) {
        tableModel.setRowCount(0);

        for (Event event : events) {
            int bookedSeats = event.getTotalSeats() - event.getAvailableSeats();
            LocalDate start = event.getStartDate() != null ? event.getStartDate() : event.getEventDate();
            LocalDate end = event.getEndDate() != null ? event.getEndDate() : start;
            tableModel.addRow(new Object[]{
                event.getEventId(),
                event.getName(),
                start,
                end,
                event.getVenue(),
                event.getCategory(),
                event.getDescription(),
                event.getTotalSeats(),
                bookedSeats,
                event.getAvailableSeats(),
                event.getFee(),
                event.getTotalBudget(),
                event.getExpenses(),
                event.getRemainingBudget()
            });
        }
    }

    private void updateDashboardCards() {
        List<Event> events = eventService.getSafeEvents();
        totalEventsValue.setText(String.valueOf(events.size()));
        totalParticipantsValue.setText(String.valueOf(eventService.getTotalParticipants()));
        totalSeatsValue.setText(String.valueOf(eventService.getTotalAvailableSeats()));
        revenueValue.setText(calculatePotentialRevenue(events));
        remainingBudgetValue.setText("Rs. " + eventService.getTotalRemainingBudget());
    }

    private void loadReportData() {
        reportTableModel.setRowCount(0);
        List<EventReport> reports = eventService.getEventReports();
        if (reports.isEmpty()) {
            reportTableModel.addRow(new Object[]{
                "No event data yet",
                0,
                0,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO
            });
            return;
        }

        for (EventReport report : reports) {
            reportTableModel.addRow(new Object[]{
                report.getEventName(),
                report.getParticipantCount(),
                report.getRemainingSeats(),
                report.getRevenue(),
                report.getBudget(),
                report.getExpenses(),
                report.getRemainingBudget()
            });
        }
    }

    private void loadAdvancedData() {
        loadRegistrationsTable();
        loadFeedbackTable();
        loadExpenseTable();
        loadLongEvents();
    }

    private void loadLongEvents() {
        longEventsTableModel.setRowCount(0);
        List<Event> longEvents = eventService.getLongEvents(3);
        for (Event event : longEvents) {
            LocalDate start = event.getStartDate() != null ? event.getStartDate() : event.getEventDate();
            LocalDate end = event.getEndDate() != null ? event.getEndDate() : start;
            longEventsTableModel.addRow(new Object[]{
                event.getEventId(),
                event.getName(),
                start,
                end,
                event.getDurationDays()
            });
        }
        windowEventsTableModel.setRowCount(0);
    }

    private void loadThreeDayWindowForSelectedLongEvent() {
        int row = longEventsTable.getSelectedRow();
        if (row == -1) {
            return;
        }
        int modelRow = longEventsTable.convertRowIndexToModel(row);
        int eventId = Integer.parseInt(longEventsTableModel.getValueAt(modelRow, 0).toString());

        Event selected = null;
        for (Event e : eventService.getSafeEvents()) {
            if (e.getEventId() == eventId) {
                selected = e;
                break;
            }
        }
        if (selected == null) {
            return;
        }

        LocalDate start = selected.getStartDate() != null ? selected.getStartDate() : selected.getEventDate();
        windowEventsTableModel.setRowCount(0);

        for (int i = 0; i < 3; i++) {
            LocalDate day = start.plusDays(i);
            List<Event> dayEvents = eventService.getEventsOverlapping(day, day);
            if (dayEvents.isEmpty()) {
                windowEventsTableModel.addRow(new Object[]{day, "-", "-", "-"});
                continue;
            }
            for (Event e : dayEvents) {
                windowEventsTableModel.addRow(new Object[]{day, e.getName(), e.getVenue(), e.getCategory()});
            }
        }
    }

    private void loadRegistrationsTable() {
        registrationsTableModel.setRowCount(0);
        for (RegistrationRecord record : adminReportService.getRegistrationRecords()) {
            registrationsTableModel.addRow(new Object[]{
                record.getRegId(),
                record.getUserName(),
                record.getEmail(),
                record.getEventName(),
                record.getEventFee(),
                record.getCheckIn(),
                record.getCheckInTime()
            });
        }
    }

    private void loadFeedbackTable() {
        feedbackTableModel.setRowCount(0);
        for (EventFeedbackSummary summary : adminReportService.getFeedbackSummary()) {
            feedbackTableModel.addRow(new Object[]{
                summary.getEventName(),
                summary.getFeedbackCount(),
                String.format("%.2f", summary.getAvgRating())
            });
        }
    }

    private void loadExpenseTable() {
        expenseTableModel.setRowCount(0);
        for (ExpenseItem item : adminReportService.getExpenseItems()) {
            expenseTableModel.addRow(new Object[]{
                item.getExpenseId(),
                item.getEventName(),
                item.getTitle(),
                item.getAmount(),
                item.getSpentOn()
            });
        }
    }

    private Integer getSelectedRegistrationId() {
        int row = registrationsTable.getSelectedRow();
        if (row == -1) {
            return null;
        }
        int modelRow = registrationsTable.convertRowIndexToModel(row);
        return Integer.parseInt(registrationsTableModel.getValueAt(modelRow, 0).toString());
    }

    private void checkInSelectedRegistration() {
        Integer regId = getSelectedRegistrationId();
        if (regId == null) {
            showError("Select a registration first.");
            return;
        }
        boolean ok = adminReportService.checkIn(regId);
        if (ok) {
            JOptionPane.showMessageDialog(this, "Checked in.");
            loadAdvancedData();
        } else {
            showError("Unable to check in.");
        }
    }

    private void undoCheckInSelectedRegistration() {
        Integer regId = getSelectedRegistrationId();
        if (regId == null) {
            showError("Select a registration first.");
            return;
        }
        boolean ok = adminReportService.undoCheckIn(regId);
        if (ok) {
            JOptionPane.showMessageDialog(this, "Check-in removed.");
            loadAdvancedData();
        } else {
            showError("Unable to update check-in.");
        }
    }

    private void openAddExpenseDialog() {
        String eventIdText = JOptionPane.showInputDialog(this, "Event ID:");
        if (eventIdText == null) {
            return;
        }
        String title = JOptionPane.showInputDialog(this, "Expense title:");
        if (title == null) {
            return;
        }
        String amountText = JOptionPane.showInputDialog(this, "Amount:");
        if (amountText == null) {
            return;
        }
        try {
            int eventId = Integer.parseInt(eventIdText.trim());
            BigDecimal amount = new BigDecimal(amountText.trim());
            boolean ok = adminReportService.addExpense(eventId, title.trim(), amount, new java.sql.Date(System.currentTimeMillis()));
            if (ok) {
                JOptionPane.showMessageDialog(this, "Expense added.");
                loadAdvancedData();
                loadReportData();
                updateDashboardCards();
            } else {
                showError("Unable to add expense.");
            }
        } catch (Exception ex) {
            showError("Enter valid Event ID and amount.");
        }
    }

    private void deleteSelectedExpense() {
        int row = expenseTable.getSelectedRow();
        if (row == -1) {
            showError("Select an expense first.");
            return;
        }
        int modelRow = expenseTable.convertRowIndexToModel(row);
        int expenseId = Integer.parseInt(expenseTableModel.getValueAt(modelRow, 0).toString());

        int choice = JOptionPane.showConfirmDialog(this, "Delete selected expense?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        boolean ok = adminReportService.deleteExpense(expenseId);
        if (ok) {
            JOptionPane.showMessageDialog(this, "Expense deleted.");
            loadAdvancedData();
        } else {
            showError("Unable to delete expense.");
        }
    }

    private String calculatePotentialRevenue(List<Event> events) {
        BigDecimal total = BigDecimal.ZERO;
        for (Event event : events) {
            int bookedSeats = event.getTotalSeats() - event.getAvailableSeats();
            total = total.add(event.getFee().multiply(BigDecimal.valueOf(bookedSeats)));
        }
        return "Rs. " + total;
    }

    private void populateSelectedEvent() {
        int selectedRow = eventTable.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }

        int modelRow = eventTable.convertRowIndexToModel(selectedRow);
        selectedEventId = Integer.parseInt(tableModel.getValueAt(modelRow, 0).toString());
        selectedEventValue.setText("<html><b>" + tableModel.getValueAt(modelRow, 1)
            + "</b><br>Start: " + tableModel.getValueAt(modelRow, 2)
            + " | End: " + tableModel.getValueAt(modelRow, 3)
            + "<br>Venue: " + tableModel.getValueAt(modelRow, 4)
            + "<br>Seats: " + tableModel.getValueAt(modelRow, 9) + "/" + tableModel.getValueAt(modelRow, 7)
            + " available</html>");
    }

    private void clearForm() {
        selectedEventId = -1;
        selectedEventValue.setText("No event selected");
        eventTable.clearSelection();
        sorter.setRowFilter((RowFilter<? super DefaultTableModel, ? super Integer>) null);
    }

    private Event getSelectedEventFromTable() {
        if (selectedEventId == -1) {
            return null;
        }
        for (Event event : eventService.getSafeEvents()) {
            if (event.getEventId() == selectedEventId) {
                return event;
            }
        }
        return null;
    }

    private void openEventDialog(Event existingEvent) {
        JDialog dialog = new JDialog(mainFrame, existingEvent == null ? "Add Event" : "Update Event", true);
        dialog.setSize(560, 560);
        dialog.setLocationRelativeTo(this);

        JTextField nameField = new JTextField();
        JTextArea descriptionArea = new JTextArea(5, 20);
        UIStyles.styleTextArea(descriptionArea);
        JSpinner startSpinner = new JSpinner(new SpinnerDateModel());
        startSpinner.setEditor(new JSpinner.DateEditor(startSpinner, "yyyy-MM-dd"));
        JSpinner endSpinner = new JSpinner(new SpinnerDateModel());
        endSpinner.setEditor(new JSpinner.DateEditor(endSpinner, "yyyy-MM-dd"));
        JTextField venueField = new JTextField();
        JComboBox<String> categoryBox = new JComboBox<>(new String[]{"Tech", "Cultural", "Sports", "Workshop", "Seminar"});
        JTextField totalSeatsField = new JTextField();
        JTextField availableSeatsField = new JTextField();
        JTextField feeField = new JTextField();
        JTextField budgetField = new JTextField();
        JTextField expensesField = new JTextField("0");

        if (existingEvent != null) {
            nameField.setText(existingEvent.getName());
            descriptionArea.setText(existingEvent.getDescription());
            LocalDate start = existingEvent.getStartDate() != null ? existingEvent.getStartDate() : existingEvent.getEventDate();
            LocalDate end = existingEvent.getEndDate() != null ? existingEvent.getEndDate() : start;
            startSpinner.setValue(Date.from(start.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            endSpinner.setValue(Date.from(end.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            venueField.setText(existingEvent.getVenue());
            categoryBox.setSelectedItem(existingEvent.getCategory());
            totalSeatsField.setText(String.valueOf(existingEvent.getTotalSeats()));
            availableSeatsField.setText(String.valueOf(existingEvent.getAvailableSeats()));
            feeField.setText(existingEvent.getFee().toString());
            budgetField.setText(existingEvent.getTotalBudget().toString());
            expensesField.setText(existingEvent.getExpenses().toString());
        } else {
            startSpinner.setValue(new Date());
            endSpinner.setValue(new Date());
        }

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(16, 16, 16, 16));
        formPanel.setBackground(UIStyles.PANEL_BACKGROUND);

        addDialogField(formPanel, "Event Name", nameField, 0, 0);
        addDialogField(formPanel, "Start Date", startSpinner, 0, 1);
        addDialogField(formPanel, "Venue", venueField, 1, 0);
        addDialogField(formPanel, "Category", categoryBox, 1, 1);
        addDialogField(formPanel, "Total Seats", totalSeatsField, 2, 0);
        addDialogField(formPanel, "Available Seats", availableSeatsField, 2, 1);
        addDialogField(formPanel, "Registration Fee", feeField, 3, 0);
        addDialogField(formPanel, "Total Budget", budgetField, 3, 1);
        addDialogField(formPanel, "Expenses", expensesField, 4, 0);
        addDialogField(formPanel, "End Date", endSpinner, 4, 1);
        addDialogField(formPanel, "Description", new JScrollPane(descriptionArea), 5, 0);

        JButton saveButton = createActionButton(existingEvent == null ? "Add Event" : "Update Event", new Color(33, 150, 243));
        JButton cancelButton = createActionButton("Cancel", new Color(96, 125, 139));

        saveButton.addActionListener(event -> {
            Event formEvent = buildEventFromDialog(
                nameField, descriptionArea, startSpinner, endSpinner, venueField, categoryBox,
                totalSeatsField, availableSeatsField, feeField, budgetField, expensesField
            );
            if (formEvent == null) {
                return;
            }

            String message;
            if (existingEvent == null) {
                message = eventService.createEvent(formEvent);
            } else {
                formEvent.setEventId(existingEvent.getEventId());
                message = eventService.updateEvent(formEvent);
            }

            if (message == null) {
                JOptionPane.showMessageDialog(dialog, existingEvent == null ? "Event added successfully." : "Event updated successfully.");
                dialog.dispose();
                refreshTable();
                clearForm();
            } else {
                showError(message);
            }
        });

        cancelButton.addActionListener(event -> dialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(UIStyles.PANEL_BACKGROUND);
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        dialog.setLayout(new BorderLayout());
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void addDialogField(JPanel parent, String labelText, java.awt.Component component, int row, int column) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = column;
        gbc.gridy = row;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(0, 0, 12, column == 0 ? 12 : 0);

        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setForeground(UIStyles.TEXT_COLOR);
        panel.add(label, BorderLayout.NORTH);

        if (component instanceof JTextField || component instanceof JComboBox || component instanceof JSpinner) {
            UIStyles.styleInput((javax.swing.JComponent) component);
        }
        panel.add(component, BorderLayout.CENTER);
        parent.add(panel, gbc);
    }

    private Event buildEventFromDialog(JTextField nameField, JTextArea descriptionArea, JSpinner startSpinner,
                                       JSpinner endSpinner, JTextField venueField, JComboBox<String> categoryBox, JTextField totalSeatsField,
                                       JTextField availableSeatsField, JTextField feeField, JTextField budgetField,
                                       JTextField expensesField) {
        try {
            Event event = new Event();
            event.setName(nameField.getText().trim());
            event.setDescription(descriptionArea.getText().trim());
            LocalDate start = Instant.ofEpochMilli(((Date) startSpinner.getValue()).getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
            LocalDate end = Instant.ofEpochMilli(((Date) endSpinner.getValue()).getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
            if (end.isBefore(start)) {
                showError("End date cannot be before start date.");
                return null;
            }
            event.setStartDate(start);
            event.setEndDate(end);
            event.setEventDate(start);
            event.setVenue(venueField.getText().trim());
            event.setCategory(categoryBox.getSelectedItem().toString());
            event.setTotalSeats(Integer.parseInt(totalSeatsField.getText().trim()));
            event.setAvailableSeats(Integer.parseInt(availableSeatsField.getText().trim()));
            event.setFee(new BigDecimal(feeField.getText().trim()));
            event.setTotalBudget(new BigDecimal(budgetField.getText().trim()));
            event.setExpenses(new BigDecimal(expensesField.getText().trim()));
            return event;
        } catch (NumberFormatException exception) {
            showError("Seats, fee, budget, and expenses must be valid numbers.");
            return null;
        }
    }

    private void showError(String message) {
        UIManager.getLookAndFeel().provideErrorFeedback(this);
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.ERROR_MESSAGE);
    }
}
