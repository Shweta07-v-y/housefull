
// // package com.ccm.housefull.booking;

// // import com.ccm.housefull.event.Event;
// // import com.ccm.housefull.event.EventRepository;
// // import com.ccm.housefull.seat.Seat;
// // import com.ccm.housefull.seat.SeatRepository;
// // import com.ccm.housefull.seat.SeatStatus;

// // import org.junit.jupiter.api.Test;
// // import org.junit.jupiter.api.extension.ExtendWith;

// // import org.mockito.InjectMocks;
// // import org.mockito.Mock;
// // import org.mockito.junit.jupiter.MockitoExtension;

// // import org.springframework.http.HttpStatus;
// // import org.springframework.test.util.ReflectionTestUtils;
// // import org.springframework.web.server.ResponseStatusException;

// // import java.math.BigDecimal;
// // import java.time.LocalDateTime;
// // import java.util.HashSet;
// // import java.util.List;
// // import java.util.Optional;

// // import static org.junit.jupiter.api.Assertions.assertEquals;
// // import static org.junit.jupiter.api.Assertions.assertThrows;
// // import static org.mockito.ArgumentMatchers.any;
// // import static org.mockito.Mockito.verify;
// // import static org.mockito.Mockito.when;

// // @ExtendWith(MockitoExtension.class)
// // class BookingServiceTest {

// //     @Mock
// //     private BookingRepository bookingRepository;

// //     @Mock
// //     private SeatRepository seatRepository;

// //     @Mock
// //     private EventRepository eventRepository;

// //     @InjectMocks
// //     private BookingService bookingService;

// //     @Test
// //     void shouldRejectSeatThatIsAlreadyHeld() {

// //         // Arrange: create an event with an ID.
// //         Event event = new Event(
// //                 "Test Concert",
// //                 "Bengaluru",
// //                 LocalDateTime.now().plusDays(1),
// //                 10
// //         );

// //         ReflectionTestUtils.setField(event, "id", 100L);

// //         // Create a seat that is already held.
// //         Seat seat = new Seat(
// //                 event,
// //                 "A",
// //                 1,
// //                 new BigDecimal("500.00")
// //         );

// //         ReflectionTestUtils.setField(seat, "id", 1L);
// //         seat.setStatus(SeatStatus.HELD);

// //         when(eventRepository.findById(100L))
// //                 .thenReturn(Optional.of(event));

// //         when(seatRepository.findSelectedSeatsForUpdate(List.of(1L)))
// //                 .thenReturn(List.of(seat));

// //         // holdSeats() uses this to find expired bookings.
// //         when(bookingRepository
// //                 .findByStatusAndHoldExpiresAtLessThanEqual(any(), any()))
// //                 .thenReturn(List.of());

// //         HoldSeatsRequest request = new HoldSeatsRequest();
// //         request.setEventId(100L);
// //         request.setSeatIds(List.of(1L));

// //         // Act: try to hold a seat that is already held.
// //         ResponseStatusException exception = assertThrows(
// //                 ResponseStatusException.class,
// //                 () -> bookingService.holdSeats(request)
// //         );

// //         // Assert: the request should return HTTP 409 CONFLICT.
// //         assertEquals(
// //                 HttpStatus.CONFLICT,
// //                 exception.getStatusCode()
// //         );
// //     }

// //     @Test
// //     void shouldConfirmBookingWhenPaymentSucceeds() {

// //         // Arrange: create an event with an ID.
// //         Event event = new Event(
// //                 "Test Concert",
// //                 "Bengaluru",
// //                 LocalDateTime.now().plusDays(1),
// //                 10
// //         );

// //         ReflectionTestUtils.setField(event, "id", 100L);

// //         // Create a seat that is currently held.
// //         Seat seat = new Seat(
// //                 event,
// //                 "A",
// //                 1,
// //                 new BigDecimal("500.00")
// //         );

// //         ReflectionTestUtils.setField(seat, "id", 1L);

// //         LocalDateTime expiry = LocalDateTime.now().plusMinutes(5);

// //         seat.setStatus(SeatStatus.HELD);
// //         seat.setHoldExpiresAt(expiry);

