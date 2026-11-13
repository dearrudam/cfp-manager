package com.dearrudam.cfp.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;

import com.dearrudam.event.entity.Event;

@Entity
@Table(name = "cfps")
public class Cfp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "event_id", nullable = false, unique = true)
    public Event event;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public CfpStatus status;

    @Column(name = "opened_at")
    public Instant openedAt;

    @Column(name = "closed_at")
    public Instant closedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    public Instant createdAt;
}
