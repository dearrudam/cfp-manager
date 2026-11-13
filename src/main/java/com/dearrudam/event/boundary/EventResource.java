package com.dearrudam.event.boundary;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import com.dearrudam.event.control.EventCreation;
import com.dearrudam.event.control.InvalidEventException;
import com.dearrudam.shared.ErrorResponse;

import jakarta.annotation.security.RolesAllowed;

@Path("/api/events")
public class EventResource {

    @Inject
    EventCreation creation;

    @Inject
    JsonWebToken jwt;

    @POST
    @RolesAllowed("COORDINATOR")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(@Valid EventRequest request) {
        var event = creation.create(
                request.name(), request.description(), request.startAt(), request.endAt(), jwt.getName());
        return Response.status(Response.Status.CREATED)
                .entity(EventResponse.from(event))
                .build();
    }

    @ServerExceptionMapper
    Response invalidEvent(InvalidEventException exception) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(ErrorResponse.of(exception.getMessage()))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
