package org.tkit.onecx.iam.bff.rs.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;

import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.iam.bff.rs.mappers.AdminMapper;
import org.tkit.onecx.iam.bff.rs.mappers.ExceptionMapper;
import org.tkit.onecx.iam.bff.rs.services.IAMClientFactory;
import org.tkit.onecx.iam.bff.rs.services.IssuerResolverService;

import gen.org.tkit.onecx.iam.bff.rs.internal.AdminInternalApiService;
import gen.org.tkit.onecx.iam.bff.rs.internal.model.*;
import gen.org.tkit.onecx.iam.kc.client.model.ProvidersResponse;
import gen.org.tkit.onecx.iam.kc.client.model.RolePageResult;
import gen.org.tkit.onecx.iam.kc.client.model.UserPageResult;
import gen.org.tkit.onecx.iam.kc.client.model.UserRolesResponse;

@ApplicationScoped
public class AdminRestController implements AdminInternalApiService {

    @Inject
    ExceptionMapper exceptionMapper;

    @Inject
    AdminMapper adminMapper;

    @Inject
    IssuerResolverService issuerResolverService;

    @Inject
    IAMClientFactory clientFactory;

    @Override
    public Response getAllProviders() {
        ProvidersResponseDTO responseDTO = new ProvidersResponseDTO();
        clientFactory.getAdminClients().forEach((s, client) -> {
            ProvidersResponse clientResponse = null;
            try (Response response = client.getAllProviders()) {
                clientResponse = response.readEntity(ProvidersResponse.class);
                responseDTO.putClientsItem(s, adminMapper.maps(clientResponse));
            }
        });
        return Response.status(Response.Status.OK).entity(responseDTO).build();
    }

    @Override
    public Response getUserRoles(String userId, SearchUserRolesRequestDTO searchUserRequestDTO) {
        var issuer = searchUserRequestDTO.getIssuer();
        var client = clientFactory.getAdminClients().get(issuerResolverService.validateAndCacheIssuer(issuer));
        System.out.println(client);
        try (Response response = client.getUserRoles(userId, adminMapper.map(searchUserRequestDTO))) {
            return Response.status(response.getStatus())
                    .entity(adminMapper.mapUserRoles(response.readEntity(UserRolesResponse.class))).build();
        }
    }

    @Override
    public Response searchRolesByCriteria(RoleSearchCriteriaDTO roleSearchCriteriaDTO) {
        var issuer = roleSearchCriteriaDTO.getIssuer();
        var client = clientFactory.getAdminClients().get(issuerResolverService.validateAndCacheIssuer(issuer));
        try (Response response = client.searchRolesByCriteria(adminMapper.map(roleSearchCriteriaDTO))) {
            RolePageResultDTO rolePageResult = adminMapper.map(response.readEntity(RolePageResult.class));
            return Response.status(response.getStatus()).entity(rolePageResult).build();
        }
    }

    @Override
    public Response searchUsersByCriteria(UserSearchCriteriaDTO userSearchCriteriaDTO) {
        var issuer = userSearchCriteriaDTO.getIssuer();
        var client = clientFactory.getAdminClients().get(issuerResolverService.validateAndCacheIssuer(issuer));
        try (Response response = client.searchUsersByCriteria(adminMapper.map(userSearchCriteriaDTO))) {
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
