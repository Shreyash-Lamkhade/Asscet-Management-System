package ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

/**
 * Shared style helpers for a consistent Swing UI.
 */
public final class UIStyles {
    public static final Color PAGE_BACKGROUND = new Color(243, 246, 251);
    public static final Color PANEL_BACKGROUND = Color.WHITE;
    public static final Color BORDER_COLOR = new Color(214, 220, 229);
    public static final Color TITLE_COLOR = new Color(17, 24, 39);
    public static final Color TEXT_COLOR = new Color(75, 85, 99);

    private UIStyles() {
    }

    public static Border createPanelBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)
        );
    }

    public static void styleInput(JComponent component) {
        component.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        component.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225)),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        component.setPreferredSize(new Dimension(220, 36));
    }

    public static void styleTextArea(JTextArea area) {
        area.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225)),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
    }

    public static void styleButton(JButton button, Color color, int width) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setPreferredSize(new Dimension(width, 38));
    }

    public static void styleTable(JTable table) {
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(233, 239, 248));
        table.getTableHeader().setForeground(TITLE_COLOR);
        table.setGridColor(new Color(228, 232, 237));
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setSelectionBackground(new Color(206, 225, 255));
        table.setSelectionForeground(TITLE_COLOR);
    }
}
