package com.dearrudam.user.boundary;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;

import com.dearrudam.user.entity.Role;
import com.dearrudam.user.entity.User;

public record UserProfileResponse(
        Long id,
        String name,
        Set<Role> roles,
        @JsonInclude(JsonInclude.Include.NON_NULL) String bio) {

    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(user.id, user.name, user.roles, user.bio);
    }
}
