package com.dearrudam.shared;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ConstraintViolationMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        var violations = List.copyOf(exception.getConstraintViolations());
        if (violations.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ErrorResponse.of("Invalid request"))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        var violation = violations.getFirst();
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(ErrorResponse.of(violation.getMessage(), fieldName(violation)))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    String fieldName(ConstraintViolation<?> violation) {
        List<Path.Node> nodes = new ArrayList<>();
        violation.getPropertyPath().forEach(nodes::add);
        return nodes.isEmpty() ? null : nodes.getLast().getName();
    }
}
