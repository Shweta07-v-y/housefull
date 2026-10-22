package com.ccm.housefull.event;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 200)
    private String venue;

    @Column(nullable = false)
    private LocalDateTime eventDateTime;

    @Column(nullable = false)
    private Integer totalSeats;

    public Event() {
    }

    public Event(String name, String venue,
                 LocalDateTime eventDateTime, Integer totalSeats) {
        this.name = name;
        this.venue = venue;
        this.eventDateTime = eventDateTime;
        this.totalSeats = totalSeats;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getVenue() {
        return venue;
    }

    public LocalDateTime getEventDateTime() {
        return eventDateTime;
    }

    public Integer getTotalSeats() {
        return totalSeats;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public void setEventDateTime(LocalDateTime eventDateTime) {
        this.eventDateTime = eventDateTime;
    }

    public void setTotalSeats(Integer totalSeats) {
        this.totalSeats = totalSeats;
    }
}