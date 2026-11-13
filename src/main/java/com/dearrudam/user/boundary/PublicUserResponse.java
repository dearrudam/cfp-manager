package com.dearrudam.user.boundary;

import com.dearrudam.user.entity.User;

public record PublicUserResponse(
        Long id,
        String name) {

    public static PublicUserResponse from(User user) {
        return new PublicUserResponse(user.id, user.name);
    }
}
