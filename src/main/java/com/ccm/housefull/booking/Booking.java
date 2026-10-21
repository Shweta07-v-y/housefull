
package com.ccm.housefull.booking;

import com.ccm.housefull.event.Event;
import com.ccm.housefull.seat.Seat;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "bookings")
public class Booking {

    // Database-generated identifier for this booking.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Unique reference that customers can use to look up their booking.
    @Column(name = "booking_code", nullable = false, unique = true, length = 36)
    private String bookingCode;

    // Multiple bookings can belong to the same event.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    // Associates this booking with the selected seats.
    @ManyToMany
    @JoinTable(
        name = "booking_seats",
        joinColumns = @JoinColumn(name = "booking_id"),
        inverseJoinColumns = @JoinColumn(name = "seat_id")
    )
    private Set<Seat> seats = new HashSet<>();

    // Tracks the current state of this booking.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status = BookingStatus.HELD;

    // Total calculated from the selected seats' database prices.
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    // Deadline for completing payment.
    @Column(name = "hold_expires_at")
    private LocalDateTime holdExpiresAt;

    // When this booking was created.
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Required by JPA when loading a booking from the database.
    public Booking() {
    }

    // Creates a new temporary booking with its initial details.
    public Booking(
            Event event,
            Set<Seat> seats,
            BigDecimal totalAmount,
            LocalDateTime holdExpiresAt) {

        this.bookingCode = UUID.randomUUID().toString();
        this.event = event;
        this.seats = new HashSet<>(seats);
        this.totalAmount = totalAmount;
        this.holdExpiresAt = holdExpiresAt;
        this.createdAt = LocalDateTime.now();
        this.status = BookingStatus.HELD;
    }

    // Getters allow other application classes to read booking details.
    public Long getId() {
        return id;
    }

    public String getBookingCode() {
        return bookingCode;
    }

    public Event getEvent() {
        return event;
    }

    public Set<Seat> getSeats() {
        return seats;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // Updates the booking state after payment, cancellation, or expiry.
    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    // Allows the hold deadline to be updated when needed.
    public void setHoldExpiresAt(LocalDateTime holdExpiresAt) {
        this.holdExpiresAt = holdExpiresAt;
    }
}
