
package com.ccm.housefull.booking;

import com.ccm.housefull.dto.SeatResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// Safe response object for displaying a booking in the organiser dashboard.
public class BookingListResponse {

    private String bookingCode;
    private Long eventId;
    private String eventName;
    private BookingStatus status;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private List<SeatResponse> seats;

    public BookingListResponse(
            String bookingCode,
            Long eventId,
            String eventName,
            BookingStatus status,
            BigDecimal totalAmount,
            LocalDateTime createdAt,
            List<SeatResponse> seats) {

        this.bookingCode = bookingCode;
        this.eventId = eventId;
        this.eventName = eventName;
        this.status = status;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
        this.seats = seats;
    }

    public String getBookingCode() {
        return bookingCode;
    }

    public Long getEventId() {
        return eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<SeatResponse> getSeats() {
        return seats;
    }
}
