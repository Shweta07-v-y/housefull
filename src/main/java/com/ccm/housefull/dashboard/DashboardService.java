
package com.ccm.housefull.dashboard;

import com.ccm.housefull.booking.Booking;
import com.ccm.housefull.booking.BookingRepository;
import com.ccm.housefull.booking.BookingStatus;
import com.ccm.housefull.event.Event;
import com.ccm.housefull.event.EventRepository;
import com.ccm.housefull.seat.Seat;
import com.ccm.housefull.seat.SeatRepository;
import com.ccm.housefull.seat.SeatStatus;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

// Contains the business logic for calculating dashboard statistics.
@Service
public class DashboardService {

    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;
    private final BookingRepository bookingRepository;

    // Spring injects these repositories when creating the service.
    public DashboardService(
            EventRepository eventRepository,
            SeatRepository seatRepository,
            BookingRepository bookingRepository) {

        this.eventRepository = eventRepository;
        this.seatRepository = seatRepository;
        this.bookingRepository = bookingRepository;
    }

    // This method only reads data, so we mark the transaction read-only.
    @Transactional(readOnly = true)
    public DashboardResponse getEventDashboard(Long eventId) {

        // Step 1: Find the event or return HTTP 404 if it does not exist.
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Event not found"));

        // Step 2: Retrieve all seats belonging to this event.
        List<Seat> seats =
                seatRepository.findByEventIdOrderByRowLabelAscSeatNumberAsc(
                        eventId);

        // Step 3: Retrieve all bookings belonging to this event.
        List<Booking> bookings =
                bookingRepository.findAllByEventId(eventId);

        // Step 4: Count seats according to their current database status.
        long seatsSold = seats.stream()
                .filter(seat -> seat.getStatus() == SeatStatus.BOOKED)
                .count();

        long seatsHeld = seats.stream()
                .filter(seat -> seat.getStatus() == SeatStatus.HELD)
                .count();

        long seatsAvailable = seats.stream()
                .filter(seat -> seat.getStatus() == SeatStatus.AVAILABLE)
                .count();

        // Step 5: Count bookings according to their status.
        long confirmedBookings =
                countBookings(bookings, BookingStatus.CONFIRMED);

        long failedBookings =
                countBookings(bookings, BookingStatus.FAILED);

        long cancelledBookings =
                countBookings(bookings, BookingStatus.CANCELLED);

        long expiredBookings =
                countBookings(bookings, BookingStatus.EXPIRED);

        long pendingBookings =
                countBookings(bookings, BookingStatus.HELD);

        // Step 6: Count confirmed tickets that have been checked in.
        // Only confirmed bookings count as valid tickets for entry.
        long checkedInCount = bookings.stream()
                .filter(booking ->
                        booking.getStatus() == BookingStatus.CONFIRMED
                                && booking.isCheckedIn())
                .count();

        // Step 7: Count confirmed bookings whose tickets have not
        // yet been checked in.
        long notCheckedInCount =
                confirmedBookings - checkedInCount;

        // Step 8: Calculate revenue from currently confirmed bookings.
        // Cancelled bookings are excluded from this figure.
        BigDecimal revenueCollected = bookings.stream()
                .filter(booking ->
                        booking.getStatus() == BookingStatus.CONFIRMED)
                .map(Booking::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Step 9: Return all calculated values as a single response.
        return new DashboardResponse(
                event.getId(),
                event.getName(),
                seats.size(),
                seatsSold,
                seatsHeld,
                seatsAvailable,
                bookings.size(),
                confirmedBookings,
                failedBookings,
                cancelledBookings,
                expiredBookings,
                pendingBookings,
                checkedInCount,
                notCheckedInCount,
                revenueCollected
        );
    }

    // Reusable helper: counts bookings that match a particular status.
    private long countBookings(
            List<Booking> bookings,
            BookingStatus status) {

        return bookings.stream()
                .filter(booking -> booking.getStatus() == status)
                .count();
    }
}
