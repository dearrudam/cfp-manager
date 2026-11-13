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

@Path("/api/speakers")
public class SpeakerResource {

    @Inject
    UserRegistration registration;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response register(@Valid SpeakerRequest request) {
        var result = registration.registerSpeaker(request.name(), request.email(), request.password(), request.bio());
        var status = result.created() ? Response.Status.CREATED : Response.Status.OK;
        return Response.status(status)
                .entity(UserResponse.from(result.user()))
                .build();
    }
}
