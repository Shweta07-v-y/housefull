
package com.ccm.housefull.booking;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Exposes the booking and simulated payment APIs to the frontend.
@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    // Spring injects the service that implements booking rules.
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    // POST /api/bookings/hold
    // Creates a temporary hold for the selected seats.
    @PostMapping("/hold")
    public ResponseEntity<HoldSeatsResponse> holdSeats(
            @Valid @RequestBody HoldSeatsRequest request) {

        HoldSeatsResponse response = bookingService.holdSeats(request);

        // HTTP 201 indicates that the temporary booking was created.
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // POST /api/bookings/{bookingCode}/payment
    // Simulates successful or failed payment for a booking.
    @PostMapping("/{bookingCode}/payment")
    public ResponseEntity<PaymentResponse> processPayment(
            @PathVariable String bookingCode,
            @Valid @RequestBody PaymentRequest request) {

        PaymentResponse response =
                bookingService.processPayment(bookingCode, request);

        // HTTP 200 indicates that the payment simulation was processed.
        return ResponseEntity.ok(response);
    }
}
