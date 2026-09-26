package it.nexus.mapper;

import org.mapstruct.Mapper;

import it.nexus.config.security.AuthenticatedUser;
import it.nexus.domain.dto.CurrentUserDTO;

@Mapper
public interface CurrentUserMapper {

    CurrentUserDTO toDto(AuthenticatedUser user);
}
