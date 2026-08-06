package model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Basic event model used across the application.
 */
public class Event {
    private int eventId;
    private String name;
    private String description;
    private LocalDate eventDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private String venue;
    private String category;
    private int totalSeats;
    private int availableSeats;
    private BigDecimal fee;
    private BigDecimal totalBudget;
    private BigDecimal expenses;

    public Event() {
    }

    public Event(int eventId, String name, String description, LocalDate eventDate, String venue,
                 String category, int totalSeats, int availableSeats, BigDecimal fee,
                 BigDecimal totalBudget, BigDecimal expenses) {
        this.eventId = eventId;
        this.name = name;
        this.description = description;
        this.eventDate = eventDate;
        this.venue = venue;
        this.category = category;
        this.totalSeats = totalSeats;
        this.availableSeats = availableSeats;
        this.fee = fee;
        this.totalBudget = totalBudget;
        this.expenses = expenses;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    /**
     * Duration in days including start and end (e.g., same-day event returns 1).
     */
    public long getDurationDays() {
        LocalDate start = startDate != null ? startDate : eventDate;
        LocalDate end = endDate != null ? endDate : start;
        if (start == null || end == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }

    public BigDecimal getFee() {
        return fee;
    }

    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }

    public BigDecimal getTotalBudget() {
        return totalBudget;
    }

    public void setTotalBudget(BigDecimal totalBudget) {
        this.totalBudget = totalBudget;
    }

    public BigDecimal getExpenses() {
        return expenses;
    }

    public void setExpenses(BigDecimal expenses) {
        this.expenses = expenses;
    }

    public BigDecimal getRemainingBudget() {
        if (totalBudget == null || expenses == null) {
            return BigDecimal.ZERO;
        }
        return totalBudget.subtract(expenses);
    }
}
