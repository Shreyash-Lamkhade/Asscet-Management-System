package dao;

import db.DBConnection;
import model.Event;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles event registration database operations.
 */
public class RegistrationDAO {

    public boolean registerForEvent(int userId, int eventId) {
        String insertRegistration = "INSERT INTO registrations (user_id, event_id) VALUES (?, ?)";
        String reduceSeat = "UPDATE events SET available_seats = available_seats - 1 WHERE event_id = ? AND available_seats > 0";

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);

            if (isAlreadyRegistered(connection, userId, eventId)) {
                connection.rollback();
                return false;
            }

            try (PreparedStatement seatStatement = connection.prepareStatement(reduceSeat)) {
                seatStatement.setInt(1, eventId);
                int updatedRows = seatStatement.executeUpdate();
                if (updatedRows == 0) {
                    connection.rollback();
                    return false;
                }
            }

            try (PreparedStatement registrationStatement = connection.prepareStatement(insertRegistration)) {
                registrationStatement.setInt(1, userId);
                registrationStatement.setInt(2, eventId);
                registrationStatement.executeUpdate();
            }

            connection.commit();
            return true;
        } catch (SQLException exception) {
            exception.printStackTrace();
            return false;
        }
    }

    public boolean cancelRegistration(int userId, int eventId) {
        String deleteRegistration = "DELETE FROM registrations WHERE user_id = ? AND event_id = ?";
        String increaseSeat = "UPDATE events SET available_seats = available_seats + 1 WHERE event_id = ?";

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);

            int deletedRows;
            try (PreparedStatement deleteStatement = connection.prepareStatement(deleteRegistration)) {
                deleteStatement.setInt(1, userId);
                deleteStatement.setInt(2, eventId);
                deletedRows = deleteStatement.executeUpdate();
            }

            if (deletedRows == 0) {
                connection.rollback();
                return false;
            }

            try (PreparedStatement seatStatement = connection.prepareStatement(increaseSeat)) {
                seatStatement.setInt(1, eventId);
                seatStatement.executeUpdate();
            }

            connection.commit();
            return true;
        } catch (SQLException exception) {
            exception.printStackTrace();
            return false;
        }
    }

    public boolean isRegistered(int userId, int eventId) {
        try (Connection connection = DBConnection.getConnection()) {
            return isAlreadyRegistered(connection, userId, eventId);
        } catch (SQLException exception) {
            exception.printStackTrace();
            return false;
        }
    }

    public List<Integer> getRegisteredEventIds(int userId) {
        String sql = "SELECT event_id FROM registrations WHERE user_id = ?";
        List<Integer> eventIds = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    eventIds.add(resultSet.getInt("event_id"));
                }
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        return eventIds;
    }

    public List<Event> getRegisteredEvents(int userId) {
        String sql = "SELECT e.* FROM events e INNER JOIN registrations r ON e.event_id = r.event_id WHERE r.user_id = ? ORDER BY e.event_date ASC";
        List<Event> events = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Event event = new Event();
                    event.setEventId(resultSet.getInt("event_id"));
                    event.setName(resultSet.getString("name"));
                    event.setDescription(resultSet.getString("description"));
                    event.setEventDate(resultSet.getDate("event_date").toLocalDate());
                    event.setVenue(resultSet.getString("venue"));
                    event.setCategory(resultSet.getString("category"));
                    event.setTotalSeats(resultSet.getInt("total_seats"));
                    event.setAvailableSeats(resultSet.getInt("available_seats"));
                    event.setFee(resultSet.getBigDecimal("fee"));
                    event.setTotalBudget(resultSet.getBigDecimal("total_budget"));
                    event.setExpenses(resultSet.getBigDecimal("expenses"));
                    events.add(event);
                }
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        return events;
    }

    private boolean isAlreadyRegistered(Connection connection, int userId, int eventId) throws SQLException {
        String sql = "SELECT reg_id FROM registrations WHERE user_id = ? AND event_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setInt(2, eventId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }
}
