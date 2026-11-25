package com.dearrudam.proposal.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;

import com.dearrudam.cfp.entity.Cfp;
import com.dearrudam.user.entity.User;

@Entity
@Table(name = "proposals")
public class Proposal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "cfp_id", nullable = false)
    public Cfp cfp;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "speaker_id", nullable = false)
    public User speaker;

    @Column(nullable = false)
    public String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    public String description;

    @CreationTimestamp
    @Column(name = "submitted_at", nullable = false)
    public Instant submittedAt;
}
