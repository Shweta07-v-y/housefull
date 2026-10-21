
package com.ccm.housefull.booking;

import com.ccm.housefull.dto.SeatResponse;
import com.ccm.housefull.event.Event;
import com.ccm.housefull.event.EventRepository;
import com.ccm.housefull.seat.Seat;
import com.ccm.housefull.seat.SeatRepository;
import com.ccm.housefull.seat.SeatStatus;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// Handles seat holds and booking rules for the Housefull application.
@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;

    // Spring injects the repositories that provide database access.
    public BookingService(
            BookingRepository bookingRepository,
            EventRepository eventRepository,
            SeatRepository seatRepository) {

        this.bookingRepository = bookingRepository;
        this.eventRepository = eventRepository;
        this.seatRepository = seatRepository;
    }

    // Creates a temporary seat hold in one database transaction.
    @Transactional
    public HoldSeatsResponse holdSeats(HoldSeatsRequest request) {

        // Capture the current time to calculate the hold deadline.
        LocalDateTime now = LocalDateTime.now();

        // Release expired holds before checking the requested seats.
        expireOldBookings(now);

        // Check that the requested event exists.
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Event not found"));

        List<Long> requestedIds = request.getSeatIds();

        // Prevent a request from including the same seat ID more than once.
        Set<Long> uniqueIds = new HashSet<>(requestedIds);

        if (uniqueIds.size() != requestedIds.size()) {
            throw new IllegalArgumentException(
                    "Duplicate seat IDs are not allowed");
        }

        // Lock the requested seat rows while checking their availability.
        List<Seat> seats =
                seatRepository.findSelectedSeatsForUpdate(requestedIds);

        // Make sure every requested seat ID exists.
        if (seats.size() != requestedIds.size()) {
            throw new IllegalArgumentException(
                    "One or more selected seats do not exist");
        }

        // Ensure that all selected seats belong to the requested event.
        for (Seat seat : seats) {
            if (!seat.getEvent().getId().equals(event.getId())) {
                throw new IllegalArgumentException(
                        "A selected seat does not belong to this event");
            }
        }

        // Only AVAILABLE seats can be held for a new booking.
        for (Seat seat : seats) {
            if (seat.getStatus() != SeatStatus.AVAILABLE) {
                throw new IllegalArgumentException(
                        "Seat " + seat.getRowLabel()
                        + seat.getSeatNumber()
                        + " is not available");
            }
        }

        // The server sets the five-minute deadline; the client cannot choose it.
        LocalDateTime expiryTime = now.plusMinutes(5);

        // Calculate the price from database seat prices, not client input.
        BigDecimal totalAmount = seats.stream()
                .map(Seat::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Mark the selected seats as temporarily held.
        for (Seat seat : seats) {
            seat.setStatus(SeatStatus.HELD);
            seat.setHoldExpiresAt(expiryTime);
        }

        // Create a booking containing the event, selected seats, price and deadline.
        Booking booking = new Booking(
                event,
                new HashSet<>(seats),
                totalAmount,
                expiryTime
        );

        // Save the booking. JPA also tracks the seat changes in this transaction.
        Booking savedBooking = bookingRepository.save(booking);

        // Convert database entities into DTOs for a safe API response.
        List<SeatResponse> seatResponses = seats.stream()
                .map(SeatResponse::from)
                .collect(Collectors.toList());

        // Return the booking reference, status, price, deadline and selected seats.
        return new HoldSeatsResponse(
                savedBooking.getBookingCode(),
                savedBooking.getStatus(),
                savedBooking.getTotalAmount(),
                savedBooking.getHoldExpiresAt(),
                seatResponses
        );
    }

    // Automatically checks for expired holds every 10 seconds.
    // This works even when no customer sends another booking request.
    @Scheduled(fixedDelay = 10000)
    @Transactional
    public void releaseExpiredBookingsAutomatically() {

        // Find and process holds whose deadlines have passed.
        expireOldBookings(LocalDateTime.now());
    }

    // Marks expired bookings and releases their seats.
    // This method is shared by the booking request and scheduled task.
    private void expireOldBookings(LocalDateTime now) {

        // Find bookings that are still marked HELD but have passed their deadline.
        List<Booking> expiredBookings =
                bookingRepository.findByStatusAndHoldExpiresAtLessThanEqual(
                        BookingStatus.HELD,
                        now
                );

        for (Booking booking : expiredBookings) {

            // Record that the customer did not complete payment in time.
            booking.setStatus(BookingStatus.EXPIRED);

            // Release seats only when they are still HELD and their deadline passed.
            for (Seat seat : booking.getSeats()) {

                if (seat.getStatus() == SeatStatus.HELD
                        && seat.getHoldExpiresAt() != null
                        && !seat.getHoldExpiresAt().isAfter(now)) {

                    // Make the seat available for another customer.
                    seat.setStatus(SeatStatus.AVAILABLE);

                    // Clear the old hold deadline.
                    seat.setHoldExpiresAt(null);
                }
            }
        }
    }
}
