package com.dearrudam.proposal.boundary;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ProposalEvaluationRequest(
        @NotNull @Min(1) @Max(5) Integer score) {
}
