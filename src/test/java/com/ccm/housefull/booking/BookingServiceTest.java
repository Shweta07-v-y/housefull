
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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

        // Arrange: create an event for the test.
        Event event = new Event(
                "Test Concert",
                "Bengaluru",
                LocalDateTime.now().plusDays(1),
                10
        );

        // Assign an ID because this event has not been saved to the database.
        ReflectionTestUtils.setField(event, "id", 100L);

        // Arrange: create a seat belonging to this event.
        Seat seat = new Seat(
                event,
                "A",
                1,
                new BigDecimal("500.00")
        );

        // The seat is already held by another customer.
        seat.setStatus(SeatStatus.HELD);

        // Mock the event lookup.
        when(eventRepository.findById(100L))
                .thenReturn(Optional.of(event));

        // Mock the selected-seat lookup.
        when(seatRepository.findSelectedSeatsForUpdate(List.of(1L)))
                .thenReturn(List.of(seat));

        // No expired bookings need to be released for this test.
        when(bookingRepository
                .findByStatusAndHoldExpiresAtLessThanEqual(any(), any()))
                .thenReturn(List.of());

        // Create the customer's booking request.
        HoldSeatsRequest request = new HoldSeatsRequest();
        request.setEventId(100L);
        request.setSeatIds(List.of(1L));

        // Act: try to hold the already-held seat.
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> bookingService.holdSeats(request)
        );

        // Assert: the service should reject the request with HTTP 409.
        assertEquals(
                HttpStatus.CONFLICT,
                exception.getStatusCode()
        );
    }
}
