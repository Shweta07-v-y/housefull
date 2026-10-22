
package com.ccm.housefull.booking;

// Defines the possible states of a booking in Housefull.
public enum BookingStatus {

    // Seats are temporarily reserved for five minutes.
    HELD,

    // Payment succeeded and the booking is confirmed.
    CONFIRMED,

    // Payment failed.
    FAILED,

    // The customer or organiser cancelled the booking.
    CANCELLED,

    // The five-minute hold expired before payment completed.
    EXPIRED
}
