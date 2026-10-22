
package com.ccm.housefull.booking;

import java.math.BigDecimal;

// Returns the result of the simulated payment to the frontend.
public class PaymentResponse {

    private String bookingCode;
    private BookingStatus status;
    private BigDecimal totalAmount;
    private String message;

    public PaymentResponse(
            String bookingCode,
            BookingStatus status,
            BigDecimal totalAmount,
            String message) {

        this.bookingCode = bookingCode;
        this.status = status;
        this.totalAmount = totalAmount;
        this.message = message;
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

    public String getMessage() {
        return message;
    }
}
