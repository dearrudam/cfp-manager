package com.dearrudam.event.entity;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.orm.panache.PanacheRepository;

@ApplicationScoped
public class EventRepository implements PanacheRepository<Event> {
}
