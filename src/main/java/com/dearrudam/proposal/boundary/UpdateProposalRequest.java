package com.dearrudam.proposal.boundary;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.dearrudam.proposal.entity.TalkLevel;

public record UpdateProposalRequest(
        @NotBlank String title,
        @NotBlank @JsonProperty("abstract") String abstractText,
        @NotNull TalkLevel level,
        @NotNull @Min(1) Integer durationMinutes) {
}
