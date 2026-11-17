package com.dearrudam.evaluation.boundary;

import java.time.Instant;

import com.dearrudam.evaluation.entity.Evaluation;

public record EvaluationResponse(
        Long id,
        Long proposalId,
        Long evaluatorId,
        String evaluatorName,
        Integer score,
        Instant createdAt) {

    public static EvaluationResponse from(Evaluation evaluation) {
        return new EvaluationResponse(
                evaluation.id,
                evaluation.proposal.id,
                evaluation.evaluator.id,
                evaluation.evaluator.name,
                evaluation.score,
                evaluation.createdAt);
    }
}
