
package com.ccm.housefull.booking;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Exposes the booking, payment, and cancellation APIs to clients.
@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    // Service contains the business rules and database operations.
    private final BookingService bookingService;

    // Spring automatically provides the BookingService instance.
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    // POST /api/bookings/hold
    // Temporarily holds the selected seats for five minutes.
    @PostMapping("/hold")
    public ResponseEntity<HoldSeatsResponse> holdSeats(
            @Valid @RequestBody HoldSeatsRequest request) {

        // Ask the service to validate the seats and create a hold.
        HoldSeatsResponse response = bookingService.holdSeats(request);

        // HTTP 201 means a new hold was created.
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // POST /api/bookings/{bookingCode}/payment
    // Simulates a successful or failed payment.
    @PostMapping("/{bookingCode}/payment")
    public ResponseEntity<PaymentResponse> processPayment(
            @PathVariable String bookingCode,
            @Valid @RequestBody PaymentRequest request) {

        // Pass the booking reference and Pay/Fail choice to the service.
        PaymentResponse response =
                bookingService.processPayment(bookingCode, request);

        // HTTP 200 means the payment simulation was processed.
        return ResponseEntity.ok(response);
    }

    // POST /api/bookings/{bookingCode}/cancel
    // Cancels a confirmed booking and reports a simulated refund.
    @PostMapping("/{bookingCode}/cancel")
    public ResponseEntity<CancellationResponse> cancelBooking(
            @PathVariable String bookingCode) {

        // The service checks whether cancellation is allowed.
        CancellationResponse response =
                bookingService.cancelBooking(bookingCode);

        // Return the cancellation result and simulated refund amount.
        return ResponseEntity.ok(response);
    }
}
