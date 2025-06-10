package org.tkit.onecx.iam.bff.rs.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.iam.bff.rs.mappers.ExceptionMapper;
import org.tkit.onecx.iam.bff.rs.mappers.UserMapper;

import gen.org.tkit.onecx.iam.bff.rs.internal.UserInternalApiService;
import gen.org.tkit.onecx.iam.bff.rs.internal.model.ProblemDetailResponseDTO;
import gen.org.tkit.onecx.iam.bff.rs.internal.model.UserResetPasswordRequestDTO;
import gen.org.tkit.onecx.iam.client.api.UserInternalApi;
import gen.org.tkit.onecx.iam.client.model.ProvidersResponse;

@ApplicationScoped
public class UserRestController implements UserInternalApiService {

    @Inject
    ExceptionMapper exceptionMapper;

    @Inject
    UserMapper userMapper;

    @Inject
    @RestClient
    UserInternalApi client;

    @Override
    public Response getUserProvider() {
        try (Response response = client.getUserProvider()) {
            return Response.status(response.getStatus())
                    .entity(userMapper.mapProvider(response.readEntity(ProvidersResponse.class)))
                    .build();
        }
    }

    @Override
    public Response resetPassword(UserResetPasswordRequestDTO userResetPasswordRequestDTO) {
        try (Response response = client.resetPassword(userMapper.map(userResetPasswordRequestDTO))) {
            return Response.status(response.getStatus()).build();
        }
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> constraint(ConstraintViolationException ex) {
        return exceptionMapper.constraint(ex);
    }

    @ServerExceptionMapper
    public Response restException(ClientWebApplicationException ex) {
        return exceptionMapper.clientException(ex);
    }
}
