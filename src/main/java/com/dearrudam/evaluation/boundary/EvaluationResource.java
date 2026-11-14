package com.dearrudam.evaluation.boundary;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.jwt.JsonWebToken;

import com.dearrudam.evaluation.control.EvaluationManagement;

import io.quarkus.security.Authenticated;

@Path("/api/proposals/{proposalId}/evaluations")
public class EvaluationResource {

    @Inject
    EvaluationManagement management;

    @Inject
    JsonWebToken jwt;

    @POST
    @Authenticated
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response submit(@PathParam("proposalId") Long proposalId,
            @Valid EvaluationRequest request) {
        var evaluation = management.submit(proposalId, request.score(), jwt.getName());
        return Response.status(Response.Status.CREATED)
                .entity(EvaluationResponse.from(evaluation))
                .build();
    }

    @GET
    @Authenticated
    @Produces(MediaType.APPLICATION_JSON)
    public List<EvaluationResponse> list(@PathParam("proposalId") Long proposalId) {
        return management.list(proposalId, jwt.getName()).stream()
                .map(EvaluationResponse::from)
                .toList();
    }
}
