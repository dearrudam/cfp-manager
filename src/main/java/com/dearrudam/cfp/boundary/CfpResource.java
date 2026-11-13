package com.dearrudam.cfp.boundary;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.jwt.JsonWebToken;

import com.dearrudam.cfp.control.CfpManagement;

import io.quarkus.security.Authenticated;

@Path("/api/events/{eventId}/cfp")
public class CfpResource {

    @Inject
    CfpManagement management;

    @Inject
    JsonWebToken jwt;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public CfpResponse get(@PathParam("eventId") Long eventId) {
        return CfpResponse.from(management.get(eventId));
    }

    @POST
    @Authenticated
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(@PathParam("eventId") Long eventId) {
        var cfp = management.create(eventId, jwt.getName());
        return Response.status(Response.Status.CREATED)
                .entity(CfpResponse.from(cfp))
                .build();
    }

    @POST
    @Path("/open")
    @Authenticated
    @Produces(MediaType.APPLICATION_JSON)
    public CfpResponse open(@PathParam("eventId") Long eventId) {
        return CfpResponse.from(management.open(eventId, jwt.getName()));
    }

    @POST
    @Path("/close")
    @Authenticated
    @Produces(MediaType.APPLICATION_JSON)
    public CfpResponse close(@PathParam("eventId") Long eventId) {
        return CfpResponse.from(management.close(eventId, jwt.getName()));
    }

    @GET
    @Path("/committee")
    @Produces(MediaType.APPLICATION_JSON)
    public List<CommitteeMemberResponse> committee(@PathParam("eventId") Long eventId) {
        return management.committee(eventId).stream()
                .map(CommitteeMemberResponse::from)
                .toList();
    }

    @POST
    @Path("/committee")
    @Authenticated
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addCommitteeMember(@PathParam("eventId") Long eventId,
            @Valid CommitteeMemberRequest request) {
        var member = management.addCommitteeMember(eventId, request.userId(), jwt.getName());
        return Response.status(Response.Status.CREATED)
                .entity(CommitteeMemberResponse.from(member))
                .build();
    }

    @DELETE
    @Path("/committee/{userId}")
    @Authenticated
    public Response removeCommitteeMember(@PathParam("eventId") Long eventId,
            @PathParam("userId") Long userId) {
        management.removeCommitteeMember(eventId, userId, jwt.getName());
        return Response.noContent().build();
    }
}
