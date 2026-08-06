package dao;

import db.DBConnection;
import model.EventFeedbackSummary;
import model.ExpenseItem;
import model.RegistrationRecord;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Admin-only reporting + updates for attendance/feedback/expenses.
 */
public class AdminReportDAO {

    public List<RegistrationRecord> getRegistrationRecords() {
        String sql = "SELECT r.reg_id, u.user_id, u.name AS user_name, u.email, " +
                     "e.event_id, e.name AS event_name, e.fee, " +
                     "NVL(r.check_in, 'N') AS check_in, r.check_in_time " +
                     "FROM registrations r " +
                     "JOIN users u ON u.user_id = r.user_id " +
                     "JOIN events e ON e.event_id = r.event_id " +
                     "ORDER BY e.event_date ASC, e.name ASC";

        List<RegistrationRecord> records = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                RegistrationRecord record = new RegistrationRecord();
                record.setRegId(resultSet.getInt("reg_id"));
                record.setUserId(resultSet.getInt("user_id"));
                record.setUserName(resultSet.getString("user_name"));
                record.setEmail(resultSet.getString("email"));
                record.setEventId(resultSet.getInt("event_id"));
                record.setEventName(resultSet.getString("event_name"));
                record.setEventFee(resultSet.getBigDecimal("fee"));
                record.setCheckIn(resultSet.getString("check_in"));
                Timestamp checkInTime = resultSet.getTimestamp("check_in_time");
                record.setCheckInTime(checkInTime == null ? null : new Date(checkInTime.getTime()));
                records.add(record);
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        return records;
    }

    public boolean checkIn(int regId) {
        String sql = "UPDATE registrations SET check_in = 'Y', check_in_time = SYSDATE WHERE reg_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, regId);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            exception.printStackTrace();
            return false;
        }
    }

    public boolean undoCheckIn(int regId) {
        String sql = "UPDATE registrations SET check_in = 'N', check_in_time = NULL WHERE reg_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, regId);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            exception.printStackTrace();
            return false;
        }
    }

    public boolean addFeedback(int userId, int eventId, int rating, String comment) {
        String sql = "INSERT INTO feedback (user_id, event_id, rating, feedback_text) VALUES (?, ?, ?, ?)";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setInt(2, eventId);
            statement.setInt(3, rating);
            statement.setString(4, comment);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            exception.printStackTrace();
            return false;
        }
    }

    public List<EventFeedbackSummary> getFeedbackSummary() {
        String sql = "SELECT e.name AS event_name, COUNT(f.feedback_id) AS feedback_count, " +
                     "NVL(AVG(f.rating), 0) AS avg_rating " +
                     "FROM events e " +
                     "LEFT JOIN feedback f ON f.event_id = e.event_id " +
                     "GROUP BY e.event_id, e.name " +
                     "ORDER BY avg_rating DESC, feedback_count DESC";

        List<EventFeedbackSummary> summaries = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                EventFeedbackSummary summary = new EventFeedbackSummary();
                summary.setEventName(resultSet.getString("event_name"));
                summary.setFeedbackCount(resultSet.getInt("feedback_count"));
                summary.setAvgRating(resultSet.getDouble("avg_rating"));
                summaries.add(summary);
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        return summaries;
    }

    public boolean addExpense(int eventId, String title, BigDecimal amount, java.sql.Date spentOn) {
        String sql = "INSERT INTO expense_items (event_id, title, amount, spent_on) VALUES (?, ?, ?, ?)";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, eventId);
            statement.setString(2, title);
            statement.setBigDecimal(3, amount);
            statement.setDate(4, spentOn);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            exception.printStackTrace();
            return false;
        }
    }

    public boolean deleteExpense(int expenseId) {
        String sql = "DELETE FROM expense_items WHERE expense_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, expenseId);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            exception.printStackTrace();
            return false;
        }
    }

    public List<ExpenseItem> getExpenseItems() {
        String sql = "SELECT x.expense_id, x.event_id, e.name AS event_name, x.title, x.amount, x.spent_on " +
                     "FROM expense_items x JOIN events e ON e.event_id = x.event_id " +
                     "ORDER BY x.spent_on DESC, x.expense_id DESC";
        List<ExpenseItem> items = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                ExpenseItem item = new ExpenseItem();
                item.setExpenseId(resultSet.getInt("expense_id"));
                item.setEventId(resultSet.getInt("event_id"));
                item.setEventName(resultSet.getString("event_name"));
                item.setTitle(resultSet.getString("title"));
                item.setAmount(resultSet.getBigDecimal("amount"));
                java.sql.Date spentOn = resultSet.getDate("spent_on");
                item.setSpentOn(spentOn == null ? null : new Date(spentOn.getTime()));
                items.add(item);
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        return items;
    }
}
