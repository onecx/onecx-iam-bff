package org.tkit.onecx.iam.bff.rs.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import gen.org.tkit.onecx.iam.bff.rs.internal.model.*;
import gen.org.tkit.onecx.iam.client.model.*;

@Mapper
public interface AdminMapper {

    @Mapping(target = "removeDomainsItem", ignore = true)
    ProviderDTO map(Provider provider);

    @Mapping(target = "removeRolesItem", ignore = true)
    UserRolesResponseDTO mapUserRoles(UserRolesResponse userRolesResponse);

    RoleSearchCriteria map(RoleSearchCriteriaDTO criteria);

    @Mapping(target = "removeStreamItem", ignore = true)
    RolePageResultDTO map(RolePageResult pageResult);

    @Mapping(target = "removeStreamItem", ignore = true)
    UserPageResultDTO map(UserPageResult pageResult);

    @Mapping(target = "removeAttributesItem", ignore = true)
    UserDTO map(User user);

    UserSearchCriteria map(UserSearchCriteriaDTO criteria);

    UserRolesSearchRequest map(SearchUserRolesRequestDTO searchUserRequestDTO);

    @Mapping(target = "removeProvidersItem", ignore = true)
    ProvidersResponseDTO mapProviders(ProvidersResponse clientResponse);
}
