
package com.ccm.housefull.dashboard;

import com.ccm.housefull.booking.Booking;
import com.ccm.housefull.booking.BookingRepository;
import com.ccm.housefull.booking.BookingStatus;
import com.ccm.housefull.event.Event;
import com.ccm.housefull.event.EventRepository;
import com.ccm.housefull.seat.Seat;
import com.ccm.housefull.seat.SeatRepository;
import com.ccm.housefull.seat.SeatStatus;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;

// Calculates dashboard statistics for an event.
@Service
public class DashboardService {

    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;
    private final BookingRepository bookingRepository;

    public DashboardService(
            EventRepository eventRepository,
            SeatRepository seatRepository,
            BookingRepository bookingRepository) {

        this.eventRepository = eventRepository;
        this.seatRepository = seatRepository;
        this.bookingRepository = bookingRepository;
    }

    // Read-only because this method only retrieves and calculates statistics.
    @Transactional(readOnly = true)
    public DashboardResponse getEventDashboard(Long eventId) {

        // Check that the event exists before calculating its statistics.
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Event not found"));

        // Retrieve all seats belonging to the event.
        List<Seat> seats =
                seatRepository.findByEventIdOrderByRowLabelAscSeatNumberAsc(
                        eventId);

        // Retrieve all bookings for the event.
        List<Booking> bookings =
                bookingRepository.findAllByEventId(eventId);

        // Count seats by their current status.
        long seatsSold = seats.stream()
                .filter(seat -> seat.getStatus() == SeatStatus.BOOKED)
                .count();

        long seatsHeld = seats.stream()
                .filter(seat -> seat.getStatus() == SeatStatus.HELD)
                .count();

        long seatsAvailable = seats.stream()
                .filter(seat -> seat.getStatus() == SeatStatus.AVAILABLE)
                .count();

        // Count bookings by their status.
        long confirmedBookings = countBookings(
                bookings, BookingStatus.CONFIRMED);

        long failedBookings = countBookings(
                bookings, BookingStatus.FAILED);

        long cancelledBookings = countBookings(
                bookings, BookingStatus.CANCELLED);

        long expiredBookings = countBookings(
                bookings, BookingStatus.EXPIRED);

        long pendingBookings = countBookings(
                bookings, BookingStatus.HELD);

        // Add amounts only for confirmed bookings.
        // Failed, held, expired and cancelled bookings are excluded.
        BigDecimal revenueCollected = bookings.stream()
                .filter(booking ->
                        booking.getStatus() == BookingStatus.CONFIRMED)
                .map(Booking::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Return all statistics in one response object.
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
                revenueCollected
        );
    }

    // Reusable helper for counting bookings with a specific status.
    private long countBookings(
            List<Booking> bookings,
            BookingStatus status) {

        return bookings.stream()
                .filter(booking -> booking.getStatus() == status)
                .count();
    }
}
