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
import gen.org.tkit.onecx.iam.kc.client.api.AdminInternalApi;
import gen.org.tkit.onecx.iam.kc.client.model.ProvidersResponse;
import gen.org.tkit.onecx.iam.kc.client.model.RolePageResult;
import gen.org.tkit.onecx.iam.kc.client.model.UserPageResult;
import gen.org.tkit.onecx.iam.kc.client.model.UserRolesResponse;

@ApplicationScoped
public class AdminRestController implements AdminInternalApiService {

    @Inject
    @RestClient
    AdminInternalApi adminClient;

    @Inject
    ExceptionMapper exceptionMapper;

    @Inject
    AdminMapper adminMapper;

    @Override
    public Response getAllProviders() {
        try (Response response = adminClient.getAllProviders()) {
            return Response.status(response.getStatus()).entity(adminMapper.map(response.readEntity(ProvidersResponse.class)))
                    .build();
        }
    }

    @Override
    public Response getUserRoles(String provider, String realm, String userId) {
        try (Response response = adminClient.getUserRoles(provider, realm, userId)) {
            return Response.status(response.getStatus())
                    .entity(adminMapper.mapUserRoles(response.readEntity(UserRolesResponse.class))).build();
        }
    }

    @Override
    public Response searchRolesByCriteria(String provider, String realm, RoleSearchCriteriaDTO roleSearchCriteriaDTO) {
        try (Response response = adminClient.searchRolesByCriteria(provider, realm, adminMapper.map(roleSearchCriteriaDTO))) {
            RolePageResultDTO rolePageResult = adminMapper.map(response.readEntity(RolePageResult.class));
            return Response.status(response.getStatus()).entity(rolePageResult).build();
        }
    }

    @Override
    public Response searchUsersByCriteria(String provider, String realm, UserSearchCriteriaDTO userSearchCriteriaDTO) {
        try (Response response = adminClient.searchUsersByCriteria(provider, realm, adminMapper.map(userSearchCriteriaDTO))) {
            UserPageResultDTO userPageResult = adminMapper.map(response.readEntity(UserPageResult.class));
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
