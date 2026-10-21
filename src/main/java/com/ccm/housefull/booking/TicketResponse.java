
package com.ccm.housefull.booking;

import com.ccm.housefull.booking.BookingStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// Defines the information shown on a customer's ticket.
public class TicketResponse {

    private String bookingCode;
    private String eventName;
    private String venue;
    private LocalDateTime eventDateTime;
    private BookingStatus status;
    private BigDecimal totalAmount;
    private List<String> seats;
    private boolean checkedIn;
    private String qrCodeBase64;

    public TicketResponse(
            String bookingCode,
            String eventName,
            String venue,
            LocalDateTime eventDateTime,
            BookingStatus status,
            BigDecimal totalAmount,
            List<String> seats,
            boolean checkedIn,
            String qrCodeBase64) {

        this.bookingCode = bookingCode;
        this.eventName = eventName;
        this.venue = venue;
        this.eventDateTime = eventDateTime;
        this.status = status;
        this.totalAmount = totalAmount;
        this.seats = seats;
        this.checkedIn = checkedIn;
        this.qrCodeBase64 = qrCodeBase64;
    }

    public String getBookingCode() {
        return bookingCode;
    }

    public String getEventName() {
        return eventName;
    }

    public String getVenue() {
        return venue;
    }

    public LocalDateTime getEventDateTime() {
        return eventDateTime;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public List<String> getSeats() {
        return seats;
    }

    public boolean isCheckedIn() {
        return checkedIn;
    }

    public String getQrCodeBase64() {
        return qrCodeBase64;
    }
}
