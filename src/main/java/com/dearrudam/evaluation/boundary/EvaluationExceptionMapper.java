package com.dearrudam.evaluation.boundary;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import com.dearrudam.evaluation.control.EvaluationException;
import com.dearrudam.shared.ErrorResponse;

@Provider
public class EvaluationExceptionMapper implements ExceptionMapper<EvaluationException> {

    @Override
    public Response toResponse(EvaluationException exception) {
        return Response.status(exception.status())
                .entity(ErrorResponse.of(exception.getMessage()))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
