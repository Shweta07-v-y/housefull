
// package com.ccm.housefull.booking;

// import com.ccm.housefull.dto.SeatResponse;
// import com.ccm.housefull.event.Event;
// import com.ccm.housefull.event.EventRepository;
// import com.ccm.housefull.seat.Seat;
// import com.ccm.housefull.seat.SeatRepository;
// import com.ccm.housefull.seat.SeatStatus;

// import org.springframework.http.HttpStatus;
// import org.springframework.scheduling.annotation.Scheduled;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;
// import org.springframework.web.server.ResponseStatusException;

// import java.math.BigDecimal;
// import java.time.LocalDateTime;
// import java.util.HashSet;
// import java.util.List;
// import java.util.Set;
// import java.util.stream.Collectors;

// // Handles seat holds, payments, expiry, and booking cancellation.
// @Service
// public class BookingService {

//     private final BookingRepository bookingRepository;
//     private final EventRepository eventRepository;
//     private final SeatRepository seatRepository;

//     // Spring injects the repositories used to access the database.
//     public BookingService(
//             BookingRepository bookingRepository,
//             EventRepository eventRepository,
//             SeatRepository seatRepository) {

//         this.bookingRepository = bookingRepository;
//         this.eventRepository = eventRepository;
//         this.seatRepository = seatRepository;
//     }

//     // Creates a temporary hold for selected seats.
//     @Transactional
//     public HoldSeatsResponse holdSeats(HoldSeatsRequest request) {

//         LocalDateTime now = LocalDateTime.now();

//         // Release expired holds before processing this request.
//         expireOldBookings(now);

//         // Find the event selected by the customer.
//         Event event = eventRepository.findById(request.getEventId())
//                 .orElseThrow(() ->
//                         new IllegalArgumentException("Event not found"));

//         List<Long> requestedIds = request.getSeatIds();

//         // Reject duplicate IDs in the same request.
//         Set<Long> uniqueIds = new HashSet<>(requestedIds);

//         if (uniqueIds.size() != requestedIds.size()) {
//             throw new IllegalArgumentException(
//                     "Duplicate seat IDs are not allowed");
//         }

//         // Lock the requested seat rows while checking availability.
//         List<Seat> seats =
//                 seatRepository.findSelectedSeatsForUpdate(requestedIds);

//         // Ensure every requested seat exists.
//         if (seats.size() != requestedIds.size()) {
//             throw new IllegalArgumentException(
//                     "One or more selected seats do not exist");
//         }

//         // Ensure all selected seats belong to this event.
//         for (Seat seat : seats) {
//             if (!seat.getEvent().getId().equals(event.getId())) {
//                 throw new IllegalArgumentException(
//                         "A selected seat does not belong to this event");
//             }
//         }

//         // Reject seats that are already held or booked.
//         for (Seat seat : seats) {
//             if (seat.getStatus() != SeatStatus.AVAILABLE) {
//                 throw new ResponseStatusException(
//                         HttpStatus.CONFLICT,
//                         "Seat " + seat.getRowLabel()
//                                 + seat.getSeatNumber()
//                                 + " is not available");
//             }
//         }

//         // Calculate the hold deadline using server time.
//         LocalDateTime expiryTime = now.plusMinutes(5);

//         // Calculate the total from database prices, not client input.
//         BigDecimal totalAmount = seats.stream()
//                 .map(Seat::getPrice)
//                 .reduce(BigDecimal.ZERO, BigDecimal::add);

//         // Mark all selected seats as temporarily held.
//         for (Seat seat : seats) {
//             seat.setStatus(SeatStatus.HELD);
//             seat.setHoldExpiresAt(expiryTime);
//         }

//         // Create a booking linked to the event and selected seats.
//         Booking booking = new Booking(
//                 event,
//                 new HashSet<>(seats),
//                 totalAmount,
//                 expiryTime
//         );

