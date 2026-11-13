package com.dearrudam.event.control;

import java.time.Instant;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import com.dearrudam.event.entity.Event;
import com.dearrudam.event.entity.EventRepository;
import com.dearrudam.user.control.InvalidCredentialsException;
import com.dearrudam.user.entity.UserRepository;

@ApplicationScoped
public class EventCreation {

    @Inject
    EventRepository events;

    @Inject
    UserRepository users;

    @Transactional
    public Event create(String name, String description, Instant startAt, Instant endAt, String ownerEmail) {
        if (!endAt.isAfter(startAt)) {
            throw new InvalidEventException("endAt must be after startAt");
        }
        var owner = users.findByEmail(ownerEmail)
                .orElseThrow(InvalidCredentialsException::new);
        var event = new Event();
        event.name = name;
        event.description = description;
        event.startAt = startAt;
        event.endAt = endAt;
        event.owner = owner;
        events.persist(event);
        return event;
    }
}
