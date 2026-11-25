package com.dearrudam.proposal.boundary;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.jwt.JsonWebToken;

import com.dearrudam.proposal.control.ProposalSubmission;

@Path("/api/events/{eventId}/cfp/proposals")
@RolesAllowed("SPEAKER")
public class ProposalSubmissionResource {

    @Inject
    ProposalSubmission submission;

    @Inject
    JsonWebToken jwt;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response submit(@PathParam("eventId") Long eventId, @Valid ProposalRequest request) {
        var proposal = submission.submit(eventId, request.title(), request.description(), jwt.getName());
        return Response.status(Response.Status.CREATED)
                .entity(ProposalResponse.from(proposal))
                .build();
    }
}
