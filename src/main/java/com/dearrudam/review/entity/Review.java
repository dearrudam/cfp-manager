package com.dearrudam.review.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.dearrudam.proposal.entity.Proposal;
import com.dearrudam.user.entity.User;

@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "proposal_id", nullable = false)
    public Proposal proposal;

    @ManyToOne(optional = false)
    @JoinColumn(name = "reviewer_id", nullable = false)
    public User reviewer;

    @Column(nullable = false)
    public Integer score;

    @Column(columnDefinition = "TEXT", nullable = false)
    public String comment;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    public Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    public Instant updatedAt;
}
