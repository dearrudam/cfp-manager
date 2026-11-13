package com.dearrudam.event.boundary;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EventRequest(
        @NotBlank String name,
        @NotBlank String description,
        @NotNull Instant startAt,
        @NotNull Instant endAt) {
}
