
package com.ccm.housefull.dto;

import com.ccm.housefull.seat.Seat;
import com.ccm.housefull.seat.SeatStatus;

import java.math.BigDecimal;

public class SeatResponse {

    private Long id;
    private String rowLabel;
    private Integer seatNumber;
    private BigDecimal price;
    private SeatStatus status;

    public SeatResponse(
            Long id,
            String rowLabel,
            Integer seatNumber,
            BigDecimal price,
            SeatStatus status) {
        this.id = id;
        this.rowLabel = rowLabel;
        this.seatNumber = seatNumber;
        this.price = price;
        this.status = status;
    }

    public static SeatResponse from(Seat seat) {
        return new SeatResponse(
                seat.getId(),
                seat.getRowLabel(),
                seat.getSeatNumber(),
                seat.getPrice(),
                seat.getStatus()
        );
    }

    public Long getId() {
        return id;
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
}
