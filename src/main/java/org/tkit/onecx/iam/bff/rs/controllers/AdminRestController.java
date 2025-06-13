package org.tkit.onecx.iam.bff.rs.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.iam.bff.rs.mappers.AdminMapper;
import org.tkit.onecx.iam.bff.rs.mappers.ExceptionMapper;

import gen.org.tkit.onecx.iam.bff.rs.internal.AdminInternalApiService;
import gen.org.tkit.onecx.iam.bff.rs.internal.model.*;
import gen.org.tkit.onecx.iam.client.api.AdminInternalApi;
import gen.org.tkit.onecx.iam.client.model.ProvidersResponse;
import gen.org.tkit.onecx.iam.client.model.RolePageResult;
import gen.org.tkit.onecx.iam.client.model.UserPageResult;
import gen.org.tkit.onecx.iam.client.model.UserRolesResponse;

@ApplicationScoped
public class AdminRestController implements AdminInternalApiService {

    @Inject
    ExceptionMapper exceptionMapper;

    @Inject
    AdminMapper adminMapper;

    @Inject
    @RestClient
    AdminInternalApi client;

    @Override
    public Response assignUserRole(String userId, RoleAssignmentRequestDTO roleAssignmentRequestDTO) {
        try (Response response = client.assignUserRole(userId, adminMapper.mapRoleAssignment(roleAssignmentRequestDTO))) {
            return Response.status(response.getStatus()).build();
        }
    }

    @Override
    public Response createRole(CreateRoleRequestDTO createRoleRequestDTO) {
        try (Response response = client.createRole(adminMapper.mapCreateRole(createRoleRequestDTO))) {
            return Response.status(response.getStatus()).build();
        }
    }

    @Override
    public Response createUser(CreateUserRequestDTO createUserRequestDTO) {
        try (Response response = client.createUser(adminMapper.mapCreateUser(createUserRequestDTO))) {
            return Response.status(response.getStatus()).build();
        }
    }

    @Override
    public Response getAllProviders() {
        try (Response response = client.getAllProviders()) {
            var clientResponse = response.readEntity(ProvidersResponse.class);
            return Response.status(Response.Status.OK).entity(adminMapper.mapProviders(clientResponse)).build();
        }
    }

    @Override
    public Response getUserRoles(String userId, SearchUserRolesRequestDTO searchUserRequestDTO) {
        try (Response response = client.getUserRoles(userId, adminMapper.map(searchUserRequestDTO))) {
            return Response.status(response.getStatus())
                    .entity(adminMapper.mapUserRoles(response.readEntity(UserRolesResponse.class))).build();
        }
    }

    @Override
    public Response searchRolesByCriteria(RoleSearchCriteriaDTO roleSearchCriteriaDTO) {
        try (Response response = client.searchRolesByCriteria(adminMapper.map(roleSearchCriteriaDTO))) {
            RolePageResultDTO rolePageResult = adminMapper.map(response.readEntity(RolePageResult.class));
            return Response.status(response.getStatus()).entity(rolePageResult).build();
        }
    }

    @Override
    public Response searchUsersByCriteria(UserSearchCriteriaDTO userSearchCriteriaDTO) {
        try (Response response = client.searchUsersByCriteria(adminMapper.map(userSearchCriteriaDTO))) {
            UserPageResultDTO userPageResult = adminMapper.map(response.readEntity(UserPageResult.class));
            return Response.status(response.getStatus()).entity(userPageResult).build();
        }
    }

    @Override
    public Response updateUser(String userId, UpdateUserRequestDTO updateUserRequestDTO) {
        try (Response response = client.updateUser(userId, adminMapper.mapUpdateUser(updateUserRequestDTO))) {
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
