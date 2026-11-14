package com.dearrudam.review.boundary;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.jwt.JsonWebToken;

import com.dearrudam.review.ReviewRequirement;
import com.dearrudam.review.control.ReviewManagement;

import io.quarkus.security.Authenticated;

import static com.dearrudam.review.ReviewRequirement.Rn.R3_1;
import static com.dearrudam.review.ReviewRequirement.Rn.R3_2;

@Path("/api/events/{eventId}/cfp/review-queue")
public class ReviewQueueResource {

    @Inject
    ReviewManagement management;

    @Inject
    JsonWebToken jwt;

    @GET
    @Authenticated
    @ReviewRequirement({ R3_1, R3_2 })
    @Produces(MediaType.APPLICATION_JSON)
    public List<QueuedProposalResponse> queue(@PathParam("eventId") Long eventId) {
        return management.queue(eventId, jwt.getName()).stream()
                .map(QueuedProposalResponse::from)
                .toList();
    }
}
