
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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_code", nullable = false, unique = true, length = 36)
    private String bookingCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToMany
    @JoinTable(
        name = "booking_seats",
        joinColumns = @JoinColumn(name = "booking_id"),
        inverseJoinColumns = @JoinColumn(name = "seat_id")
    )
    private Set<Seat> seats = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status = BookingStatus.HELD;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "hold_expires_at")
    private LocalDateTime holdExpiresAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Tracks whether staff have validated this ticket.
    @Column(name = "checked_in", nullable = false, columnDefinition = "boolean default false")
    private boolean checkedIn = false;

    // Time when staff validated the ticket.
    @Column(name = "checked_in_at")
    private LocalDateTime checkedInAt;

    // Required by JPA.
    public Booking() {
    }

    // Creates a new booking with a unique reference code.
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
        this.checkedIn = false;
        this.checkedInAt = null;
    }

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

    public boolean isCheckedIn() {
        return checkedIn;
    }

    public LocalDateTime getCheckedInAt() {
        return checkedInAt;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public void setHoldExpiresAt(LocalDateTime holdExpiresAt) {
        this.holdExpiresAt = holdExpiresAt;
    }

    // Records the successful check-in.
    public void markCheckedIn(LocalDateTime checkedInAt) {
        this.checkedIn = true;
        this.checkedInAt = checkedInAt;
    }
}
