package com.dearrudam.user.boundary;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import com.dearrudam.user.control.UserRegistration;

@Path("/api/coordinators")
public class CoordinatorResource {

    @Inject
    UserRegistration registration;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response register(@Valid CoordinatorRequest request) {
        var result = registration.registerCoordinator(request.name(), request.email(), request.password());
        var status = result.created() ? Response.Status.CREATED : Response.Status.OK;
        return Response.status(status)
                .entity(UserResponse.from(result.user()))
                .build();
    }
}
