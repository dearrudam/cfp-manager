package com.dearrudam.user.boundary;

import java.time.Instant;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;

import com.dearrudam.user.entity.Role;
import com.dearrudam.user.entity.User;

public record UserResponse(
        Long id,
        String name,
        String email,
        Set<Role> roles,
        @JsonInclude(JsonInclude.Include.NON_NULL) String bio,
        Instant createdAt) {

    public static UserResponse from(User user) {
        return new UserResponse(user.id, user.name, user.email, user.roles, user.bio, user.createdAt);
    }
}
