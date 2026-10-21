
package com.ccm.housefull.booking;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// Provides database access for booking records.
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Finds a booking using its unique reference code.
    Optional<Booking> findByBookingCode(String bookingCode);

    // Finds temporary bookings whose expiry deadline has passed.
    List<Booking> findByStatusAndHoldExpiresAtLessThanEqual(
            BookingStatus status,
            LocalDateTime currentTime);
}
