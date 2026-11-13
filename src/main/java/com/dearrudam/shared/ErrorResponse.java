package com.dearrudam.shared;

import com.fasterxml.jackson.annotation.JsonInclude;

public record ErrorResponse(
        String error,
        @JsonInclude(JsonInclude.Include.NON_NULL) String field) {

    public static ErrorResponse of(String error) {
        return new ErrorResponse(error, null);
    }

    public static ErrorResponse of(String error, String field) {
        return new ErrorResponse(error, field);
    }
}
