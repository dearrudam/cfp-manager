package com.dearrudam.cfp.boundary;

import java.time.Instant;

import com.dearrudam.cfp.entity.CfpCommitteeMember;

public record CommitteeMemberResponse(
        Long id,
        Long userId,
        String name,
        Instant createdAt) {

    public static CommitteeMemberResponse from(CfpCommitteeMember member) {
        return new CommitteeMemberResponse(
                member.id,
                member.user.id,
                member.user.name,
                member.createdAt);
    }
}
