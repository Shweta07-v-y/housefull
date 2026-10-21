
package com.ccm.housefull.booking;

import jakarta.validation.constraints.NotNull;

// Receives the outcome selected on the fake payment page.
public class PaymentRequest {

    // TRUE simulates successful payment; FALSE simulates payment failure.
    @NotNull(message = "Payment outcome is required")
    private Boolean success;

    // Required for JSON-to-Java conversion.
    public PaymentRequest() {
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }
}
