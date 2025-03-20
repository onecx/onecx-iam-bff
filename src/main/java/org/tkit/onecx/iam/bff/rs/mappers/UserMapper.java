package org.tkit.onecx.iam.bff.rs.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import gen.org.tkit.onecx.iam.bff.rs.internal.model.ProviderDTO;
import gen.org.tkit.onecx.iam.bff.rs.internal.model.ProvidersResponseDTO;
import gen.org.tkit.onecx.iam.bff.rs.internal.model.UserResetPasswordRequestDTO;
import gen.org.tkit.onecx.iam.kc.client.model.Provider;
import gen.org.tkit.onecx.iam.kc.client.model.ProvidersResponse;
import gen.org.tkit.onecx.iam.kc.client.model.UserResetPasswordRequest;

@Mapper
public interface UserMapper {

    UserResetPasswordRequest map(UserResetPasswordRequestDTO resetPasswordRequestDTO);

    @Mapping(target = "removeDomainsItem", ignore = true)
    ProviderDTO map(Provider provider);

    @Mapping(target = "removeProvidersItem", ignore = true)
    ProvidersResponseDTO map(ProvidersResponse providersResponse);

}
