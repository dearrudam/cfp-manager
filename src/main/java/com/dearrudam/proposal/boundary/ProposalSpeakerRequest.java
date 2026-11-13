package com.dearrudam.proposal.boundary;

import jakarta.validation.constraints.NotNull;

public record ProposalSpeakerRequest(
        @NotNull Long userId) {
}
