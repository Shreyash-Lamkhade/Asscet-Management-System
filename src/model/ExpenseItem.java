package model;

import java.math.BigDecimal;
import java.util.Date;

public class ExpenseItem {
    private int expenseId;
    private int eventId;
    private String eventName;
    private String title;
    private BigDecimal amount;
    private Date spentOn;

    public int getExpenseId() {
        return expenseId;
    }

    public void setExpenseId(int expenseId) {
        this.expenseId = expenseId;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Date getSpentOn() {
        return spentOn;
    }

    public void setSpentOn(Date spentOn) {
        this.spentOn = spentOn;
    }
}

