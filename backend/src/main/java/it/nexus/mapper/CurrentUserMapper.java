package it.nexus.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import it.nexus.config.security.AuthenticatedUser;
import it.nexus.domain.dto.CurrentUserDTO;

@Mapper
public interface CurrentUserMapper {

    @Mapping(target = "coordinator", ignore = true)
    CurrentUserDTO toDto(AuthenticatedUser user);
}
