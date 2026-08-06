package dao;

import db.DBConnection;
import model.Event;
import model.EventReport;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

/**
 * Handles all database operations related to events.
 */
public class EventDAO {

    public boolean addEvent(Event event) {
        String sql = "INSERT INTO events (name, description, event_date, start_date, end_date, venue, category, total_seats, available_seats, fee, total_budget, expenses) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            java.time.LocalDate start = event.getStartDate() != null ? event.getStartDate() : event.getEventDate();
            java.time.LocalDate end = event.getEndDate() != null ? event.getEndDate() : start;

            statement.setString(1, event.getName());
            statement.setString(2, event.getDescription());
            statement.setDate(3, Date.valueOf(start));
            statement.setDate(4, Date.valueOf(start));
            statement.setDate(5, Date.valueOf(end));
            statement.setString(6, event.getVenue());
            statement.setString(7, event.getCategory());
            statement.setInt(8, event.getTotalSeats());
            statement.setInt(9, event.getAvailableSeats());
            statement.setBigDecimal(10, event.getFee());
            statement.setBigDecimal(11, event.getTotalBudget());
            statement.setBigDecimal(12, event.getExpenses());

            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            exception.printStackTrace();
            return false;
        }
    }

    public boolean updateEvent(Event event) {
        String sql = "UPDATE events SET name = ?, description = ?, event_date = ?, start_date = ?, end_date = ?, venue = ?, category = ?, "
                   + "total_seats = ?, available_seats = ?, fee = ?, total_budget = ?, expenses = ? WHERE event_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            java.time.LocalDate start = event.getStartDate() != null ? event.getStartDate() : event.getEventDate();
            java.time.LocalDate end = event.getEndDate() != null ? event.getEndDate() : start;

            statement.setString(1, event.getName());
            statement.setString(2, event.getDescription());
            statement.setDate(3, Date.valueOf(start));
            statement.setDate(4, Date.valueOf(start));
            statement.setDate(5, Date.valueOf(end));
            statement.setString(6, event.getVenue());
            statement.setString(7, event.getCategory());
            statement.setInt(8, event.getTotalSeats());
            statement.setInt(9, event.getAvailableSeats());
            statement.setBigDecimal(10, event.getFee());
            statement.setBigDecimal(11, event.getTotalBudget());
            statement.setBigDecimal(12, event.getExpenses());
            statement.setInt(13, event.getEventId());

            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            exception.printStackTrace();
            return false;
        }
    }

    public boolean deleteEvent(int eventId) {
        String sql = "DELETE FROM events WHERE event_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, eventId);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            exception.printStackTrace();
            return false;
        }
    }

    public List<Event> getAllEvents() {
        String sql = "SELECT * FROM events ORDER BY start_date ASC";
        List<Event> events = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                events.add(mapRow(resultSet));
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        return events;
    }

    public List<Event> searchEvents(String keyword) {
        String sql = "SELECT * FROM events WHERE name LIKE ? OR category LIKE ? OR venue LIKE ? ORDER BY start_date ASC";
        List<Event> events = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            String pattern = "%" + keyword + "%";
            statement.setString(1, pattern);
            statement.setString(2, pattern);
            statement.setString(3, pattern);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    events.add(mapRow(resultSet));
                }
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        return events;
    }

    public List<EventReport> getEventReports() {
        String sql = "SELECT e.name, e.start_date, e.available_seats, e.total_seats, e.fee, e.total_budget, e.expenses, "
                   + "COUNT(r.reg_id) AS participant_count "
                   + "FROM events e LEFT JOIN registrations r ON e.event_id = r.event_id "
                   + "GROUP BY e.event_id, e.name, e.start_date, e.available_seats, e.total_seats, e.fee, e.total_budget, e.expenses "
                   + "ORDER BY e.start_date ASC";

        List<EventReport> reports = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                EventReport report = new EventReport();
                int participants = resultSet.getInt("participant_count");
                BigDecimal fee = resultSet.getBigDecimal("fee");

                report.setEventName(resultSet.getString("name"));
                report.setParticipantCount(participants);
                report.setRemainingSeats(resultSet.getInt("available_seats"));
                report.setRevenue(fee.multiply(BigDecimal.valueOf(participants)));
                report.setBudget(resultSet.getBigDecimal("total_budget"));
                report.setExpenses(resultSet.getBigDecimal("expenses"));
                report.setRemainingBudget(report.getBudget().subtract(report.getExpenses()));
                reports.add(report);
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        return reports;
    }

    public List<Event> getLongEvents(int minDays) {
        String sql = "SELECT * FROM events WHERE (end_date - start_date + 1) > ? ORDER BY start_date ASC";
        List<Event> events = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, minDays);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    events.add(mapRow(resultSet));
                }
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return events;
    }

    public List<Event> getEventsOverlapping(LocalDate from, LocalDate to) {
        String sql = "SELECT * FROM events WHERE start_date <= ? AND end_date >= ? ORDER BY start_date ASC";
        List<Event> events = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setDate(1, Date.valueOf(to));
            statement.setDate(2, Date.valueOf(from));

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    events.add(mapRow(resultSet));
                }
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        return events;
    }

    private Event mapRow(ResultSet resultSet) throws SQLException {
        Event event = new Event();
        event.setEventId(resultSet.getInt("event_id"));
        event.setName(resultSet.getString("name"));
        event.setDescription(resultSet.getString("description"));
        event.setEventDate(resultSet.getDate("event_date").toLocalDate());
        event.setStartDate(resultSet.getDate("start_date") == null ? null : resultSet.getDate("start_date").toLocalDate());
        event.setEndDate(resultSet.getDate("end_date") == null ? null : resultSet.getDate("end_date").toLocalDate());
        event.setVenue(resultSet.getString("venue"));
        event.setCategory(resultSet.getString("category"));
        event.setTotalSeats(resultSet.getInt("total_seats"));
        event.setAvailableSeats(resultSet.getInt("available_seats"));
        event.setFee(resultSet.getBigDecimal("fee"));
        event.setTotalBudget(resultSet.getBigDecimal("total_budget"));
        event.setExpenses(resultSet.getBigDecimal("expenses"));
        return event;
    }
}
