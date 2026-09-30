package it.nexus.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import it.nexus.domain.Ticket;
import it.nexus.domain.dto.TicketDTO;

@Mapper(uses = {ReferenceMapper.class, CandidateMapper.class}, imports = TsidMapper.class)
public interface TicketMapper {

    @Mapping(target = "id", expression = "java(TsidMapper.toExternal(ticket.getId()))")
    @Mapping(target = "fastTrack", source = "fastTrack")
    TicketDTO toDto(Ticket ticket);
}
