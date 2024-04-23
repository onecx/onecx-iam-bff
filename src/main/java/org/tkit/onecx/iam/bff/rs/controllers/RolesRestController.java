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
import org.tkit.onecx.iam.bff.rs.mappers.RolesMapper;

import gen.org.tkit.onecx.iam.bff.rs.internal.RolesInternalApiService;
import gen.org.tkit.onecx.iam.bff.rs.internal.model.ProblemDetailResponseDTO;
import gen.org.tkit.onecx.iam.bff.rs.internal.model.RolePageResultDTO;
import gen.org.tkit.onecx.iam.bff.rs.internal.model.RoleSearchCriteriaDTO;
import gen.org.tkit.onecx.iam.kc.client.api.RolesInternalApi;
import gen.org.tkit.onecx.iam.kc.client.model.RolePageResult;

@ApplicationScoped
public class RolesRestController implements RolesInternalApiService {

    @Inject
    @RestClient
    RolesInternalApi rolesClient;

    @Inject
    RolesMapper rolesMapper;
    @Inject
    ExceptionMapper exceptionMapper;

    @Override
    public Response searchRolesByCriteria(RoleSearchCriteriaDTO roleSearchCriteriaDTO) {

        try (Response response = rolesClient.searchRolesByCriteria(rolesMapper.map(roleSearchCriteriaDTO))) {
            RolePageResultDTO rolePageResult = rolesMapper.map(response.readEntity(RolePageResult.class));
            return Response.status(response.getStatus()).entity(rolePageResult).build();
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
