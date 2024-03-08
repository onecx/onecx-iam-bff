package org.tkit.onecx.iam.bff.rs.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import gen.org.tkit.onecx.iam.bff.rs.internal.model.UserPageResultDTO;
import gen.org.tkit.onecx.iam.bff.rs.internal.model.UserSearchCriteriaDTO;
import gen.org.tkit.onecx.iam.kc.client.model.UserPageResult;
import gen.org.tkit.onecx.iam.kc.client.model.UserSearchCriteria;

@Mapper
public interface UsersMapper {

    UserSearchCriteria map(UserSearchCriteriaDTO criteria);

    @Mapping(target = "removeStreamItem", ignore = true)
    UserPageResultDTO map(UserPageResult pageResult);
}
