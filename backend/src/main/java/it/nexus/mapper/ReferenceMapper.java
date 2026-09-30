package it.nexus.mapper;

import org.mapstruct.Mapper;

import it.nexus.domain.AbstractReferenceEntity;
import it.nexus.domain.dto.ReferenceItemDTO;

@Mapper
public interface ReferenceMapper {

    default ReferenceItemDTO toDto(AbstractReferenceEntity entity) {
        return entity == null ? null : new ReferenceItemDTO(TsidMapper.toExternal(entity.getId()), entity.getCode(), entity.getName());
    }
}
