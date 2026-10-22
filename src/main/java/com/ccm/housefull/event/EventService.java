
package com.ccm.housefull.event;

import com.ccm.housefull.dto.CreateEventRequest;
import com.ccm.housefull.dto.SeatRowRequest;
import com.ccm.housefull.dto.SeatResponse;
import com.ccm.housefull.seat.Seat;
import com.ccm.housefull.seat.SeatRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;

    public EventService(
            EventRepository eventRepository,
            SeatRepository seatRepository) {
        this.eventRepository = eventRepository;
        this.seatRepository = seatRepository;
    }

    @Transactional
    public Event createEvent(CreateEventRequest request) {

        int totalSeats = request.getRows()
                .stream()
                .mapToInt(SeatRowRequest::getSeatCount)
                .sum();

        Set<String> rowLabels = new HashSet<>();

        for (SeatRowRequest row : request.getRows()) {
            String label = row.getRowLabel().trim().toUpperCase();

            if (!rowLabels.add(label)) {
                throw new IllegalArgumentException(
                        "Duplicate row label: " + label);
            }
        }

        Event event = new Event(
                request.getName(),
                request.getVenue(),
                request.getEventDateTime(),
                totalSeats
        );

        Event savedEvent = eventRepository.save(event);

        List<Seat> seats = new ArrayList<>();

        for (SeatRowRequest row : request.getRows()) {

            String rowLabel = row.getRowLabel().trim().toUpperCase();
            BigDecimal price = row.getPrice();

            for (int seatNumber = 1;
                    seatNumber <= row.getSeatCount();
                    seatNumber++) {

                Seat seat = new Seat(
                        savedEvent,
                        rowLabel,
                        seatNumber,
                        price
                );

                seats.add(seat);
            }
        }

        seatRepository.saveAll(seats);

        return savedEvent;
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public List<SeatResponse> getSeatsForEvent(Long eventId) {
        return seatRepository
                .findByEventIdOrderByRowLabelAscSeatNumberAsc(eventId)
                .stream()
                .map(SeatResponse::from)
                .collect(Collectors.toList());
    }
}
