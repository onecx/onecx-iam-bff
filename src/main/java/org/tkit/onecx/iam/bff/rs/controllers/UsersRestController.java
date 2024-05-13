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
import org.tkit.onecx.iam.bff.rs.mappers.UsersMapper;

import gen.org.tkit.onecx.iam.bff.rs.internal.UsersInternalApiService;
import gen.org.tkit.onecx.iam.bff.rs.internal.model.ProblemDetailResponseDTO;
import gen.org.tkit.onecx.iam.bff.rs.internal.model.UserPageResultDTO;
import gen.org.tkit.onecx.iam.bff.rs.internal.model.UserResetPasswordRequestDTO;
import gen.org.tkit.onecx.iam.bff.rs.internal.model.UserSearchCriteriaDTO;
import gen.org.tkit.onecx.iam.kc.client.api.UsersInternalApi;
import gen.org.tkit.onecx.iam.kc.client.model.UserPageResult;

@ApplicationScoped
public class UsersRestController implements UsersInternalApiService {

    @Inject
    @RestClient
    UsersInternalApi usersClient;

    @Inject
    ExceptionMapper exceptionMapper;

    @Inject
    UsersMapper usersMapper;

    @Override
    public Response resetPassword(UserResetPasswordRequestDTO userResetPasswordRequestDTO) {

        try (Response response = usersClient.resetPassword(usersMapper.map(userResetPasswordRequestDTO))) {
            return Response.status(response.getStatus()).build();
        }
    }

    @Override
    public Response searchUsersByCriteria(UserSearchCriteriaDTO userSearchCriteriaDTO) {

        try (Response response = usersClient.searchUsersByCriteria(usersMapper.map(userSearchCriteriaDTO))) {
            UserPageResultDTO userPageResult = usersMapper.map(response.readEntity(UserPageResult.class));
            return Response.status(response.getStatus()).entity(userPageResult).build();
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
