package org.tkit.onecx.iam.bff.rs.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import gen.org.tkit.onecx.iam.bff.rs.internal.model.RolePageResultDTO;
import gen.org.tkit.onecx.iam.bff.rs.internal.model.RoleSearchCriteriaDTO;
import gen.org.tkit.onecx.iam.bff.rs.internal.model.UserRolesResponseDTO;
import gen.org.tkit.onecx.iam.kc.client.model.RolePageResult;
import gen.org.tkit.onecx.iam.kc.client.model.RoleSearchCriteria;
import gen.org.tkit.onecx.iam.kc.client.model.UserRolesResponse;

@Mapper
public interface RolesMapper {

    RoleSearchCriteria map(RoleSearchCriteriaDTO criteria);

    @Mapping(target = "removeStreamItem", ignore = true)
    RolePageResultDTO map(RolePageResult pageResult);

    @Mapping(target = "removeRolesItem", ignore = true)
    UserRolesResponseDTO mapUserRoles(UserRolesResponse userRolesResponse);
}
