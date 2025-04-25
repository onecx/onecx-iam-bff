package org.tkit.onecx.iam.bff.rs.services;

import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.tkit.quarkus.context.ApplicationContext;
import org.tkit.quarkus.rs.context.token.TokenParserService;

import gen.org.tkit.onecx.iam.kc.client.api.AdminInternalApi;
import gen.org.tkit.onecx.iam.kc.client.model.ValidateIssuerRequest;
import io.quarkus.cache.CacheResult;

@ApplicationScoped
public class IssuerResolverService {

    @Inject
    IAMClientFactory clientFactory;

    @Inject
    TokenParserService tokenParserService;

    @CacheResult(cacheName = "issuer-cache")
    public String validateAndCacheIssuer(String issuer) {
        var clients = clientFactory.getAdminClients();
        ValidateIssuerRequest issuerRequest = new ValidateIssuerRequest();
        for (Map.Entry<String, AdminInternalApi> entry : clients.entrySet()) {
            String s = entry.getKey();
            AdminInternalApi adminInternalApi = entry.getValue();
            issuerRequest.setIssuer(issuer);
            try (Response response = adminInternalApi.validateIssuer(issuerRequest)) {
                return s;
            } catch (WebApplicationException exception) {
                // ignore exception
            }
        }
        throw new ClientWebApplicationException("unknown issuer");
    }

    public String getIssuerFromToken() {
        var ctx = ApplicationContext.get();
        return ctx.getPrincipalToken().getIssuer();
    }
}
