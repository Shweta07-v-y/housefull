
package com.ccm.housefull.seat;

import com.ccm.housefull.event.Event;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "seats",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_event_row_seat",
        columnNames = {"event_id", "row_label", "seat_number"}
    )
)
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "row_label", nullable = false, length = 10)
    private String rowLabel;

    @Column(name = "seat_number", nullable = false)
    private Integer seatNumber;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SeatStatus status = SeatStatus.AVAILABLE;

    @Column(name = "hold_expires_at")
    private LocalDateTime holdExpiresAt;

    public Seat() {
    }

    public Seat(
            Event event,
            String rowLabel,
            Integer seatNumber,
            BigDecimal price) {
        this.event = event;
        this.rowLabel = rowLabel;
        this.seatNumber = seatNumber;
        this.price = price;
    }

    public Long getId() {
        return id;
    }

    public Event getEvent() {
        return event;
    }

    public String getRowLabel() {
        return rowLabel;
    }

    public Integer getSeatNumber() {
        return seatNumber;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public SeatStatus getStatus() {
        return status;
    }

    public LocalDateTime getHoldExpiresAt() {
        return holdExpiresAt;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public void setRowLabel(String rowLabel) {
        this.rowLabel = rowLabel;
    }

    public void setSeatNumber(Integer seatNumber) {
        this.seatNumber = seatNumber;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setStatus(SeatStatus status) {
        this.status = status;
    }

    public void setHoldExpiresAt(LocalDateTime holdExpiresAt) {
        this.holdExpiresAt = holdExpiresAt;
    }
}
