
package com.ccm.housefull.booking;

import com.ccm.housefull.dto.SeatResponse;
import com.ccm.housefull.event.Event;
import com.ccm.housefull.event.EventRepository;
import com.ccm.housefull.seat.Seat;
import com.ccm.housefull.seat.SeatRepository;
import com.ccm.housefull.seat.SeatStatus;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// Holds seats and creates bookings according to Housefull's business rules.
@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;

    // Spring injects the repositories needed to read and update the database.
    public BookingService(
            BookingRepository bookingRepository,
            EventRepository eventRepository,
            SeatRepository seatRepository) {

        this.bookingRepository = bookingRepository;
        this.eventRepository = eventRepository;
        this.seatRepository = seatRepository;
    }

    // Runs the hold operation as one transaction to keep database changes consistent.
    @Transactional
    public HoldSeatsResponse holdSeats(HoldSeatsRequest request) {

        LocalDateTime now = LocalDateTime.now();

        // Clean up old holds before checking the requested seats.
        expireOldBookings(now);

        // Reject requests for events that do not exist.
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Event not found"));

        List<Long> requestedIds = request.getSeatIds();

        // Prevent the same seat ID from appearing more than once in a request.
        Set<Long> uniqueIds = new HashSet<>(requestedIds);

        if (uniqueIds.size() != requestedIds.size()) {
            throw new IllegalArgumentException(
                    "Duplicate seat IDs are not allowed");
        }

        // Lock the selected database rows while this transaction checks availability.
        List<Seat> seats =
                seatRepository.findSelectedSeatsForUpdate(requestedIds);

        // Reject requests containing seat IDs that do not exist.
        if (seats.size() != requestedIds.size()) {
            throw new IllegalArgumentException(
                    "One or more selected seats do not exist");
        }

        // Customers can only book seats belonging to the requested event.
        for (Seat seat : seats) {
            if (!seat.getEvent().getId().equals(event.getId())) {
                throw new IllegalArgumentException(
                        "A selected seat does not belong to this event");
            }
        }

        // Do not allow seats that are already held or sold.
        for (Seat seat : seats) {
            if (seat.getStatus() != SeatStatus.AVAILABLE) {
                throw new IllegalArgumentException(
                        "Seat " + seat.getRowLabel()
                        + seat.getSeatNumber()
                        + " is not available");
            }
        }

        // The server controls the five-minute deadline.
        LocalDateTime expiryTime = now.plusMinutes(5);

        // Calculate the total using trusted prices stored in the database.
        BigDecimal totalAmount = seats.stream()
                .map(Seat::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Mark every selected seat as temporarily held.
        for (Seat seat : seats) {
            seat.setStatus(SeatStatus.HELD);
            seat.setHoldExpiresAt(expiryTime);
        }

        // Create a booking containing the selected seats and total price.
        Booking booking = new Booking(
                event,
                new HashSet<>(seats),
                totalAmount,
                expiryTime
        );

        // Save the booking. JPA tracks the seat updates in this transaction too.
        Booking savedBooking = bookingRepository.save(booking);

        // Return only the seat details the customer needs to see.
        List<SeatResponse> seatResponses = seats.stream()
                .map(SeatResponse::from)
                .collect(Collectors.toList());

        return new HoldSeatsResponse(
                savedBooking.getBookingCode(),
                savedBooking.getStatus(),
                savedBooking.getTotalAmount(),
                savedBooking.getHoldExpiresAt(),
                seatResponses
        );
    }

    // Expires old holds and releases their seats when another hold is requested.
    private void expireOldBookings(LocalDateTime now) {

        List<Booking> expiredBookings =
                bookingRepository.findByStatusAndHoldExpiresAtLessThanEqual(
                        BookingStatus.HELD,
                        now
                );

        for (Booking booking : expiredBookings) {

            // Record that payment time ran out for this booking.
            booking.setStatus(BookingStatus.EXPIRED);

            // Release seats only if they still have this expired hold.
            for (Seat seat : booking.getSeats()) {
                if (seat.getStatus() == SeatStatus.HELD
                        && seat.getHoldExpiresAt() != null
                        && !seat.getHoldExpiresAt().isAfter(now)) {

                    seat.setStatus(SeatStatus.AVAILABLE);
                    seat.setHoldExpiresAt(null);
                }
            }
        }
    }
}
