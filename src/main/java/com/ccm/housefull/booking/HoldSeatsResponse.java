
package com.ccm.housefull.booking;

import com.ccm.housefull.dto.SeatResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// Contains the information the customer needs after seats are held.
public class HoldSeatsResponse {

    private String bookingCode;
    private BookingStatus status;
    private BigDecimal totalAmount;
    private LocalDateTime holdExpiresAt;
    private List<SeatResponse> seats;

    public HoldSeatsResponse(
            String bookingCode,
            BookingStatus status,
            BigDecimal totalAmount,
            LocalDateTime holdExpiresAt,
            List<SeatResponse> seats) {

        this.bookingCode = bookingCode;
        this.status = status;
        this.totalAmount = totalAmount;
        this.holdExpiresAt = holdExpiresAt;
        this.seats = seats;
    }

    public String getBookingCode() {
        return bookingCode;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public LocalDateTime getHoldExpiresAt() {
        return holdExpiresAt;
    }

    public List<SeatResponse> getSeats() {
        return seats;
    }
}
