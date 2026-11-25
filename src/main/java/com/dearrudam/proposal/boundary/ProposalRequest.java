package com.dearrudam.proposal.boundary;

import jakarta.validation.constraints.NotBlank;

public record ProposalRequest(
        @NotBlank String title,
        @NotBlank String description) {
}