//         // Save the booking. JPA tracks the seat changes in this transaction.
//         Booking savedBooking = bookingRepository.save(booking);

//         // Convert seat entities to response DTOs.
//         List<SeatResponse> seatResponses = seats.stream()
//                 .map(SeatResponse::from)
//                 .collect(Collectors.toList());

//         // Return the reference, price, expiry, and selected seats.
//         return new HoldSeatsResponse(
//                 savedBooking.getBookingCode(),
//                 savedBooking.getStatus(),
//                 savedBooking.getTotalAmount(),
//                 savedBooking.getHoldExpiresAt(),
//                 seatResponses
//         );
//     }

//     // Processes the simulated Pay/Fail action.
//     @Transactional
//     public PaymentResponse processPayment(
//             String bookingCode,
//             PaymentRequest request) {

//         // Find the booking using its unique reference.
//         Booking booking = bookingRepository.findByBookingCode(bookingCode)
//                 .orElseThrow(() ->
//                         new IllegalArgumentException("Booking not found"));

//         LocalDateTime now = LocalDateTime.now();

//         // Only a booking awaiting payment may be processed.
//         if (booking.getStatus() != BookingStatus.HELD) {
//             throw new ResponseStatusException(
//                     HttpStatus.CONFLICT,
//                     "This booking is no longer awaiting payment");
//         }

//         // Reject payment if the hold deadline is missing or has passed.
//         if (booking.getHoldExpiresAt() == null
//                 || !booking.getHoldExpiresAt().isAfter(now)) {

//             // The scheduled task also expires holds automatically.
//             // Do not throw after making expiry changes here: an exception
//             // could roll back those changes with this transaction.
//             throw new ResponseStatusException(
//                     HttpStatus.CONFLICT,
//                     "The seat hold has expired. Please book again.");
//         }

//         // Handle simulated successful payment.
//         if (Boolean.TRUE.equals(request.getSuccess())) {

//             // Verify that every seat still belongs to this active hold.
//             for (Seat seat : booking.getSeats()) {
//                 if (seat.getStatus() != SeatStatus.HELD
//                         || seat.getHoldExpiresAt() == null
//                         || !seat.getHoldExpiresAt().isAfter(now)
//                         || !seat.getHoldExpiresAt().equals(
//                                 booking.getHoldExpiresAt())) {

//                     throw new ResponseStatusException(
//                             HttpStatus.CONFLICT,
//                             "A seat hold is no longer valid. Please book again.");
//                 }
//             }

//             // Confirm the booking and permanently book its seats.
//             for (Seat seat : booking.getSeats()) {
//                 seat.setStatus(SeatStatus.BOOKED);
//                 seat.setHoldExpiresAt(null);
//             }

//             booking.setStatus(BookingStatus.CONFIRMED);

//             return new PaymentResponse(
//                     booking.getBookingCode(),
//                     booking.getStatus(),
//                     booking.getTotalAmount(),
//                     "Payment successful. Your booking is confirmed."
//             );
//         }

//         // Handle simulated payment failure.
//         // Release only seats still held under this booking's deadline.
//         for (Seat seat : booking.getSeats()) {
//             if (seat.getStatus() == SeatStatus.HELD
//                     && seat.getHoldExpiresAt() != null
//                     && seat.getHoldExpiresAt().equals(
//                             booking.getHoldExpiresAt())) {

//                 seat.setStatus(SeatStatus.AVAILABLE);
//                 seat.setHoldExpiresAt(null);
//             }
//         }

//         // Mark the booking failed so it cannot be paid again.
//         booking.setStatus(BookingStatus.FAILED);

//         return new PaymentResponse(
//                 booking.getBookingCode(),
//                 booking.getStatus(),
//                 booking.getTotalAmount(),
//                 "Payment failed. The held seats have been released."
//         );
//     }

//     // Cancels a confirmed booking and releases its seats.
//     @Transactional
//     public CancellationResponse cancelBooking(String bookingCode) {

