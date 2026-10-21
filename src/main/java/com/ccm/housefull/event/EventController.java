
package com.ccm.housefull.event;

import com.ccm.housefull.dto.CreateEventRequest;
import com.ccm.housefull.dto.SeatResponse;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    // Create an event and generate its seats
    @PostMapping
    public ResponseEntity<Event> createEvent(
            @Valid @RequestBody CreateEventRequest request) {

        Event savedEvent = eventService.createEvent(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedEvent);
    }

    // Get all events
    @GetMapping
    public List<Event> getAllEvents() {
        return eventService.getAllEvents();
    }

    // Get all seats for a particular event
    @GetMapping("/{eventId}/seats")
    public List<SeatResponse> getSeatsForEvent(
            @PathVariable Long eventId) {

        return eventService.getSeatsForEvent(eventId);
    }
}
