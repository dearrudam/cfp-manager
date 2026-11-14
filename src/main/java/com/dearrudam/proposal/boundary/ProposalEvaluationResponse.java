package com.dearrudam.proposal.boundary;

import java.time.Instant;

import com.dearrudam.proposal.entity.ProposalEvaluation;

public record ProposalEvaluationResponse(
        Long id,
        Long proposalId,
        Long evaluatorId,
        String evaluatorName,
        Integer score,
        Instant createdAt) {

    public static ProposalEvaluationResponse from(ProposalEvaluation evaluation) {
        return new ProposalEvaluationResponse(
                evaluation.id,
                evaluation.proposal.id,
                evaluation.evaluator.id,
                evaluation.evaluator.name,
                evaluation.score,
                evaluation.createdAt);
    }
}
