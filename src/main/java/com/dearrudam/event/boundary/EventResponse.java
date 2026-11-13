package com.dearrudam.event.boundary;

import java.time.Instant;

import com.dearrudam.event.entity.Event;

public record EventResponse(
        Long id,
        String name,
        String description,
        Instant startAt,
        Instant endAt,
        Long ownerId,
        String ownerName,
        Instant createdAt) {

    public static EventResponse from(Event event) {
        return new EventResponse(
                event.id,
                event.name,
                event.description,
                event.startAt,
                event.endAt,
                event.owner.id,
                event.owner.name,
                event.createdAt);
    }
}
