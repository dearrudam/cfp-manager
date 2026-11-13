package com.dearrudam.draft.boundary;

import java.util.Optional;

import com.dearrudam.shared.NotBlankIfPresent;

public record UpdateDraftRequest(
        @NotBlankIfPresent Optional<String> title,
        @NotBlankIfPresent Optional<String> description) {

    public UpdateDraftRequest {
        title = title == null ? Optional.empty() : title;
        description = description == null ? Optional.empty() : description;
    }
}
