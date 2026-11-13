package com.dearrudam.user.boundary;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.jwt.JsonWebToken;

import com.dearrudam.user.control.UserDirectory;
import com.dearrudam.user.control.UserProfile;

import io.quarkus.security.Authenticated;

@Path("/api/users")
public class UserResource {

    @Inject
    JsonWebToken jwt;

    @Inject
    UserProfile profile;

    @Inject
    UserDirectory directory;

    @GET
    @Path("/me")
    @Authenticated
    @Produces(MediaType.APPLICATION_JSON)
    public UserResponse me() {
        return UserResponse.from(profile.get(jwt.getName()));
    }

    @PUT
    @Path("/me")
    @Authenticated
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public UserResponse update(@Valid UpdateProfileRequest request) {
        return UserResponse.from(profile.update(jwt.getName(), request.name(), request.bio()));
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public UserProfileResponse profile(@PathParam("id") Long id) {
        return UserProfileResponse.from(directory.get(id));
    }
}
