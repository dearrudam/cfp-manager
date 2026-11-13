package com.dearrudam.proposal.boundary;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.jwt.JsonWebToken;

import com.dearrudam.proposal.control.ProposalManagement;

import io.quarkus.security.Authenticated;

import jakarta.annotation.security.RolesAllowed;

@Path("/api/proposals")
public class ProposalResource {

    @Inject
    ProposalManagement management;

    @Inject
    JsonWebToken jwt;

    @POST
    @RolesAllowed("SPEAKER")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(@Valid ProposalRequest request) {
        var result = management.create(request.eventId(), request.title(), request.abstractText(),
                request.level(), request.durationMinutes(), request.saveAsDraft(), jwt.getName());
        return Response.status(Response.Status.CREATED)
                .entity(ProposalResponse.from(result.proposal(),
                        management.coSpeakers(result.proposal().id), result.draft()))
                .build();
    }

    @GET
    @Path("/mine")
    @RolesAllowed("SPEAKER")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ProposalResponse> mine() {
        return management.mine(jwt.getName()).stream()
                .map(proposal -> ProposalResponse.from(proposal,
                        management.coSpeakers(proposal.id), null))
                .toList();
    }

    @GET
    @Path("/{id}")
    @Authenticated
    @Produces(MediaType.APPLICATION_JSON)
    public ProposalResponse get(@PathParam("id") Long id) {
        var proposal = management.get(id);
        return ProposalResponse.from(proposal, management.coSpeakers(id), null);
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed("SPEAKER")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ProposalResponse update(@PathParam("id") Long id, @Valid UpdateProposalRequest request) {
        var proposal = management.update(id, request.title(), request.abstractText(),
                request.level(), request.durationMinutes(), jwt.getName());
        return ProposalResponse.from(proposal, management.coSpeakers(id), null);
    }

    @POST
    @Path("/{id}/speakers")
    @RolesAllowed("SPEAKER")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addCoSpeaker(@PathParam("id") Long id, @Valid ProposalSpeakerRequest request) {
        var speaker = management.addCoSpeaker(id, request.userId(), jwt.getName());
        return Response.status(Response.Status.CREATED)
                .entity(ProposalSpeakerResponse.from(speaker))
                .build();
    }
}
