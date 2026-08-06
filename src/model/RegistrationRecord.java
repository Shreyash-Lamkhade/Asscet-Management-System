package model;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Read-model for admin reporting on registrations (attendance).
 */
public class RegistrationRecord {
    private int regId;
    private int userId;
    private String userName;
    private String email;
    private int eventId;
    private String eventName;
    private BigDecimal eventFee;
    private String checkIn;
    private Date checkInTime;

    public int getRegId() {
        return regId;
    }

    public void setRegId(int regId) {
        this.regId = regId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public BigDecimal getEventFee() {
        return eventFee;
    }

    public void setEventFee(BigDecimal eventFee) {
        this.eventFee = eventFee;
    }

    public String getCheckIn() {
        return checkIn;
    }

    public void setCheckIn(String checkIn) {
        this.checkIn = checkIn;
    }

    public Date getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(Date checkInTime) {
        this.checkInTime = checkInTime;
    }
}
