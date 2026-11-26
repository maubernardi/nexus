package it.nexus.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import it.nexus.domain.JobSlot;
import it.nexus.domain.dto.JobSlotDTO;
import it.nexus.domain.dto.JobSlotMatchDTO;

@Mapper(uses = ReferenceMapper.class, imports = TsidMapper.class)
public interface JobSlotMapper {

    @Mapping(target = "id", expression = "java(TsidMapper.toExternal(slot.getId()))")
    @Mapping(target = "companyId", expression = "java(TsidMapper.toExternal(slot.getCompany().getId()))")
    @Mapping(target = "assignments", expression = "java(java.util.List.of())")
    @Mapping(target = "active", source = "active")
    @Mapping(target = "activeBoardPostId", ignore = true)
    @Mapping(target = "activeBoardPostNumber", ignore = true)
    JobSlotDTO toDto(JobSlot slot);

    @Mapping(target = "id", expression = "java(TsidMapper.toExternal(slot.getId()))")
    @Mapping(target = "companyId", expression = "java(TsidMapper.toExternal(slot.getCompany().getId()))")
    @Mapping(target = "companyName", source = "company.name")
    @Mapping(target = "companyTrainingShip", source = "company.trainingShip")
    @Mapping(target = "assignments", expression = "java(java.util.List.of())")
    JobSlotMatchDTO toMatch(JobSlot slot);
}
