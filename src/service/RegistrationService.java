package service;

import dao.RegistrationDAO;
import model.Event;

import java.util.List;

/**
 * Business logic for event registration.
 */
public class RegistrationService {
    private final RegistrationDAO registrationDAO;

    public RegistrationService() {
        this.registrationDAO = new RegistrationDAO();
    }

    public String registerForEvent(int userId, int eventId, int availableSeats, boolean alreadyRegistered) {
        if (alreadyRegistered) {
            return "You have already registered for this event.";
        }
        if (availableSeats <= 0) {
            return "Event is full. No seats available.";
        }
        return registrationDAO.registerForEvent(userId, eventId) ? null : "Unable to register for event.";
    }

    public String cancelRegistration(int userId, int eventId, boolean alreadyRegistered) {
        if (!alreadyRegistered) {
            return "You are not registered for this event.";
        }
        return registrationDAO.cancelRegistration(userId, eventId) ? null : "Unable to cancel registration.";
    }

    public List<Integer> getRegisteredEventIds(int userId) {
        return registrationDAO.getRegisteredEventIds(userId);
    }

    public List<Event> getRegisteredEvents(int userId) {
        return registrationDAO.getRegisteredEvents(userId);
    }
}
