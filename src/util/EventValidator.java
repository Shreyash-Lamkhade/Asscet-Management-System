package util;

import model.Event;

import java.math.BigDecimal;

/**
 * Validates event input before sending it to the database layer.
 */
public final class EventValidator {
    private EventValidator() {
    }

    public static String validate(Event event) {
        if (event == null) {
            return "Event data is missing.";
        }
        if (isBlank(event.getName())) {
            return "Event name is required.";
        }
        if (isBlank(event.getDescription())) {
            return "Description is required.";
        }
        if (event.getEventDate() == null) {
            return "Event date is required.";
        }
        if (event.getStartDate() != null && event.getEndDate() != null) {
            if (event.getEndDate().isBefore(event.getStartDate())) {
                return "End date cannot be before start date.";
            }
        }
        if (isBlank(event.getVenue())) {
            return "Venue is required.";
        }
        if (isBlank(event.getCategory())) {
            return "Category is required.";
        }
        if (event.getTotalSeats() <= 0) {
            return "Total seats must be greater than zero.";
        }
        if (event.getAvailableSeats() < 0) {
            return "Available seats cannot be negative.";
        }
        if (event.getAvailableSeats() > event.getTotalSeats()) {
            return "Available seats cannot be greater than total seats.";
        }
        if (isNegative(event.getFee())) {
            return "Fee cannot be negative.";
        }
        if (isNegative(event.getTotalBudget())) {
            return "Total budget cannot be negative.";
        }
        if (isNegative(event.getExpenses())) {
            return "Expenses cannot be negative.";
        }
        if (event.getExpenses() != null && event.getTotalBudget() != null
                && event.getExpenses().compareTo(event.getTotalBudget()) > 0) {
            return "Expenses cannot be greater than total budget.";
        }
        return null;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static boolean isNegative(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) < 0;
    }
}
