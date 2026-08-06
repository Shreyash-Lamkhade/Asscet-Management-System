package service;

import dao.EventDAO;
import model.Event;
import model.EventReport;
import util.EventValidator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * Contains business logic between the UI and DAO layers.
 */
public class EventService {
    private final EventDAO eventDAO;

    public EventService() {
        this.eventDAO = new EventDAO();
    }

    public String createEvent(Event event) {
        String validationMessage = EventValidator.validate(event);
        if (validationMessage != null) {
            return validationMessage;
        }
        return eventDAO.addEvent(event) ? null : "Unable to save event.";
    }

    public String updateEvent(Event event) {
        String validationMessage = EventValidator.validate(event);
        if (validationMessage != null) {
            return validationMessage;
        }
        return eventDAO.updateEvent(event) ? null : "Unable to update event.";
    }

    public boolean deleteEvent(int eventId) {
        return eventDAO.deleteEvent(eventId);
    }

    public List<Event> getAllEvents() {
        return eventDAO.getAllEvents();
    }

    public List<Event> searchEvents(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllEvents();
        }
        return eventDAO.searchEvents(keyword.trim());
    }

    public int getTotalEvents() {
        List<Event> events = getAllEvents();
        return events == null ? 0 : events.size();
    }

    public int getTotalParticipants() {
        int total = 0;
        for (EventReport report : getEventReports()) {
            total += report.getParticipantCount();
        }
        return total;
    }

    public BigDecimal getTotalRemainingBudget() {
        BigDecimal total = BigDecimal.ZERO;
        for (EventReport report : getEventReports()) {
            total = total.add(report.getRemainingBudget());
        }
        return total;
    }

    public int getTotalAvailableSeats() {
        List<Event> events = getAllEvents();
        if (events == null) {
            return 0;
        }

        int total = 0;
        for (Event event : events) {
            total += event.getAvailableSeats();
        }
        return total;
    }

    public List<Event> getSafeEvents() {
        List<Event> events = getAllEvents();
        return events == null ? Collections.emptyList() : events;
    }

    public List<EventReport> getEventReports() {
        return eventDAO.getEventReports();
    }

    public List<Event> getLongEvents(int minDays) {
        return eventDAO.getLongEvents(minDays);
    }

    public List<Event> getEventsOverlapping(LocalDate from, LocalDate to) {
        return eventDAO.getEventsOverlapping(from, to);
    }
}
