package com.dearrudam.draft.boundary;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import com.dearrudam.draft.control.DraftException;
import com.dearrudam.shared.ErrorResponse;

@Provider
public class DraftExceptionMapper implements ExceptionMapper<DraftException> {

    @Override
    public Response toResponse(DraftException exception) {
        return Response.status(exception.status())
                .entity(ErrorResponse.of(exception.getMessage()))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
