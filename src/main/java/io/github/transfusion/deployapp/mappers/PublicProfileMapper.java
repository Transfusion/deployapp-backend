package io.github.transfusion.deployapp.mappers;

import io.github.transfusion.deployapp.db.entities.User;
import io.github.transfusion.deployapp.dto.response.PublicUserProfileDTO;
import org.mapstruct.Mapper;

@Mapper(
        componentModel = "spring"
)
public interface PublicProfileMapper {

    PublicUserProfileDTO toDTO(User user);
}
