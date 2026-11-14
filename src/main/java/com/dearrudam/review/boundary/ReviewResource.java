package com.dearrudam.review.boundary;

import java.util.List;

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

import com.dearrudam.review.ReviewRequirement;
import com.dearrudam.review.control.ReviewManagement;

import io.quarkus.security.Authenticated;

import static com.dearrudam.review.ReviewRequirement.Rn.R1_1;
import static com.dearrudam.review.ReviewRequirement.Rn.R1_2;
import static com.dearrudam.review.ReviewRequirement.Rn.R1_3;
import static com.dearrudam.review.ReviewRequirement.Rn.R1_4;
import static com.dearrudam.review.ReviewRequirement.Rn.R1_5;
import static com.dearrudam.review.ReviewRequirement.Rn.R1_6;
import static com.dearrudam.review.ReviewRequirement.Rn.R1_7;
import static com.dearrudam.review.ReviewRequirement.Rn.R2_1;
import static com.dearrudam.review.ReviewRequirement.Rn.R2_2;
import static com.dearrudam.review.ReviewRequirement.Rn.R2_3;

@Path("/api/proposals/{proposalId}/reviews")
public class ReviewResource {

    @Inject
    ReviewManagement management;

    @Inject
    JsonWebToken jwt;

    @PUT
    @Authenticated
    @ReviewRequirement({ R1_1, R1_2, R1_3, R1_4, R1_5, R1_6, R1_7 })
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ReviewResponse submit(@PathParam("proposalId") Long proposalId,
            @Valid ReviewRequest request) {
        return ReviewResponse.from(management.submit(proposalId, request.score(),
                request.comment(), jwt.getName()));
    }

    @GET
    @Authenticated
    @ReviewRequirement({ R2_1, R2_2, R2_3 })
    @Produces(MediaType.APPLICATION_JSON)
    public List<ReviewResponse> list(@PathParam("proposalId") Long proposalId) {
        return management.list(proposalId, jwt.getName()).stream()
                .map(ReviewResponse::from)
                .toList();
    }
}
