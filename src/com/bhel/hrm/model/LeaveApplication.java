package com.bhel.hrm.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

public class LeaveApplication implements Serializable {
    private static final long serialVersionUID = 1L;
    public enum Status {PENDING, APPROVED, REJECTED}

    private final String id = UUID.randomUUID().toString();
    private final LocalDate appliedOn = LocalDate.now();
    private final int days;
    private Status status = Status.PENDING;

    public LeaveApplication(int days) { this.days = days; }

    public String getId() { return id; }
    public LocalDate getAppliedOn() { return appliedOn; }
    public int getDays() { return days; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}

