
package com.ccm.housefull.booking;

import com.ccm.housefull.event.Event;
import com.ccm.housefull.event.EventRepository;
import com.ccm.housefull.seat.Seat;
import com.ccm.housefull.seat.SeatRepository;
import com.ccm.housefull.seat.SeatStatus;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private BookingService bookingService;

    @Test
    void shouldRejectSeatThatIsAlreadyHeld() {

        // Arrange: create an event with an ID.
        Event event = new Event(
                "Test Concert",
                "Bengaluru",
                LocalDateTime.now().plusDays(1),
                10
        );

        ReflectionTestUtils.setField(event, "id", 100L);

        // Create a seat that is already held.
        Seat seat = new Seat(
                event,
                "A",
                1,
                new BigDecimal("500.00")
        );

        ReflectionTestUtils.setField(seat, "id", 1L);
        seat.setStatus(SeatStatus.HELD);

        when(eventRepository.findById(100L))
                .thenReturn(Optional.of(event));

        when(seatRepository.findSelectedSeatsForUpdate(List.of(1L)))
                .thenReturn(List.of(seat));

        // holdSeats() uses this to find expired bookings.
        when(bookingRepository
                .findByStatusAndHoldExpiresAtLessThanEqual(any(), any()))
                .thenReturn(List.of());

        HoldSeatsRequest request = new HoldSeatsRequest();
        request.setEventId(100L);
        request.setSeatIds(List.of(1L));

        // Act: try to hold a seat that is already held.
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> bookingService.holdSeats(request)
        );

        // Assert: the request should return HTTP 409 CONFLICT.
        assertEquals(
                HttpStatus.CONFLICT,
                exception.getStatusCode()
        );
    }

    @Test
    void shouldConfirmBookingWhenPaymentSucceeds() {

        // Arrange: create an event with an ID.
        Event event = new Event(
                "Test Concert",
                "Bengaluru",
                LocalDateTime.now().plusDays(1),
                10
        );

        ReflectionTestUtils.setField(event, "id", 100L);

        // Create a seat that is currently held.
        Seat seat = new Seat(
                event,
                "A",
                1,
                new BigDecimal("500.00")
        );

        ReflectionTestUtils.setField(seat, "id", 1L);

        LocalDateTime expiry = LocalDateTime.now().plusMinutes(5);

        seat.setStatus(SeatStatus.HELD);
        seat.setHoldExpiresAt(expiry);

        // Create a booking containing the held seat.
        Booking booking = new Booking(
                event,
                new HashSet<>(List.of(seat)),
                new BigDecimal("500.00"),
                expiry
        );

        when(bookingRepository.findByBookingCodeForUpdate(
                booking.getBookingCode()))
                .thenReturn(Optional.of(booking));

        // Create a request that simulates successful payment.
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setSuccess(true);

        // Act: process the payment.
        PaymentResponse response = bookingService.processPayment(
                booking.getBookingCode(),
                paymentRequest
        );

        // Assert: the booking and seat should be confirmed.
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        assertEquals(SeatStatus.BOOKED, seat.getStatus());

        assertEquals(BookingStatus.CONFIRMED, response.getStatus());
        assertEquals(
                new BigDecimal("500.00"),
                response.getTotalAmount()
        );

        verify(bookingRepository).save(booking);
        verify(seatRepository).saveAll(any());
    }
}
