package com.dearrudam.draft.boundary;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.dearrudam.draft.entity.Draft;
import com.dearrudam.proposal.entity.TalkLevel;

public record DraftResponse(
        Long id,
        String title,
        @JsonProperty("abstract") String abstractText,
        TalkLevel level,
        Integer durationMinutes,
        Long ownerId,
        String ownerName,
        Instant createdAt,
        @JsonInclude(JsonInclude.Include.NON_NULL) Instant updatedAt) {

    public static DraftResponse from(Draft draft) {
        return new DraftResponse(
                draft.id,
                draft.title,
                draft.abstractText,
                draft.level,
                draft.durationMinutes,
                draft.owner.id,
                draft.owner.name,
                draft.createdAt,
                draft.updatedAt);
    }
}
