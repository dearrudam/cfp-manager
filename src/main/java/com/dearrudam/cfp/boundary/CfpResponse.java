package com.dearrudam.cfp.boundary;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;

import com.dearrudam.cfp.entity.Cfp;
import com.dearrudam.cfp.entity.CfpStatus;

public record CfpResponse(
        Long id,
        Long eventId,
        CfpStatus status,
        @JsonInclude(JsonInclude.Include.NON_NULL) Instant openedAt,
        @JsonInclude(JsonInclude.Include.NON_NULL) Instant closedAt,
        Instant createdAt) {

    public static CfpResponse from(Cfp cfp) {
        return new CfpResponse(
                cfp.id,
                cfp.event.id,
                cfp.status,
                cfp.openedAt,
                cfp.closedAt,
                cfp.createdAt);
    }
}
