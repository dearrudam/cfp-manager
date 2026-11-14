package com.dearrudam.review.boundary;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;

import com.dearrudam.review.entity.Review;

public record ReviewResponse(
        Long id,
        Long proposalId,
        Long reviewerId,
        String reviewerName,
        Integer score,
        String comment,
        Instant createdAt,
        @JsonInclude(JsonInclude.Include.NON_NULL) Instant updatedAt) {

    public static ReviewResponse from(Review review) {
        return new ReviewResponse(
                review.id,
                review.proposal.id,
                review.reviewer.id,
                review.reviewer.name,
                review.score,
                review.comment,
                review.createdAt,
                review.updatedAt);
    }
}
