package com.dearrudam.draft.boundary;

import java.util.List;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.jwt.JsonWebToken;

import com.dearrudam.draft.control.DraftManagement;

@Path("/api/drafts")
@RolesAllowed("SPEAKER")
public class DraftResource {

    @Inject
    DraftManagement management;

    @Inject
    JsonWebToken jwt;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<DraftResponse> list() {
        return management.list(jwt.getName()).stream()
                .map(DraftResponse::from)
                .toList();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(@Valid DraftRequest request) {
        var draft = management.create(request.title(), request.description(), jwt.getName());
        return Response.status(Response.Status.CREATED)
                .entity(DraftResponse.from(draft))
                .build();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public DraftResponse get(@PathParam("id") Long id) {
        return DraftResponse.from(management.get(id, jwt.getName()));
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DraftResponse update(@PathParam("id") Long id, @Valid UpdateDraftRequest request) {
        return DraftResponse.from(
                management.update(id, request.title(), request.description(), jwt.getName()));
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        management.delete(id, jwt.getName());
        return Response.noContent().build();
    }
}
