package org.tkit.onecx.iam.bff.rs.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import gen.org.tkit.onecx.iam.bff.rs.internal.model.RealmResponseDTO;
import gen.org.tkit.onecx.iam.kc.client.model.RealmResponse;

@Mapper
public interface RealmMapper {
    @Mapping(target = "removeRealmsItem", ignore = true)
    RealmResponseDTO map(RealmResponse realmResponse);
}