// //         // Create a booking containing the held seat.
// //         Booking booking = new Booking(
// //                 event,
// //                 new HashSet<>(List.of(seat)),
// //                 new BigDecimal("500.00"),
// //                 expiry
// //         );

// //         when(bookingRepository.findByBookingCodeForUpdate(
// //                 booking.getBookingCode()))
// //                 .thenReturn(Optional.of(booking));

// //         // Create a request that simulates successful payment.
// //         PaymentRequest paymentRequest = new PaymentRequest();
// //         paymentRequest.setSuccess(true);

// //         // Act: process the payment.
// //         PaymentResponse response = bookingService.processPayment(
// //                 booking.getBookingCode(),
// //                 paymentRequest
// //         );

// //         // Assert: the booking and seat should be confirmed.
// //         assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
// //         assertEquals(SeatStatus.BOOKED, seat.getStatus());

// //         assertEquals(BookingStatus.CONFIRMED, response.getStatus());
// //         assertEquals(
// //                 new BigDecimal("500.00"),
// //                 response.getTotalAmount()
// //         );

// //         verify(bookingRepository).save(booking);
// //         verify(seatRepository).saveAll(any());
// //     }
// // }




// package com.ccm.housefull.booking;

// import com.ccm.housefull.event.Event;
// import com.ccm.housefull.event.EventRepository;
// import com.ccm.housefull.seat.Seat;
// import com.ccm.housefull.seat.SeatRepository;
// import com.ccm.housefull.seat.SeatStatus;

// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;

// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;

// import org.springframework.http.HttpStatus;
// import org.springframework.test.util.ReflectionTestUtils;
// import org.springframework.web.server.ResponseStatusException;

// import java.math.BigDecimal;
// import java.time.LocalDateTime;
// import java.util.HashSet;
// import java.util.List;
// import java.util.Optional;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertThrows;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.eq;
// import static org.mockito.Mockito.times;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;

// @ExtendWith(MockitoExtension.class)
// class BookingServiceTest {

//     @Mock
//     private BookingRepository bookingRepository;

//     @Mock
//     private SeatRepository seatRepository;

//     @Mock
//     private EventRepository eventRepository;

//     @InjectMocks
//     private BookingService bookingService;

//     @Test
//     void shouldRejectSeatThatIsAlreadyHeld() {

//         // Arrange: create an event with an ID.
//         Event event = new Event(
//                 "Test Concert",
//                 "Bengaluru",
//                 LocalDateTime.now().plusDays(1),
//                 10
//         );

//         ReflectionTestUtils.setField(event, "id", 100L);

//         // Create a seat that is already held.
//         Seat seat = new Seat(
//                 event,
//                 "A",
//                 1,
//                 new BigDecimal("500.00")
//         );

//         ReflectionTestUtils.setField(seat, "id", 1L);
//         seat.setStatus(SeatStatus.HELD);

//         when(eventRepository.findById(100L))
//                 .thenReturn(Optional.of(event));

//         when(seatRepository.findSelectedSeatsForUpdate(List.of(1L)))
//                 .thenReturn(List.of(seat));

//         // holdSeats() first searches for expired bookings.
//         when(bookingRepository
//                 .findByStatusAndHoldExpiresAtLessThanEqual(
//                         eq(BookingStatus.HELD),
//                         any(LocalDateTime.class)
//                 ))
//                 .thenReturn(List.of());

//         HoldSeatsRequest request = new HoldSeatsRequest();
//         request.setEventId(100L);
//         request.setSeatIds(List.of(1L));

//         // Act: try to hold a seat that is already held.
//         ResponseStatusException exception = assertThrows(
//                 ResponseStatusException.class,
//                 () -> bookingService.holdSeats(request)
//         );

//         // Assert: the service rejects the request with HTTP 409.
//         assertEquals(
//                 HttpStatus.CONFLICT,
//                 exception.getStatusCode()
//         );
//     }

//     @Test
//     void shouldConfirmBookingWhenPaymentSucceeds() {

