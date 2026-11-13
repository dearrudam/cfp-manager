package com.dearrudam.proposal.boundary;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.dearrudam.draft.entity.Draft;
import com.dearrudam.proposal.entity.Proposal;
import com.dearrudam.proposal.entity.ProposalSpeaker;
import com.dearrudam.proposal.entity.ProposalStatus;
import com.dearrudam.proposal.entity.TalkLevel;

public record ProposalResponse(
        Long id,
        String title,
        @JsonProperty("abstract") String abstractText,
        TalkLevel level,
        Integer durationMinutes,
        ProposalStatus status,
        Long cfpId,
        Long eventId,
        Long ownerId,
        String ownerName,
        List<ProposalSpeakerResponse> speakers,
        Instant createdAt,
        @JsonInclude(JsonInclude.Include.NON_NULL) Instant updatedAt,
        @JsonInclude(JsonInclude.Include.NON_NULL) Long draftId) {

    public static ProposalResponse from(Proposal proposal, List<ProposalSpeaker> speakers, Draft draft) {
        return new ProposalResponse(
                proposal.id,
                proposal.title,
                proposal.abstractText,
                proposal.level,
                proposal.durationMinutes,
                proposal.status,
                proposal.cfp.id,
                proposal.cfp.event.id,
                proposal.owner.id,
                proposal.owner.name,
                speakers.stream().map(ProposalSpeakerResponse::from).toList(),
                proposal.createdAt,
                proposal.updatedAt,
                draft == null ? null : draft.id);
    }
}
