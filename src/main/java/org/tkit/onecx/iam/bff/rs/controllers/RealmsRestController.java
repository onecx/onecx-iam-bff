package org.tkit.onecx.iam.bff.rs.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.tkit.onecx.iam.bff.rs.mappers.RealmMapper;

import gen.org.tkit.onecx.iam.bff.rs.internal.RealmsInternalApiService;
import gen.org.tkit.onecx.iam.kc.client.api.RealmsInternalApi;
import gen.org.tkit.onecx.iam.kc.client.model.RealmResponse;

@ApplicationScoped
public class RealmsRestController implements RealmsInternalApiService {

    @Inject
    @RestClient
    RealmsInternalApi realmsInternalApi;

    @Inject
    RealmMapper realmMapper;

    @Override
    public Response getAllRealms() {
        try (Response response = realmsInternalApi.getAllRealms()) {
            return Response.status(response.getStatus()).entity(realmMapper.map(response.readEntity(RealmResponse.class)))
                    .build();
        }
    }
}