//         // Arrange: create an event with an ID.
//         Event event = new Event(
//                 "Test Concert",
//                 "Bengaluru",
//                 LocalDateTime.now().plusDays(1),
//                 10
//         );

//         ReflectionTestUtils.setField(event, "id", 100L);

//         // Create a seat that is currently held.
//         Seat seat = new Seat(
//                 event,
//                 "A",
//                 1,
//                 new BigDecimal("500.00")
//         );

//         ReflectionTestUtils.setField(seat, "id", 1L);

//         LocalDateTime expiry = LocalDateTime.now().plusMinutes(5);

//         seat.setStatus(SeatStatus.HELD);
//         seat.setHoldExpiresAt(expiry);

//         // Create a booking containing the held seat.
//         Booking booking = new Booking(
//                 event,
//                 new HashSet<>(List.of(seat)),
//                 new BigDecimal("500.00"),
//                 expiry
//         );

//         when(bookingRepository.findByBookingCodeForUpdate(
//                 booking.getBookingCode()))
//                 .thenReturn(Optional.of(booking));

//         // Create a request that simulates successful payment.
//         PaymentRequest paymentRequest = new PaymentRequest();
//         paymentRequest.setSuccess(true);

//         // Act: process the payment.
//         PaymentResponse response = bookingService.processPayment(
//                 booking.getBookingCode(),
//                 paymentRequest
//         );

//         // Assert: the booking and seat should be confirmed.
//         assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
//         assertEquals(SeatStatus.BOOKED, seat.getStatus());

//         assertEquals(BookingStatus.CONFIRMED, response.getStatus());
//         assertEquals(
//                 new BigDecimal("500.00"),
//                 response.getTotalAmount()
//         );

//         verify(bookingRepository).save(booking);
//         verify(seatRepository).saveAll(any());
//     }

//     @Test
//     void shouldExpireBookingAndReleaseSeatsWhenHoldDeadlineHasPassed() {

//         // Arrange: create an event with an ID.
//         Event event = new Event(
//                 "Test Concert",
//                 "Bengaluru",
//                 LocalDateTime.now().plusDays(1),
//                 10
//         );

//         ReflectionTestUtils.setField(event, "id", 100L);

//         // Set the hold deadline six minutes in the past.
//         LocalDateTime expiredTime = LocalDateTime.now().minusMinutes(6);

//         Seat seat = new Seat(
//                 event,
//                 "A",
//                 1,
//                 new BigDecimal("500.00")
//         );

//         ReflectionTestUtils.setField(seat, "id", 1L);
//         seat.setStatus(SeatStatus.HELD);
//         seat.setHoldExpiresAt(expiredTime);

//         // Create a booking that is still awaiting payment.
//         Booking booking = new Booking(
//                 event,
//                 new HashSet<>(List.of(seat)),
//                 new BigDecimal("500.00"),
//                 expiredTime
//         );

//         // Simulate the repository finding this expired booking.
//         when(bookingRepository
//                 .findByStatusAndHoldExpiresAtLessThanEqual(
//                         eq(BookingStatus.HELD),
//                         any(LocalDateTime.class)
//                 ))
//                 .thenReturn(List.of(booking));

//         // Act: run the expiry process without waiting five minutes.
//         bookingService.releaseExpiredBookingsAutomatically();

//         // Assert: the booking is marked EXPIRED.
//         assertEquals(BookingStatus.EXPIRED, booking.getStatus());

//         // Assert: the seat becomes available again.
//         assertEquals(SeatStatus.AVAILABLE, seat.getStatus());

//         // Assert: temporary expiry timestamps are cleared.
//         assertEquals(null, booking.getHoldExpiresAt());
//         assertEquals(null, seat.getHoldExpiresAt());

