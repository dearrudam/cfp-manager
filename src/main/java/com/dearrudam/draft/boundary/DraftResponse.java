package com.dearrudam.draft.boundary;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;

import com.dearrudam.draft.entity.Draft;

public record DraftResponse(
        Long id,
        String title,
        String description,
        Long ownerId,
        String ownerName,
        Instant createdAt,
        @JsonInclude(JsonInclude.Include.NON_NULL) Instant updatedAt) {

    public static DraftResponse from(Draft draft) {
        return new DraftResponse(
                draft.id,
                draft.title,
                draft.description,
                draft.owner.id,
                draft.owner.name,
                draft.createdAt,
                draft.updatedAt);
    }
}
