package com.dearrudam.draft.boundary;

import jakarta.validation.constraints.NotBlank;

public record DraftRequest(
        @NotBlank String title,
        @NotBlank String description) {
}
