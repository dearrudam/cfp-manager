package com.dearrudam.review.boundary;

import java.time.Instant;

import com.dearrudam.proposal.entity.Proposal;

public record QueuedProposalResponse(
        Long id,
        String title,
        String ownerName,
        Instant createdAt) {

    public static QueuedProposalResponse from(Proposal proposal) {
        return new QueuedProposalResponse(
                proposal.id,
                proposal.title,
                proposal.owner.name,
                proposal.createdAt);
    }
}
