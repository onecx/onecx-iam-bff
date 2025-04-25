package org.tkit.onecx.iam.bff.rs.services;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;

import org.jboss.resteasy.reactive.client.api.QuarkusRestClientProperties;
import org.tkit.onecx.iam.bff.rs.config.IAMClientConfig;

import gen.org.tkit.onecx.iam.kc.client.api.AdminInternalApi;
import gen.org.tkit.onecx.iam.kc.client.api.UserInternalApi;
import io.quarkus.oidc.client.reactive.filter.OidcClientRequestReactiveFilter;
import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;
import io.quarkus.rest.client.reactive.ReactiveClientHeadersFactory;
import io.smallrye.mutiny.Uni;

@ApplicationScoped
public class IAMClientFactory {

    @Inject
    IAMClientConfig clientConfig;

    private final Map<String, AdminInternalApi> adminClients = new HashMap<>();

    private final Map<String, UserInternalApi> userClients = new HashMap<>();

    @PostConstruct
    public void createClients() {
        clientConfig.clients().forEach((key, configClient) -> {
            var client = QuarkusRestClientBuilder.newBuilder()
                    .baseUri(URI.create(configClient.url()))
                    .property(QuarkusRestClientProperties.CONNECTION_POOL_SIZE, configClient.connectionPoolSize())
                    .property(QuarkusRestClientProperties.NAME, key)
                    .property(QuarkusRestClientProperties.SHARED, configClient.shared())
                    .register(OidcClientRequestReactiveFilter.class)
                    .clientHeadersFactory(new ReactiveClientHeadersFactory() {
                        @Override
                        public Uni<MultivaluedMap<String, String>> getHeaders(MultivaluedMap<String, String> incomingHeaders,
                                MultivaluedMap<String, String> clientOutgoingHeaders) {
                            MultivaluedMap<String, String> propagatedHeaders = new MultivaluedHashMap<>();
                            //propagatedHeaders.putSingle(parameterConfig.token().headerParam(), token); ??
                            return Uni.createFrom().item(propagatedHeaders);
                        }
                    })
                    .build(AdminInternalApi.class);

            var userClient = QuarkusRestClientBuilder.newBuilder()
                    .baseUri(URI.create(configClient.url()))
                    .property(QuarkusRestClientProperties.CONNECTION_POOL_SIZE, configClient.connectionPoolSize())
                    .property(QuarkusRestClientProperties.NAME, key)
                    .property(QuarkusRestClientProperties.SHARED, configClient.shared())
                    .register(OidcClientRequestReactiveFilter.class)
                    .clientHeadersFactory(new ReactiveClientHeadersFactory() {
                        @Override
                        public Uni<MultivaluedMap<String, String>> getHeaders(MultivaluedMap<String, String> incomingHeaders,
                                MultivaluedMap<String, String> clientOutgoingHeaders) {
                            MultivaluedMap<String, String> propagatedHeaders = new MultivaluedHashMap<>();
                            //propagatedHeaders.putSingle(parameterConfig.token().headerParam(), token); ??
                            return Uni.createFrom().item(propagatedHeaders);
                        }
                    })
                    .build(UserInternalApi.class);

            this.userClients.put(key, userClient);
            this.adminClients.put(key, client);
        });
    }

    public Map<String, AdminInternalApi> getAdminClients() {
        return adminClients;
    }

    public Map<String, UserInternalApi> getUserClients() {
        return userClients;
    }
}
