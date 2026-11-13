package com.dearrudam.user.boundary;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import com.dearrudam.shared.ErrorResponse;
import com.dearrudam.user.control.CoordinatorRegistration;
import com.dearrudam.user.control.EmailAlreadyRegisteredException;

@Path("/api/coordinators")
public class CoordinatorResource {

    @Inject
    CoordinatorRegistration registration;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response register(@Valid CoordinatorRequest request) {
        var coordinator = registration.register(request.name(), request.email(), request.password());
        return Response.status(Response.Status.CREATED)
                .entity(CoordinatorResponse.from(coordinator))
                .build();
    }

    @ServerExceptionMapper(EmailAlreadyRegisteredException.class)
    public Response emailAlreadyRegistered(EmailAlreadyRegisteredException exception) {
        return Response.status(Response.Status.CONFLICT)
                .entity(ErrorResponse.of("Email already registered", "email"))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