//         // Find the booking using its unique reference.
//         Booking booking = bookingRepository.findByBookingCode(bookingCode)
//                 .orElseThrow(() ->
//                         new IllegalArgumentException("Booking not found"));

//         // Only confirmed bookings can be cancelled.
//         if (booking.getStatus() != BookingStatus.CONFIRMED) {
//             throw new ResponseStatusException(
//                     HttpStatus.CONFLICT,
//                     "Only confirmed bookings can be cancelled");
//         }

//         // Validate every seat before modifying any seat.
//         for (Seat seat : booking.getSeats()) {
//             if (seat.getStatus() != SeatStatus.BOOKED) {
//                 throw new ResponseStatusException(
//                         HttpStatus.CONFLICT,
//                         "A seat is not in BOOKED status; cancellation cannot proceed");
//             }
//         }

//         // Release the booked seats for future customers.
//         for (Seat seat : booking.getSeats()) {
//             seat.setStatus(SeatStatus.AVAILABLE);
//             seat.setHoldExpiresAt(null);
//         }

//         // Update the booking's final state.
//         booking.setStatus(BookingStatus.CANCELLED);

//         // This is a simulated refund amount, not an actual money transfer.
//         BigDecimal simulatedRefund = booking.getTotalAmount();

//         // Return cancellation and refund details to the caller.
//         return new CancellationResponse(
//                 booking.getBookingCode(),
//                 booking.getStatus(),
//                 booking.getTotalAmount(),
//                 simulatedRefund,
//                 "Booking cancelled successfully. Simulated refund recorded."
//         );
//     }

//     // Runs every 10 seconds to release expired holds automatically.
//     @Scheduled(fixedDelay = 10000)
//     @Transactional
//     public void releaseExpiredBookingsAutomatically() {
//         expireOldBookings(LocalDateTime.now());
//     }

//     // Marks expired bookings and releases their expired seats.
//     private void expireOldBookings(LocalDateTime now) {

//         List<Booking> expiredBookings =
//                 bookingRepository.findByStatusAndHoldExpiresAtLessThanEqual(
//                         BookingStatus.HELD,
//                         now
//                 );

//         for (Booking booking : expiredBookings) {

//             // Mark the temporary booking as expired.
//             booking.setStatus(BookingStatus.EXPIRED);

//             // Release only seats still held under this booking's deadline.
//             for (Seat seat : booking.getSeats()) {
//                 if (seat.getStatus() == SeatStatus.HELD
//                         && seat.getHoldExpiresAt() != null
//                         && !seat.getHoldExpiresAt().isAfter(now)
//                         && seat.getHoldExpiresAt().equals(
//                                 booking.getHoldExpiresAt())) {

//                     seat.setStatus(SeatStatus.AVAILABLE);
//                     seat.setHoldExpiresAt(null);
//                 }
//             }
//         }
//     }
// }







// package com.ccm.housefull.booking;

// import com.ccm.housefull.dto.SeatResponse;
// import com.ccm.housefull.event.Event;
// import com.ccm.housefull.event.EventRepository;
// import com.ccm.housefull.seat.Seat;
// import com.ccm.housefull.seat.SeatRepository;
// import com.ccm.housefull.seat.SeatStatus;

// import org.springframework.http.HttpStatus;
// import org.springframework.scheduling.annotation.Scheduled;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;
// import org.springframework.web.server.ResponseStatusException;

// import java.math.BigDecimal;
// import java.time.LocalDateTime;
// import java.util.HashSet;
// import java.util.List;
// import java.util.Set;
// import java.util.stream.Collectors;

// // Handles booking creation, seat holds, payments, expiry and cancellation.
// @Service
// public class BookingService {

//     private final BookingRepository bookingRepository;
//     private final EventRepository eventRepository;
//     private final SeatRepository seatRepository;

//     // Spring injects the repositories required to access MySQL.
//     public BookingService(
//             BookingRepository bookingRepository,
//             EventRepository eventRepository,
//             SeatRepository seatRepository) {

