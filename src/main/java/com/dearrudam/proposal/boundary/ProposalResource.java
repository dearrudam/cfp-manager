package com.dearrudam.proposal.boundary;

import java.util.List;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.jwt.JsonWebToken;

import com.dearrudam.proposal.control.ProposalSubmission;

@Path("/api/proposals")
@RolesAllowed("SPEAKER")
public class ProposalResource {

    @Inject
    ProposalSubmission submission;

    @Inject
    JsonWebToken jwt;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<ProposalResponse> list() {
        return submission.list(jwt.getName()).stream()
                .map(ProposalResponse::from)
                .toList();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public ProposalResponse get(@PathParam("id") Long id) {
        return ProposalResponse.from(submission.get(id, jwt.getName()));
    }
}
