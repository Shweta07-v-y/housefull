
package com.ccm.housefull.dashboard;

import java.math.BigDecimal;

// Defines the summary data returned to the organiser dashboard.
public class DashboardResponse {

    private Long eventId;
    private String eventName;

    // Seat availability counts.
    private long totalSeats;
    private long seatsSold;
    private long seatsHeld;
    private long seatsAvailable;

    // Booking counts.
    private long totalBookings;
    private long confirmedBookings;
    private long failedBookings;
    private long cancelledBookings;
    private long expiredBookings;
    private long pendingBookings;

    // Money from confirmed bookings in this simulated payment system.
    private BigDecimal revenueCollected;

    public DashboardResponse(
            Long eventId,
            String eventName,
            long totalSeats,
            long seatsSold,
            long seatsHeld,
            long seatsAvailable,
            long totalBookings,
            long confirmedBookings,
            long failedBookings,
            long cancelledBookings,
            long expiredBookings,
            long pendingBookings,
            BigDecimal revenueCollected) {

        this.eventId = eventId;
        this.eventName = eventName;
        this.totalSeats = totalSeats;
        this.seatsSold = seatsSold;
        this.seatsHeld = seatsHeld;
        this.seatsAvailable = seatsAvailable;
        this.totalBookings = totalBookings;
        this.confirmedBookings = confirmedBookings;
        this.failedBookings = failedBookings;
        this.cancelledBookings = cancelledBookings;
        this.expiredBookings = expiredBookings;
        this.pendingBookings = pendingBookings;
        this.revenueCollected = revenueCollected;
    }

    public Long getEventId() {
        return eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public long getTotalSeats() {
        return totalSeats;
    }

    public long getSeatsSold() {
        return seatsSold;
    }

    public long getSeatsHeld() {
        return seatsHeld;
    }

    public long getSeatsAvailable() {
        return seatsAvailable;
    }

    public long getTotalBookings() {
        return totalBookings;
    }

    public long getConfirmedBookings() {
        return confirmedBookings;
    }

    public long getFailedBookings() {
        return failedBookings;
    }

    public long getCancelledBookings() {
        return cancelledBookings;
    }

    public long getExpiredBookings() {
        return expiredBookings;
    }

    public long getPendingBookings() {
        return pendingBookings;
    }

    public BigDecimal getRevenueCollected() {
        return revenueCollected;
    }
}
