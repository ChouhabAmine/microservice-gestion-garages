package com.renault.garage.domain.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.AssertTrue;
import java.time.LocalTime;


public class OpeningTime {

    @NotNull
    private LocalTime startTime;

    @NotNull
    private LocalTime endTime;

    public OpeningTime() { }

    public OpeningTime(LocalTime startTime, LocalTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    @AssertTrue(message = "endTime must be after startTime")
    public boolean isRangeValid() {
        return startTime == null || endTime == null || endTime.isAfter(startTime);
    }

    // Getters / Setters
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
}
