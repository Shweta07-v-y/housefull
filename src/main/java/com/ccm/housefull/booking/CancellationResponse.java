
package com.ccm.housefull.booking;

import java.math.BigDecimal;

// Defines the information returned after a cancellation request.
public class CancellationResponse {

    // Unique reference for the cancelled booking.
    private String bookingCode;

    // Final booking status, which should be CANCELLED.
    private BookingStatus status;

    // Original amount paid for the booking.
    private BigDecimal totalAmount;

    // Simulated refund amount; no real money is transferred.
    private BigDecimal refundAmount;

    // Human-readable explanation of the result.
    private String message;

    // Constructor used by the service to create the API response.
    public CancellationResponse(
            String bookingCode,
            BookingStatus status,
            BigDecimal totalAmount,
            BigDecimal refundAmount,
            String message) {

        this.bookingCode = bookingCode;
        this.status = status;
        this.totalAmount = totalAmount;
        this.refundAmount = refundAmount;
        this.message = message;
    }

    // Getters allow Spring/Jackson to serialize these fields as JSON.
    public String getBookingCode() {
        return bookingCode;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public String getMessage() {
        return message;
    }
}
