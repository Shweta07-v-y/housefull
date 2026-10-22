
package com.ccm.housefull.booking;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Exposes the customer ticket endpoint.
@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    // GET /api/tickets/{bookingCode}
    @GetMapping("/{bookingCode}")
    public ResponseEntity<TicketResponse> getTicket(
            @PathVariable String bookingCode) {

        // Retrieve ticket details and its QR code.
        TicketResponse ticket = ticketService.getTicket(bookingCode);

        return ResponseEntity.ok(ticket);
    }
}
