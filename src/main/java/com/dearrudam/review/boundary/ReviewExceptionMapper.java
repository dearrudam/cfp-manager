package com.dearrudam.review.boundary;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import com.dearrudam.review.control.ReviewException;
import com.dearrudam.shared.ErrorResponse;

@Provider
public class ReviewExceptionMapper implements ExceptionMapper<ReviewException> {

    @Override
    public Response toResponse(ReviewException exception) {
        return Response.status(exception.status())
                .entity(ErrorResponse.of(exception.getMessage()))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
