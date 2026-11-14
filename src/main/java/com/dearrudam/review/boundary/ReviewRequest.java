package com.dearrudam.review.boundary;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReviewRequest(
        @NotNull @Min(1) @Max(6) Integer score,
        @NotBlank String comment) {
}