//         this.bookingRepository = bookingRepository;
//         this.eventRepository = eventRepository;
//         this.seatRepository = seatRepository;
//     }

//     // Creates a temporary hold for selected seats.
//     @Transactional
//     public HoldSeatsResponse holdSeats(HoldSeatsRequest request) {

//         LocalDateTime now = LocalDateTime.now();

//         // Release expired holds before handling the new request.
//         expireOldBookings(now);

//         // Find the event requested by the customer.
//         Event event = eventRepository.findById(request.getEventId())
//                 .orElseThrow(() ->
//                         new IllegalArgumentException("Event not found"));

//         List<Long> requestedIds = request.getSeatIds();

//         // Prevent duplicate seat IDs in the same request.
//         Set<Long> uniqueIds = new HashSet<>(requestedIds);

//         if (uniqueIds.size() != requestedIds.size()) {
//             throw new IllegalArgumentException(
//                     "Duplicate seat IDs are not allowed");
//         }

//         // Lock selected seat rows while checking their availability.
//         List<Seat> seats =
//                 seatRepository.findSelectedSeatsForUpdate(requestedIds);

//         // Ensure that all requested seats exist.
//         if (seats.size() != requestedIds.size()) {
//             throw new IllegalArgumentException(
//                     "One or more selected seats do not exist");
//         }

//         // Verify that every selected seat belongs to the requested event.
//         for (Seat seat : seats) {
//             if (!seat.getEvent().getId().equals(event.getId())) {
//                 throw new IllegalArgumentException(
//                         "A selected seat does not belong to this event");
//             }
//         }

//         // Reject seats that are already held or booked.
//         for (Seat seat : seats) {
//             if (seat.getStatus() != SeatStatus.AVAILABLE) {
//                 throw new ResponseStatusException(
//                         HttpStatus.CONFLICT,
//                         "Seat " + seat.getRowLabel()
//                                 + seat.getSeatNumber()
//                                 + " is not available");
//             }
//         }

//         // Set the temporary hold deadline to five minutes from now.
//         LocalDateTime expiryTime = now.plusMinutes(5);

//         // Calculate the price using trusted database values.
//         BigDecimal totalAmount = seats.stream()
//                 .map(Seat::getPrice)
//                 .reduce(BigDecimal.ZERO, BigDecimal::add);

//         // Mark each selected seat as temporarily held.
//         for (Seat seat : seats) {
//             seat.setStatus(SeatStatus.HELD);
//             seat.setHoldExpiresAt(expiryTime);
//         }

//         // Create the booking containing the event, seats, price and deadline.
//         Booking booking = new Booking(
//                 event,
//                 new HashSet<>(seats),
//                 totalAmount,
//                 expiryTime
//         );

//         // Save the booking within the current transaction.
//         Booking savedBooking = bookingRepository.save(booking);

//         // Convert seat entities into response objects.
//         List<SeatResponse> seatResponses = seats.stream()
//                 .map(SeatResponse::from)
//                 .collect(Collectors.toList());

//         // Return the booking details required by the customer.
//         return new HoldSeatsResponse(
//                 savedBooking.getBookingCode(),
//                 savedBooking.getStatus(),
//                 savedBooking.getTotalAmount(),
//                 savedBooking.getHoldExpiresAt(),
//                 seatResponses
//         );
//     }

//     // Processes the simulated Pay/Fail action for a booking.
//     @Transactional
//     public PaymentResponse processPayment(
//             String bookingCode,
//             PaymentRequest request) {

//         // Lock this booking row so concurrent payment/cancellation
//         // operations on the same booking cannot proceed simultaneously.
//         Booking booking = bookingRepository
//                 .findByBookingCodeForUpdate(bookingCode)
//                 .orElseThrow(() ->
//                         new IllegalArgumentException("Booking not found"));

//         LocalDateTime now = LocalDateTime.now();

