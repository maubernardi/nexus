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
    @Mapping(target = "blockedByTicketNumber", source = "blockedByTicket.number")
    @Mapping(target = "active", source = "active")
    JobSlotDTO toDto(JobSlot slot);

    @Mapping(target = "id", expression = "java(TsidMapper.toExternal(slot.getId()))")
    @Mapping(target = "companyId", expression = "java(TsidMapper.toExternal(slot.getCompany().getId()))")
    @Mapping(target = "companyName", source = "company.name")
    JobSlotMatchDTO toMatch(JobSlot slot);
}
