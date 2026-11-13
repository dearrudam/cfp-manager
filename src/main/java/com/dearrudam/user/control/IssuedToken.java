package com.dearrudam.user.control;

/// A signed JWT and its time-to-live in seconds.
public record IssuedToken(String token, long expiresInSeconds) {
}
