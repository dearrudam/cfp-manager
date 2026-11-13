package com.dearrudam.proposal.entity;

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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.dearrudam.cfp.entity.Cfp;
import com.dearrudam.user.entity.User;

@Entity
@Table(name = "proposals")
public class Proposal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false)
    public String title;

    @Column(name = "abstract", columnDefinition = "TEXT", nullable = false)
    public String abstractText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public TalkLevel level;

    @Column(name = "duration_minutes", nullable = false)
    public Integer durationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public ProposalStatus status;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cfp_id")
    public Cfp cfp;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    public User owner;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    public Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    public Instant updatedAt;
}
