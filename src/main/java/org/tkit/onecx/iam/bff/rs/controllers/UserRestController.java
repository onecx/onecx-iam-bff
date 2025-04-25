package org.tkit.onecx.iam.bff.rs.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;

import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.iam.bff.rs.mappers.ExceptionMapper;
import org.tkit.onecx.iam.bff.rs.mappers.UserMapper;
import org.tkit.onecx.iam.bff.rs.services.IAMClientFactory;
import org.tkit.onecx.iam.bff.rs.services.IssuerResolverService;
import org.tkit.quarkus.context.ApplicationContext;

import gen.org.tkit.onecx.iam.bff.rs.internal.UserInternalApiService;
import gen.org.tkit.onecx.iam.bff.rs.internal.model.ProblemDetailResponseDTO;
import gen.org.tkit.onecx.iam.bff.rs.internal.model.UserResetPasswordRequestDTO;
import gen.org.tkit.onecx.iam.kc.client.model.ProvidersResponse;

@ApplicationScoped
public class UserRestController implements UserInternalApiService {

    @Inject
    ExceptionMapper exceptionMapper;

    @Inject
    UserMapper userMapper;

    @Inject
    HttpHeaders headers;

    @Inject
    IssuerResolverService issuerResolverService;

    @Inject
    IAMClientFactory clientFactory;

    @Override
    public Response getUserProvider() {
        var context = ApplicationContext.get();
        context.getPrincipalToken().getIssuer();
        var issuer = issuerResolverService.getIssuerFromToken();
        var client = clientFactory.getUserClients().get(issuerResolverService.validateAndCacheIssuer(issuer));
        try (Response response = client.getUserProvider()) {
            return Response.status(response.getStatus())
                    .entity(userMapper.map(issuerResolverService.validateAndCacheIssuer(issuer),
                            response.readEntity(ProvidersResponse.class)))
                    .build();
        }
    }

    @Override
    public Response resetPassword(UserResetPasswordRequestDTO userResetPasswordRequestDTO) {
        var issuer = issuerResolverService.getIssuerFromToken();
        var client = clientFactory.getUserClients().get(issuerResolverService.validateAndCacheIssuer(issuer));
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
