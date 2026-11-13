package com.dearrudam.proposal.boundary;

import java.time.Instant;

import com.dearrudam.proposal.entity.ProposalSpeaker;

public record ProposalSpeakerResponse(
        Long id,
        Long userId,
        String name,
        Instant createdAt) {

    public static ProposalSpeakerResponse from(ProposalSpeaker speaker) {
        return new ProposalSpeakerResponse(
                speaker.id,
                speaker.user.id,
                speaker.user.name,
                speaker.createdAt);
    }
}
