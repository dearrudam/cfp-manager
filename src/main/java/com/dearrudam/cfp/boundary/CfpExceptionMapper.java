package com.dearrudam.cfp.boundary;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import com.dearrudam.cfp.control.CfpException;
import com.dearrudam.shared.ErrorResponse;

@Provider
public class CfpExceptionMapper implements ExceptionMapper<CfpException> {

    @Override
    public Response toResponse(CfpException exception) {
        return Response.status(exception.status())
                .entity(ErrorResponse.of(exception.getMessage()))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
