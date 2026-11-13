package com.dearrudam.user.boundary;

import java.time.Instant;
import java.util.Set;

import com.dearrudam.user.entity.Role;
import com.dearrudam.user.entity.User;

public record CoordinatorResponse(
        Long id,
        String name,
        String email,
        Set<Role> roles,
        Instant createdAt) {

    public static CoordinatorResponse from(User user) {
        return new CoordinatorResponse(user.id, user.name, user.email, user.roles, user.createdAt);
    }
}
