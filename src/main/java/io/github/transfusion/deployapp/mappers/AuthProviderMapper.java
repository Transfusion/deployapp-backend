package io.github.transfusion.deployapp.mappers;

import io.github.transfusion.deployapp.db.entities.AuthProvider;
import io.github.transfusion.deployapp.dto.response.AuthProviderDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        componentModel = "spring"
)
public interface AuthProviderMapper {

    @Mapping(expression = "java(a.getUser().getId())", target = "userId")
    @Mapping(expression = "java(a.getId().getProviderName())", target = "providerName")
    AuthProviderDTO toDTO(AuthProvider a);
}
