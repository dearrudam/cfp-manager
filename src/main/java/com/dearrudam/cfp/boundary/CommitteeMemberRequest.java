package com.dearrudam.cfp.boundary;

import jakarta.validation.constraints.NotNull;

public record CommitteeMemberRequest(
        @NotNull Long userId) {
}
