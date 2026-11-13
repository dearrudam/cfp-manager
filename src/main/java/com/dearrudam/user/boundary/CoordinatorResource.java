package com.dearrudam.user.boundary;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import io.quarkus.security.identity.SecurityIdentity;

import com.dearrudam.user.control.UserDirectory;
import com.dearrudam.user.control.UserRegistration;
import com.dearrudam.user.entity.Role;

@Path("/api/coordinators")
public class CoordinatorResource {

    @Inject
    UserRegistration registration;

    @Inject
    UserDirectory directory;

    @Inject
    SecurityIdentity identity;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<PublicUserResponse> list() {
        return directory.list(Role.COORDINATOR, callerEmail()).stream()
                .map(PublicUserResponse::from)
                .toList();
    }

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

    String callerEmail() {
        return identity.isAnonymous() ? null : identity.getPrincipal().getName();
    }
}
