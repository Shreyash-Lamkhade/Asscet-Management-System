package model;

/**
 * Represents a user's event registration.
 */
public class Registration {
    private int registrationId;
    private int userId;
    private int eventId;

    public Registration() {
    }

    public Registration(int registrationId, int userId, int eventId) {
        this.registrationId = registrationId;
        this.userId = userId;
        this.eventId = eventId;
    }

    public int getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(int registrationId) {
        this.registrationId = registrationId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }
}
