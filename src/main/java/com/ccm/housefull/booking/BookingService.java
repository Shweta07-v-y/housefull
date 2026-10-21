
package com.ccm.housefull.booking;

import com.ccm.housefull.dto.SeatResponse;
import com.ccm.housefull.event.Event;
import com.ccm.housefull.event.EventRepository;
import com.ccm.housefull.seat.Seat;
import com.ccm.housefull.seat.SeatRepository;
import com.ccm.housefull.seat.SeatStatus;

import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// Handles booking creation, seat holds, expiry and simulated payments.
@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;

    // Spring injects the repositories used to access MySQL.
    public BookingService(
            BookingRepository bookingRepository,
            EventRepository eventRepository,
            SeatRepository seatRepository) {

        this.bookingRepository = bookingRepository;
        this.eventRepository = eventRepository;
        this.seatRepository = seatRepository;
    }

    // Creates a temporary hold for the selected seats.
    // The transaction keeps booking and seat changes together.
    @Transactional
    public HoldSeatsResponse holdSeats(HoldSeatsRequest request) {

        LocalDateTime now = LocalDateTime.now();

        // Release any expired holds before processing this request.
        expireOldBookings(now);

        // Check that the requested event exists.
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Event not found"));

        List<Long> requestedIds = request.getSeatIds();

        // Prevent the same seat from being requested more than once.
        Set<Long> uniqueIds = new HashSet<>(requestedIds);

        if (uniqueIds.size() != requestedIds.size()) {
            throw new IllegalArgumentException(
                    "Duplicate seat IDs are not allowed");
        }

        // Lock the requested seat rows while checking availability.
        List<Seat> seats =
                seatRepository.findSelectedSeatsForUpdate(requestedIds);

        // Ensure every requested seat ID exists.
        if (seats.size() != requestedIds.size()) {
            throw new IllegalArgumentException(
                    "One or more selected seats do not exist");
        }

        // Ensure the selected seats belong to the requested event.
        for (Seat seat : seats) {
            if (!seat.getEvent().getId().equals(event.getId())) {
                throw new IllegalArgumentException(
                        "A selected seat does not belong to this event");
            }
        }

        // Reject seats that are already held or booked.
        for (Seat seat : seats) {
            if (seat.getStatus() != SeatStatus.AVAILABLE) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Seat " + seat.getRowLabel()
                                + seat.getSeatNumber()
                                + " is not available");
            }
        }

        // The backend determines the five-minute expiry deadline.
        LocalDateTime expiryTime = now.plusMinutes(5);

        // Calculate the price from database values, not client input.
        BigDecimal totalAmount = seats.stream()
                .map(Seat::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Temporarily hold every selected seat.
        for (Seat seat : seats) {
            seat.setStatus(SeatStatus.HELD);
            seat.setHoldExpiresAt(expiryTime);
        }

        // Create a booking with its event, seats, total and expiry time.
        Booking booking = new Booking(
                event,
                new HashSet<>(seats),
                totalAmount,
                expiryTime
        );

        // Save the booking; JPA tracks the seat updates in this transaction.
        Booking savedBooking = bookingRepository.save(booking);

        // Convert entities into response DTOs to avoid exposing JPA entities.
        List<SeatResponse> seatResponses = seats.stream()
                .map(SeatResponse::from)
                .collect(Collectors.toList());

        // Return the booking reference and details needed by the customer.
        return new HoldSeatsResponse(
                savedBooking.getBookingCode(),
                savedBooking.getStatus(),
                savedBooking.getTotalAmount(),
                savedBooking.getHoldExpiresAt(),
                seatResponses
        );
    }

    // Processes the fake Pay/Fail action for a booking.
    @Transactional
    public PaymentResponse processPayment(
            String bookingCode,
            PaymentRequest request) {

        // Find the booking using the code returned when seats were held.
        Booking booking = bookingRepository.findByBookingCode(bookingCode)
                .orElseThrow(() ->
                        new IllegalArgumentException("Booking not found"));

        LocalDateTime now = LocalDateTime.now();

        // Only bookings that are still awaiting payment can be processed.
        if (booking.getStatus() != BookingStatus.HELD) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This booking is no longer awaiting payment");
        }

        // Reject payment if the five-minute hold has expired.
        if (booking.getHoldExpiresAt() == null
                || !booking.getHoldExpiresAt().isAfter(now)) {

            // Process expiry now rather than relying on the scheduled task.
            expireOldBookings(now);

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "The seat hold has expired. Please book again.");
        }

        // Handle simulated successful payment.
        if (Boolean.TRUE.equals(request.getSuccess())) {

            // Verify that all seats are still held and their deadlines are valid.
            for (Seat seat : booking.getSeats()) {
                if (seat.getStatus() != SeatStatus.HELD
                        || seat.getHoldExpiresAt() == null
                        || !seat.getHoldExpiresAt().isAfter(now)
                        || !seat.getHoldExpiresAt().equals(
                                booking.getHoldExpiresAt())) {

                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "A seat hold is no longer valid. Please book again.");
                }
            }

            // Payment succeeded: confirm the booking and sell its seats.
            for (Seat seat : booking.getSeats()) {
                seat.setStatus(SeatStatus.BOOKED);

                // Clear the temporary deadline after confirmation.
                seat.setHoldExpiresAt(null);
            }

            booking.setStatus(BookingStatus.CONFIRMED);

            return new PaymentResponse(
                    booking.getBookingCode(),
                    booking.getStatus(),
                    booking.getTotalAmount(),
                    "Payment successful. Your booking is confirmed."
            );
        }

        // Handle simulated failed payment by releasing this booking's seats.
        for (Seat seat : booking.getSeats()) {
            if (seat.getStatus() == SeatStatus.HELD
                    && seat.getHoldExpiresAt() != null
                    && seat.getHoldExpiresAt().equals(
                            booking.getHoldExpiresAt())) {

                seat.setStatus(SeatStatus.AVAILABLE);
                seat.setHoldExpiresAt(null);
            }
        }

        // Record the failed payment so it cannot be processed a second time.
        booking.setStatus(BookingStatus.FAILED);

        return new PaymentResponse(
                booking.getBookingCode(),
                booking.getStatus(),
                booking.getTotalAmount(),
                "Payment failed. The held seats have been released."
        );
    }

    // Runs automatically every 10 seconds to release expired seat holds.
    @Scheduled(fixedDelay = 10000)
    @Transactional
    public void releaseExpiredBookingsAutomatically() {
        expireOldBookings(LocalDateTime.now());
    }

    // Marks expired bookings and releases seats whose holds have expired.
    private void expireOldBookings(LocalDateTime now) {

        List<Booking> expiredBookings =
                bookingRepository.findByStatusAndHoldExpiresAtLessThanEqual(
                        BookingStatus.HELD,
                        now
                );

        for (Booking booking : expiredBookings) {

            // Mark the booking as expired because its payment deadline passed.
            booking.setStatus(BookingStatus.EXPIRED);

            // Release only seats that are still held under an expired deadline.
            for (Seat seat : booking.getSeats()) {
                if (seat.getStatus() == SeatStatus.HELD
                        && seat.getHoldExpiresAt() != null
                        && !seat.getHoldExpiresAt().isAfter(now)
                        && seat.getHoldExpiresAt().equals(
                                booking.getHoldExpiresAt())) {

                    seat.setStatus(SeatStatus.AVAILABLE);
                    seat.setHoldExpiresAt(null);
                }
            }
        }
    }
}