//         // Only bookings awaiting payment may be processed.
//         if (booking.getStatus() != BookingStatus.HELD) {
//             throw new ResponseStatusException(
//                     HttpStatus.CONFLICT,
//                     "This booking is no longer awaiting payment");
//         }

//         // Reject payment if the temporary hold has expired.
//         if (booking.getHoldExpiresAt() == null
//                 || !booking.getHoldExpiresAt().isAfter(now)) {

//             // The scheduled expiry task will release expired holds.
//             throw new ResponseStatusException(
//                     HttpStatus.CONFLICT,
//                     "The seat hold has expired. Please book again.");
//         }

//         // Handle simulated successful payment.
//         if (Boolean.TRUE.equals(request.getSuccess())) {

//             // Confirm that all seats still belong to this active hold.
//             for (Seat seat : booking.getSeats()) {
//                 if (seat.getStatus() != SeatStatus.HELD
//                         || seat.getHoldExpiresAt() == null
//                         || !seat.getHoldExpiresAt().isAfter(now)
//                         || !seat.getHoldExpiresAt().equals(
//                                 booking.getHoldExpiresAt())) {

//                     throw new ResponseStatusException(
//                             HttpStatus.CONFLICT,
//                             "A seat hold is no longer valid. Please book again.");
//                 }
//             }

//             // Mark the seats as booked after successful payment.
//             for (Seat seat : booking.getSeats()) {
//                 seat.setStatus(SeatStatus.BOOKED);

//                 // Remove the temporary hold deadline.
//                 seat.setHoldExpiresAt(null);
//             }

//             // Update the booking status.
//             booking.setStatus(BookingStatus.CONFIRMED);

//             return new PaymentResponse(
//                     booking.getBookingCode(),
//                     booking.getStatus(),
//                     booking.getTotalAmount(),
//                     "Payment successful. Your booking is confirmed."
//             );
//         }

//         // Handle failed payment by releasing seats held by this booking.
//         for (Seat seat : booking.getSeats()) {
//             if (seat.getStatus() == SeatStatus.HELD
//                     && seat.getHoldExpiresAt() != null
//                     && seat.getHoldExpiresAt().equals(
//                             booking.getHoldExpiresAt())) {

//                 seat.setStatus(SeatStatus.AVAILABLE);
//                 seat.setHoldExpiresAt(null);
//             }
//         }

//         // Record failure so this booking cannot be paid again.
//         booking.setStatus(BookingStatus.FAILED);

//         return new PaymentResponse(
//                 booking.getBookingCode(),
//                 booking.getStatus(),
//                 booking.getTotalAmount(),
//                 "Payment failed. The held seats have been released."
//         );
//     }

//     // Cancels a confirmed booking and releases its seats.
//     @Transactional
//     public CancellationResponse cancelBooking(String bookingCode) {

//         // Lock the booking row before checking its status.
//         // This uses the same lock as the payment method.
//         Booking booking = bookingRepository
//                 .findByBookingCodeForUpdate(bookingCode)
//                 .orElseThrow(() ->
//                         new IllegalArgumentException("Booking not found"));

//         // Only confirmed bookings can be cancelled.
//         if (booking.getStatus() != BookingStatus.CONFIRMED) {
//             throw new ResponseStatusException(
//                     HttpStatus.CONFLICT,
//                     "Only confirmed bookings can be cancelled");
//         }

//         // Validate all seats before changing any of them.
//         for (Seat seat : booking.getSeats()) {
//             if (seat.getStatus() != SeatStatus.BOOKED) {
//                 throw new ResponseStatusException(
//                         HttpStatus.CONFLICT,
//                         "A seat is not in BOOKED status; cancellation cannot proceed");
//             }
//         }

//         // Release all seats associated with the confirmed booking.
//         for (Seat seat : booking.getSeats()) {
//             seat.setStatus(SeatStatus.AVAILABLE);
//             seat.setHoldExpiresAt(null);
//         }

