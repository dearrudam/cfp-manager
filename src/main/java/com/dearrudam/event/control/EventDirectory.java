package com.dearrudam.event.control;

import java.util.List;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import com.dearrudam.event.entity.Event;
import com.dearrudam.event.entity.EventRepository;

import io.quarkus.panache.common.Sort;

@ApplicationScoped
public class EventDirectory {

    @Inject
    EventRepository events;

    public List<Event> list() {
        return events.listAll(Sort.by("startAt"));
    }

    public Event get(Long id) {
        return Optional.ofNullable(events.findById(id))
                .orElseThrow(EventNotFoundException::new);
    }
}
