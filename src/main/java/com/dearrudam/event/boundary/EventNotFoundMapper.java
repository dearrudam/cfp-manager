package com.dearrudam.event.boundary;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import com.dearrudam.event.control.EventNotFoundException;
import com.dearrudam.shared.ErrorResponse;

@Provider
public class EventNotFoundMapper implements ExceptionMapper<EventNotFoundException> {

    @Override
    public Response toResponse(EventNotFoundException exception) {
        return Response.status(Response.Status.NOT_FOUND)
                .entity(ErrorResponse.of("Event not found"))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