//         // Prevent the booking from being cancelled again.
//         booking.setStatus(BookingStatus.CANCELLED);

//         // Calculate the simulated refund; no real money is transferred.
//         BigDecimal simulatedRefund = booking.getTotalAmount();

//         // Return the cancellation result.
//         return new CancellationResponse(
//                 booking.getBookingCode(),
//                 booking.getStatus(),
//                 booking.getTotalAmount(),
//                 simulatedRefund,
//                 "Booking cancelled successfully. Simulated refund recorded."
//         );
//     }

//     // Runs automatically every ten seconds to release expired holds.
//     @Scheduled(fixedDelay = 10000)
//     @Transactional
//     public void releaseExpiredBookingsAutomatically() {
//         expireOldBookings(LocalDateTime.now());
//     }

//     // Marks expired holds and releases the corresponding seats.
//     private void expireOldBookings(LocalDateTime now) {

//         List<Booking> expiredBookings =
//                 bookingRepository.findByStatusAndHoldExpiresAtLessThanEqual(
//                         BookingStatus.HELD,
//                         now
//                 );

//         for (Booking booking : expiredBookings) {

//             // Mark the booking as expired.
//             booking.setStatus(BookingStatus.EXPIRED);

//             // Release only seats that still belong to this expired hold.
//             for (Seat seat : booking.getSeats()) {
//                 if (seat.getStatus() == SeatStatus.HELD
//                         && seat.getHoldExpiresAt() != null
//                         && !seat.getHoldExpiresAt().isAfter(now)
//                         && seat.getHoldExpiresAt().equals(
//                                 booking.getHoldExpiresAt())) {

//                     seat.setStatus(SeatStatus.AVAILABLE);
//                     seat.setHoldExpiresAt(null);
//                 }
//             }
//         }
//     }
// }



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

