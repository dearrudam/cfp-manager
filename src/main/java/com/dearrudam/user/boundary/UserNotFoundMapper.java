package com.dearrudam.user.boundary;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import com.dearrudam.shared.ErrorResponse;
import com.dearrudam.user.control.UserNotFoundException;

@Provider
public class UserNotFoundMapper implements ExceptionMapper<UserNotFoundException> {

    @Override
    public Response toResponse(UserNotFoundException exception) {
        return Response.status(Response.Status.NOT_FOUND)
                .entity(ErrorResponse.of("User not found"))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
