package service;

import dao.AdminReportDAO;
import model.EventFeedbackSummary;
import model.ExpenseItem;
import model.RegistrationRecord;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.Collections;
import java.util.List;

public class AdminReportService {
    private final AdminReportDAO adminReportDAO;

    public AdminReportService() {
        this.adminReportDAO = new AdminReportDAO();
    }

    public List<RegistrationRecord> getRegistrationRecords() {
        List<RegistrationRecord> records = adminReportDAO.getRegistrationRecords();
        return records == null ? Collections.emptyList() : records;
    }

    public boolean checkIn(int regId) {
        return adminReportDAO.checkIn(regId);
    }

    public boolean undoCheckIn(int regId) {
        return adminReportDAO.undoCheckIn(regId);
    }

    public boolean addFeedback(int userId, int eventId, int rating, String comment) {
        return adminReportDAO.addFeedback(userId, eventId, rating, comment);
    }

    public List<EventFeedbackSummary> getFeedbackSummary() {
        List<EventFeedbackSummary> summaries = adminReportDAO.getFeedbackSummary();
        return summaries == null ? Collections.emptyList() : summaries;
    }

    public boolean addExpense(int eventId, String title, BigDecimal amount, Date spentOn) {
        return adminReportDAO.addExpense(eventId, title, amount, spentOn);
    }

    public boolean deleteExpense(int expenseId) {
        return adminReportDAO.deleteExpense(expenseId);
    }

    public List<ExpenseItem> getExpenseItems() {
        List<ExpenseItem> items = adminReportDAO.getExpenseItems();
        return items == null ? Collections.emptyList() : items;
    }
}
