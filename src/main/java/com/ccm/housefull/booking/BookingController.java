
package com.ccm.housefull.booking;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Exposes booking operations through HTTP endpoints.
@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    // Inject the service that handles the booking rules.
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    // POST /api/bookings/hold
    // Receives the customer's event and selected seat IDs.
    @PostMapping("/hold")
    public ResponseEntity<HoldSeatsResponse> holdSeats(
            @Valid @RequestBody HoldSeatsRequest request) {

        // The service validates the seats and creates the temporary hold.
        HoldSeatsResponse response = bookingService.holdSeats(request);

        // HTTP 201 indicates that a new hold was created.
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
