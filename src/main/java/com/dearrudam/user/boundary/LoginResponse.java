package com.dearrudam.user.boundary;

public record LoginResponse(
        String token,
        String tokenType,
        long expiresIn) {
}