//         // Verify that the changed entities are saved.
//         verify(bookingRepository, times(1)).save(booking);
//         verify(seatRepository, times(1)).saveAll(booking.getSeats());
//     }
// }



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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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

    // ---------------------------------------------------------
    // TEST 1: Reject a seat that is already held
    // ---------------------------------------------------------

    @Test
    void shouldRejectSeatThatIsAlreadyHeld() {

        Event event = createEvent();
        Seat seat = createSeat(event, SeatStatus.HELD);

        when(eventRepository.findById(100L))
                .thenReturn(Optional.of(event));

        when(seatRepository.findSelectedSeatsForUpdate(List.of(1L)))
                .thenReturn(List.of(seat));

        when(bookingRepository
                .findByStatusAndHoldExpiresAtLessThanEqual(
                        eq(BookingStatus.HELD),
                        any(LocalDateTime.class)))
                .thenReturn(List.of());

        HoldSeatsRequest request = new HoldSeatsRequest();
        request.setEventId(100L);
        request.setSeatIds(List.of(1L));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> bookingService.holdSeats(request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals(SeatStatus.HELD, seat.getStatus());
    }

    // ---------------------------------------------------------
    // TEST 2: Confirm booking after successful payment
    // ---------------------------------------------------------

    @Test
    void shouldConfirmBookingWhenPaymentSucceeds() {

        Event event = createEvent();
        Seat seat = createSeat(event, SeatStatus.HELD);

        LocalDateTime expiry = LocalDateTime.now().plusMinutes(5);
        seat.setHoldExpiresAt(expiry);

        Booking booking = new Booking(
                event,
                new HashSet<>(List.of(seat)),
                new BigDecimal("500.00"),
                expiry
        );

        when(bookingRepository.findByBookingCodeForUpdate(
                booking.getBookingCode()))
                .thenReturn(Optional.of(booking));

        PaymentRequest request = new PaymentRequest();
        request.setSuccess(true);

        PaymentResponse response = bookingService.processPayment(
                booking.getBookingCode(),
                request
        );

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

    // ---------------------------------------------------------
    // TEST 3: Expire booking and release its seat
    // ---------------------------------------------------------

    @Test
    void shouldExpireBookingAndReleaseSeatsWhenHoldDeadlineHasPassed() {

        Event event = createEvent();
        LocalDateTime expiredTime = LocalDateTime.now().minusMinutes(6);

        Seat seat = createSeat(event, SeatStatus.HELD);
        seat.setHoldExpiresAt(expiredTime);

        Booking booking = new Booking(
                event,
                new HashSet<>(List.of(seat)),
                new BigDecimal("500.00"),
                expiredTime
        );

        when(bookingRepository
                .findByStatusAndHoldExpiresAtLessThanEqual(
                        eq(BookingStatus.HELD),
                        any(LocalDateTime.class)))
                .thenReturn(List.of(booking));

        bookingService.releaseExpiredBookingsAutomatically();

        assertEquals(BookingStatus.EXPIRED, booking.getStatus());
        assertEquals(SeatStatus.AVAILABLE, seat.getStatus());
        assertEquals(null, booking.getHoldExpiresAt());
        assertEquals(null, seat.getHoldExpiresAt());

        verify(bookingRepository, times(1)).save(booking);
        verify(seatRepository, times(1)).saveAll(any());
    }

    // ---------------------------------------------------------
    // TEST 4: Cancel confirmed booking and simulate full refund
    // ---------------------------------------------------------

    @Test
    void shouldCancelConfirmedBookingAndReleaseSeats() {

        Event event = createEvent();
        Seat seat = createSeat(event, SeatStatus.BOOKED);

        Booking booking = new Booking(
                event,
                new HashSet<>(List.of(seat)),
                new BigDecimal("500.00"),
                null
        );

        booking.setStatus(BookingStatus.CONFIRMED);

        when(bookingRepository.findByBookingCodeForUpdate(
                booking.getBookingCode()))
                .thenReturn(Optional.of(booking));

        CancellationResponse response =
                bookingService.cancelBooking(booking.getBookingCode());

        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        assertEquals(SeatStatus.AVAILABLE, seat.getStatus());
        assertEquals(null, seat.getHoldExpiresAt());
        assertEquals(
                new BigDecimal("500.00"),
                response.getTotalAmount()
        );
        assertEquals(
                new BigDecimal("500.00"),
                response.getRefundAmount()
        );

        verify(seatRepository).saveAll(any());
        verify(bookingRepository).save(booking);
    }

    // ---------------------------------------------------------
    // TEST 5: Reject cancellation after customer checks in
    // ---------------------------------------------------------

    @Test
    void shouldRejectCancellationAfterCheckIn() {

        Event event = createEvent();
        Seat seat = createSeat(event, SeatStatus.BOOKED);

        Booking booking = new Booking(
                event,
                new HashSet<>(List.of(seat)),
                new BigDecimal("500.00"),
                null
        );

        booking.setStatus(BookingStatus.CONFIRMED);
        booking.markCheckedIn(LocalDateTime.now());

        when(bookingRepository.findByBookingCodeForUpdate(
                booking.getBookingCode()))
                .thenReturn(Optional.of(booking));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> bookingService.cancelBooking(booking.getBookingCode())
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        assertEquals(SeatStatus.BOOKED, seat.getStatus());

        verify(bookingRepository, never()).save(any());
        verify(seatRepository, never()).saveAll(any());
    }

    // ---------------------------------------------------------
    // TEST 6: Reject cancellation of an already cancelled booking
    // ---------------------------------------------------------

    @Test
    void shouldRejectCancellingTheSameBookingTwice() {

        Event event = createEvent();
        Seat seat = createSeat(event, SeatStatus.AVAILABLE);

        Booking booking = new Booking(
                event,
                new HashSet<>(List.of(seat)),
                new BigDecimal("500.00"),
                null
        );

        booking.setStatus(BookingStatus.CANCELLED);

        when(bookingRepository.findByBookingCodeForUpdate(
                booking.getBookingCode()))
                .thenReturn(Optional.of(booking));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> bookingService.cancelBooking(booking.getBookingCode())
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        verify(bookingRepository, never()).save(any());
        verify(seatRepository, never()).saveAll(any());
    }

    // ---------------------------------------------------------
    // TEST 7: Allow check-in for a confirmed booking
    // ---------------------------------------------------------

    @Test
    void shouldAllowCheckInForConfirmedBooking() {

        Event event = createEvent();
        Seat seat = createSeat(event, SeatStatus.BOOKED);

        Booking booking = new Booking(
                event,
                new HashSet<>(List.of(seat)),
                new BigDecimal("500.00"),
                null
        );

        booking.setStatus(BookingStatus.CONFIRMED);

        when(bookingRepository.findByBookingCodeForUpdate(
                booking.getBookingCode()))
                .thenReturn(Optional.of(booking));

        CheckInResponse response =
                bookingService.checkInBooking(booking.getBookingCode());

        assertEquals(true, booking.isCheckedIn());
        assertEquals(true, response.isCheckedIn());
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());

        verify(bookingRepository).save(booking);
    }

    // ---------------------------------------------------------
    // TEST 8: Reject duplicate check-in
    // ---------------------------------------------------------

    @Test
    void shouldRejectCheckInWhenTicketWasAlreadyUsed() {

        Event event = createEvent();
        Seat seat = createSeat(event, SeatStatus.BOOKED);

        Booking booking = new Booking(
                event,
                new HashSet<>(List.of(seat)),
                new BigDecimal("500.00"),
                null
        );

        booking.setStatus(BookingStatus.CONFIRMED);
        booking.markCheckedIn(LocalDateTime.now().minusMinutes(2));

        when(bookingRepository.findByBookingCodeForUpdate(
                booking.getBookingCode()))
                .thenReturn(Optional.of(booking));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> bookingService.checkInBooking(booking.getBookingCode())
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals(true, booking.isCheckedIn());

        verify(bookingRepository, never()).save(any());
    }

    // ---------------------------------------------------------
    // HELPER METHODS
    // ---------------------------------------------------------

    private Event createEvent() {

        Event event = new Event(
                "Test Concert",
                "Bengaluru",
                LocalDateTime.now().plusDays(1),
                10
        );

        ReflectionTestUtils.setField(event, "id", 100L);

        return event;
    }

    private Seat createSeat(Event event, SeatStatus status) {

        Seat seat = new Seat(
                event,
                "A",
                1,
                new BigDecimal("500.00")
        );

        ReflectionTestUtils.setField(seat, "id", 1L);
        seat.setStatus(status);

        return seat;
    }
}
