package com.dearrudam.user.boundary;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import com.dearrudam.shared.ErrorResponse;
import com.dearrudam.user.control.InvalidCredentialsException;

@Provider
public class InvalidCredentialsMapper implements ExceptionMapper<InvalidCredentialsException> {

    @Override
    public Response toResponse(InvalidCredentialsException exception) {
        return Response.status(Response.Status.UNAUTHORIZED)
                .entity(ErrorResponse.of("Invalid credentials", "password"))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
