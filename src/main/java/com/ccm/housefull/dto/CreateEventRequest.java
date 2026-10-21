
package com.ccm.housefull.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public class CreateEventRequest {

    @NotBlank(message = "Event name is required")
    private String name;

    @NotBlank(message = "Venue is required")
    private String venue;

    @NotNull(message = "Event date and time are required")
    @Future(message = "Event date and time must be in the future")
    private LocalDateTime eventDateTime;

    @NotEmpty(message = "At least one seat row is required")
    @Valid
    private List<SeatRowRequest> rows;

    public CreateEventRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public LocalDateTime getEventDateTime() {
        return eventDateTime;
    }

    public void setEventDateTime(LocalDateTime eventDateTime) {
        this.eventDateTime = eventDateTime;
    }

    public List<SeatRowRequest> getRows() {
        return rows;
    }

    public void setRows(List<SeatRowRequest> rows) {
        this.rows = rows;
    }
}