// Service layer containing the business logic for bookings.
@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final SeatRepository seatRepository;
    private final EventRepository eventRepository;

    // Spring injects the repositories required to access MySQL.
    public BookingService(
            BookingRepository bookingRepository,
            SeatRepository seatRepository,
            EventRepository eventRepository) {

        this.bookingRepository = bookingRepository;
        this.seatRepository = seatRepository;
        this.eventRepository = eventRepository;
    }

    // Holds selected seats for five minutes while the customer pays.
    @Transactional
    public HoldSeatsResponse holdSeats(HoldSeatsRequest request) {

        LocalDateTime now = LocalDateTime.now();

        // Release expired holds before checking seat availability.
        expireOldBookings(now);

        // Find the event for which the customer wants seats.
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Event not found"));

        List<Long> seatIds = request.getSeatIds();

        // Prevent duplicate seat IDs in the same request.
        if (new HashSet<>(seatIds).size() != seatIds.size()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Duplicate seat IDs are not allowed");
        }

        // Lock selected seat rows to protect against concurrent requests.
        List<Seat> selectedSeats =
                seatRepository.findSelectedSeatsForUpdate(seatIds);

        // Ensure every requested seat exists.
        if (selectedSeats.size() != seatIds.size()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "One or more seats do not exist");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;

        // Validate seats and calculate the total from database prices.
        for (Seat seat : selectedSeats) {

            if (!seat.getEvent().getId().equals(event.getId())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "A selected seat does not belong to this event");
            }

            if (seat.getStatus() != SeatStatus.AVAILABLE) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "One or more selected seats are unavailable");
            }

            totalAmount = totalAmount.add(seat.getPrice());
        }

        // Set the hold deadline to five minutes from now.
        LocalDateTime holdExpiresAt = now.plusMinutes(5);

        // Temporarily hold the selected seats.
        for (Seat seat : selectedSeats) {
            seat.setStatus(SeatStatus.HELD);
            seat.setHoldExpiresAt(holdExpiresAt);
        }

        seatRepository.saveAll(selectedSeats);

        // Create a booking containing the event, seats, amount and deadline.
        Booking booking = new Booking(
                event,
                new HashSet<>(selectedSeats),
                totalAmount,
                holdExpiresAt);

        bookingRepository.save(booking);

        // Convert seats into response DTOs.
        List<SeatResponse> seatResponses = selectedSeats.stream()
                .map(SeatResponse::from)
                .collect(Collectors.toList());

        // Return booking details to the customer.
        return new HoldSeatsResponse(
                booking.getBookingCode(),
                booking.getStatus(),
                booking.getTotalAmount(),
                booking.getHoldExpiresAt(),
                seatResponses);
    }

    // Processes the simulated payment result.
    @Transactional
    public PaymentResponse processPayment(
            String bookingCode, PaymentRequest request) {

        // Lock the booking to prevent concurrent payment or cancellation.
        Booking booking = bookingRepository
                .findByBookingCodeForUpdate(bookingCode)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Booking not found"));

        LocalDateTime now = LocalDateTime.now();

        // Only bookings awaiting payment can be processed.
        if (booking.getStatus() != BookingStatus.HELD) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This booking is no longer awaiting payment");
        }

        // Reject payment if the hold deadline has passed.
        if (booking.getHoldExpiresAt() == null
                || !booking.getHoldExpiresAt().isAfter(now)) {

            // The scheduled task will release expired holds.
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "The seat hold has expired. Please book again.");
        }

        // Validate the simulated payment choice.
        if (request.getSuccess() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Payment success must be true or false");
        }

        if (Boolean.TRUE.equals(request.getSuccess())) {

            // Verify every seat is still held for this booking.
            for (Seat seat : booking.getSeats()) {
                if (seat.getStatus() != SeatStatus.HELD
                        || seat.getHoldExpiresAt() == null
                        || !seat.getHoldExpiresAt()
                                .equals(booking.getHoldExpiresAt())
                        || !seat.getHoldExpiresAt().isAfter(now)) {

                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "One or more seat holds are no longer valid");
                }
            }

            // Payment succeeded: mark seats as permanently booked.
            for (Seat seat : booking.getSeats()) {
                seat.setStatus(SeatStatus.BOOKED);
                seat.setHoldExpiresAt(null);
            }

            seatRepository.saveAll(booking.getSeats());

            booking.setStatus(BookingStatus.CONFIRMED);
            booking.setHoldExpiresAt(null);
            bookingRepository.save(booking);

            return new PaymentResponse(
                    booking.getBookingCode(),
                    booking.getStatus(),
                    booking.getTotalAmount(),
                    "Payment successful. Booking confirmed.");
        }

        // Payment failed: release seats still held for this booking.
        for (Seat seat : booking.getSeats()) {
            if (seat.getStatus() == SeatStatus.HELD
                    && seat.getHoldExpiresAt() != null
                    && seat.getHoldExpiresAt()
                            .equals(booking.getHoldExpiresAt())) {

                seat.setStatus(SeatStatus.AVAILABLE);
                seat.setHoldExpiresAt(null);
            }
        }

        seatRepository.saveAll(booking.getSeats());

        booking.setStatus(BookingStatus.FAILED);
        booking.setHoldExpiresAt(null);
        bookingRepository.save(booking);

        return new PaymentResponse(
                booking.getBookingCode(),
                booking.getStatus(),
                booking.getTotalAmount(),
                "Payment failed. The held seats have been released.");
    }

    // Cancels a confirmed booking and simulates a full refund.
    @Transactional
    public CancellationResponse cancelBooking(String bookingCode) {

        // Lock the booking before checking its current status.
        Booking booking = bookingRepository
                .findByBookingCodeForUpdate(bookingCode)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Booking not found"));

        // Only confirmed bookings can be cancelled.
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only confirmed bookings can be cancelled");
        }

        // Verify all seats are booked before releasing any of them.
        for (Seat seat : booking.getSeats()) {
            if (seat.getStatus() != SeatStatus.BOOKED) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "A seat is not in the expected booked state");
            }
        }

        // Release the seats for future customers.
        for (Seat seat : booking.getSeats()) {
            seat.setStatus(SeatStatus.AVAILABLE);
            seat.setHoldExpiresAt(null);
        }

        seatRepository.saveAll(booking.getSeats());

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        // Simulated refund only; no real payment provider is used.
        return new CancellationResponse(
                booking.getBookingCode(),
                booking.getStatus(),
                booking.getTotalAmount(),
                booking.getTotalAmount(),
                "Booking cancelled. A simulated full refund has been issued.");
    }

    // Returns bookings for an event, newest first.
    @Transactional(readOnly = true)
    public List<BookingListResponse> getBookingsForEvent(Long eventId) {

        // Confirm the event exists before querying its bookings.
        if (!eventRepository.existsById(eventId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Event not found");
        }

        // Retrieve bookings in descending creation-time order.
        return bookingRepository
                .findAllByEventIdOrderByCreatedAtDesc(eventId)
                .stream()

                // Convert each booking entity into a response DTO.
                .map(booking -> new BookingListResponse(
                        booking.getBookingCode(),
                        booking.getEvent().getId(),
                        booking.getEvent().getName(),
                        booking.getStatus(),
                        booking.getTotalAmount(),
                        booking.getCreatedAt(),

                        // Return safe seat DTOs instead of raw entities.
                        booking.getSeats()
                                .stream()
                                .map(SeatResponse::from)
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
    }

    // NEW: Validates a ticket and records the customer's entry.
    @Transactional
    public CheckInResponse checkInBooking(String bookingCode) {

        // Lock the booking row so concurrent check-in requests wait.
        Booking booking = bookingRepository
                .findByBookingCodeForUpdate(bookingCode)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Ticket not found"));

        // Only paid and confirmed bookings are valid for entry.
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ticket is not valid for entry. Booking is "
                            + booking.getStatus());
        }

        // Reject a second attempt to use the same booking.
        if (booking.isCheckedIn()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ticket has already been checked in at "
                            + booking.getCheckedInAt());
        }

        // Record the current server time and mark the ticket as used.
        LocalDateTime checkInTime = LocalDateTime.now();
        booking.markCheckedIn(checkInTime);

        // Save the check-in status and timestamp in MySQL.
        bookingRepository.save(booking);

        // Return the validation result to the staff interface.
        return new CheckInResponse(
                booking.getBookingCode(),
                booking.getEvent().getName(),
                booking.getStatus(),
                booking.isCheckedIn(),
                booking.getCheckedInAt(),
                "Ticket validated successfully. Entry permitted.");
    }

    // Runs every ten seconds to release expired holds automatically.
    @Scheduled(fixedDelay = 10000)
    @Transactional
    public void releaseExpiredBookingsAutomatically() {
        expireOldBookings(LocalDateTime.now());
    }

    // Marks expired bookings and releases their corresponding seats.
    private void expireOldBookings(LocalDateTime now) {

        List<Booking> expiredBookings =
                bookingRepository
                        .findByStatusAndHoldExpiresAtLessThanEqual(
                                BookingStatus.HELD, now);

        for (Booking booking : expiredBookings) {

            LocalDateTime bookingExpiry = booking.getHoldExpiresAt();

            if (bookingExpiry == null || bookingExpiry.isAfter(now)) {
                continue;
            }

            // Release only seats still held for this booking.
            for (Seat seat : booking.getSeats()) {
                if (seat.getStatus() == SeatStatus.HELD
                        && bookingExpiry.equals(seat.getHoldExpiresAt())) {

                    seat.setStatus(SeatStatus.AVAILABLE);
                    seat.setHoldExpiresAt(null);
                }
            }

            seatRepository.saveAll(booking.getSeats());

            booking.setStatus(BookingStatus.EXPIRED);
            booking.setHoldExpiresAt(null);
            bookingRepository.save(booking);
        }
    }
}
