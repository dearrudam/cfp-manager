package com.dearrudam.user.boundary;

import java.util.Optional;

import com.dearrudam.shared.NotBlankIfPresent;

public record UpdateProfileRequest(
        @NotBlankIfPresent Optional<String> name,
        Optional<String> bio) {

    public UpdateProfileRequest {
        name = name == null ? Optional.empty() : name;
        bio = bio == null ? Optional.empty() : bio;
    }
}
