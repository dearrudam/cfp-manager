package com.dearrudam.evaluation.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.hibernate.annotations.CreationTimestamp;

import com.dearrudam.proposal.entity.Proposal;
import com.dearrudam.user.entity.User;

@Entity
@Table(name = "evaluations", uniqueConstraints = @UniqueConstraint(columnNames = { "proposal_id", "evaluator_id" }))
public class Evaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "proposal_id", nullable = false)
    public Proposal proposal;

    @ManyToOne(optional = false)
    @JoinColumn(name = "evaluator_id", nullable = false)
    public User evaluator;

    @Column(nullable = false)
    public Integer score;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    public Instant createdAt;
}
