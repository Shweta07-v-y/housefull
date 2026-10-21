
package com.ccm.housefull.booking;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

// Receives the event and seat IDs that a customer wants to hold.
public class HoldSeatsRequest {

    // The event for which the customer wants to book seats.
    @NotNull(message = "Event ID is required")
    @Positive(message = "Event ID must be positive")
    private Long eventId;

    // The selected seat IDs must not be empty.
    @NotEmpty(message = "Select at least one seat")
    @Size(max = 20, message = "You can hold at most 20 seats per booking")
    private List<@NotNull @Positive Long> seatIds;

    // Required by frameworks that create DTOs from JSON.
    public HoldSeatsRequest() {
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public List<Long> getSeatIds() {
        return seatIds;
    }

    public void setSeatIds(List<Long> seatIds) {
        this.seatIds = seatIds;
    }
}
