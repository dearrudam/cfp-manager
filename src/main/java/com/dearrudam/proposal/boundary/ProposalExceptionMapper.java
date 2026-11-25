package com.dearrudam.proposal.boundary;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import com.dearrudam.proposal.control.ProposalException;
import com.dearrudam.shared.ErrorResponse;

@Provider
public class ProposalExceptionMapper implements ExceptionMapper<ProposalException> {

    @Override
    public Response toResponse(ProposalException exception) {
        return Response.status(exception.status())
                .entity(ErrorResponse.of(exception.getMessage()))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
