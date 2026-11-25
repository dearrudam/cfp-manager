package com.dearrudam.proposal.boundary;

import java.time.Instant;

import com.dearrudam.proposal.entity.Proposal;

public record ProposalResponse(
        Long id,
        String title,
        String description,
        Long cfpId,
        Long eventId,
        String eventName,
        Long speakerId,
        String speakerName,
        Instant submittedAt) {

    public static ProposalResponse from(Proposal proposal) {
        return new ProposalResponse(
                proposal.id,
                proposal.title,
                proposal.description,
                proposal.cfp.id,
                proposal.cfp.event.id,
                proposal.cfp.event.name,
                proposal.speaker.id,
                proposal.speaker.name,
                proposal.submittedAt);
    }
}
