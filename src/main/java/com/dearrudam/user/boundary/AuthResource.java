package com.dearrudam.user.boundary;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import com.dearrudam.user.control.Authentication;

@Path("/api/auth")
public class AuthResource {

    @Inject
    Authentication authentication;

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(@Valid LoginRequest request) {
        var issued = authentication.authenticate(request.email(), request.password());
        return Response.ok(new LoginResponse(issued.token(), "Bearer", issued.expiresInSeconds()))
                .build();
    }
}
