
// // package com.ccm.housefull.booking;

// // import org.springframework.data.jpa.repository.JpaRepository;

// // import java.time.LocalDateTime;
// // import java.util.List;
// // import java.util.Optional;

// // // Provides database access for booking records.
// // public interface BookingRepository extends JpaRepository<Booking, Long> {

// //     // Finds a booking using its unique reference code.
// //     Optional<Booking> findByBookingCode(String bookingCode);

// //     // Finds temporary bookings whose expiry deadline has passed.
// //     List<Booking> findByStatusAndHoldExpiresAtLessThanEqual(
// //             BookingStatus status,
// //             LocalDateTime currentTime);
// // }










// package com.ccm.housefull.booking;

// import jakarta.persistence.LockModeType;

// import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.Lock;
// import org.springframework.data.jpa.repository.Query;
// import org.springframework.data.repository.query.Param;

// import java.time.LocalDateTime;
// import java.util.List;
// import java.util.Optional;

// // Provides database access for booking records.
// public interface BookingRepository extends JpaRepository<Booking, Long> {

//     // Finds a booking by its unique customer-facing reference.
//     Optional<Booking> findByBookingCode(String bookingCode);

//     // Locks a booking row while payment or cancellation is processed.
//     // Another transaction requesting the same lock must wait.
//     @Lock(LockModeType.PESSIMISTIC_WRITE)
//     @Query("SELECT b FROM Booking b WHERE b.bookingCode = :bookingCode")
//     Optional<Booking> findByBookingCodeForUpdate(
//             @Param("bookingCode") String bookingCode);

//     // Finds temporary bookings whose hold deadlines have passed.
//     List<Booking> findByStatusAndHoldExpiresAtLessThanEqual(
//             BookingStatus status,
//             LocalDateTime currentTime);
// }





package com.ccm.housefull.booking;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// Provides database access for booking records.
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Finds a booking using its unique customer-facing reference code.
    Optional<Booking> findByBookingCode(String bookingCode);

    // Locks a booking row while payment or cancellation is processed.
    // The lock is held until the surrounding transaction completes.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Booking b WHERE b.bookingCode = :bookingCode")
    Optional<Booking> findByBookingCodeForUpdate(
            @Param("bookingCode") String bookingCode);

    // Finds temporary bookings whose hold expiry deadline has passed.
    List<Booking> findByStatusAndHoldExpiresAtLessThanEqual(
            BookingStatus status,
            LocalDateTime currentTime);

    // Retrieves all bookings associated with a particular event.
    @Query("SELECT b FROM Booking b WHERE b.event.id = :eventId")
    List<Booking> findAllByEventId(
            @Param("eventId") Long eventId);
}
