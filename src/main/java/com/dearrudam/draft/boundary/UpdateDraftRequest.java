package com.dearrudam.draft.boundary;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.dearrudam.proposal.entity.TalkLevel;
import com.dearrudam.shared.MinIfPresent;
import com.dearrudam.shared.NotBlankIfPresent;

public record UpdateDraftRequest(
        @NotBlankIfPresent Optional<String> title,
        @NotBlankIfPresent @JsonProperty("abstract") Optional<String> abstractText,
        Optional<TalkLevel> level,
        @MinIfPresent(1) Optional<Integer> durationMinutes) {

    public UpdateDraftRequest {
        title = title == null ? Optional.empty() : title;
        abstractText = abstractText == null ? Optional.empty() : abstractText;
        level = level == null ? Optional.empty() : level;
        durationMinutes = durationMinutes == null ? Optional.empty() : durationMinutes;
    }
}
