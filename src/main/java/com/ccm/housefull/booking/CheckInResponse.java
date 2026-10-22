
package com.ccm.housefull.booking;

import java.time.LocalDateTime;

// Response returned after staff validate a ticket.
public class CheckInResponse {

    private String bookingCode;
    private String eventName;
    private BookingStatus status;
    private boolean checkedIn;
    private LocalDateTime checkedInAt;
    private String message;

    public CheckInResponse(
            String bookingCode,
            String eventName,
            BookingStatus status,
            boolean checkedIn,
            LocalDateTime checkedInAt,
            String message) {

        this.bookingCode = bookingCode;
        this.eventName = eventName;
        this.status = status;
        this.checkedIn = checkedIn;
        this.checkedInAt = checkedInAt;
        this.message = message;
    }

    public String getBookingCode() {
        return bookingCode;
    }

    public String getEventName() {
        return eventName;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public boolean isCheckedIn() {
        return checkedIn;
    }

    public LocalDateTime getCheckedInAt() {
        return checkedInAt;
    }

    public String getMessage() {
        return message;
    }
}
