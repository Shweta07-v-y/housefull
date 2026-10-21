
package com.ccm.housefull.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

public class SeatRowRequest {

    @NotBlank(message = "Row label is required")
    private String rowLabel;

    @NotNull(message = "Seat count is required")
    @Min(value = 1, message = "A row must have at least one seat")
    private Integer seatCount;

    @NotNull(message = "Row price is required")
    @DecimalMin(value = "0.01", message = "Row price must be greater than zero")
    private BigDecimal price;

    public SeatRowRequest() {
    }

    public String getRowLabel() {
        return rowLabel;
    }

    public void setRowLabel(String rowLabel) {
        this.rowLabel = rowLabel;
    }

    public Integer getSeatCount() {
        return seatCount;
    }

    public void setSeatCount(Integer seatCount) {
        this.seatCount = seatCount;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
