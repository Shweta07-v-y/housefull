
package com.ccm.housefull.seat;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    // Retrieves an event's seats in a predictable display order.
    List<Seat> findByEventIdOrderByRowLabelAscSeatNumberAsc(Long eventId);

    // Locks the selected seat rows while a booking transaction is running.
    // This helps prevent two customers from holding the same seat.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Seat s WHERE s.id IN :seatIds ORDER BY s.id")
    List<Seat> findSelectedSeatsForUpdate(
            @Param("seatIds") List<Long> seatIds);
}
