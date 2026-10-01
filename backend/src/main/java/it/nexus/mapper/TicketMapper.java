package it.nexus.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import it.nexus.domain.Ticket;
import it.nexus.domain.dto.QueueItemDTO;
import it.nexus.domain.dto.TicketDTO;

@Mapper(uses = {ReferenceMapper.class, BeneficiaryMapper.class}, imports = TsidMapper.class)
public interface TicketMapper {

    @Mapping(target = "id", expression = "java(TsidMapper.toExternal(ticket.getId()))")
    @Mapping(target = "fastTrack", source = "fastTrack")
    TicketDTO toDto(Ticket ticket);

    @Mapping(target = "id", expression = "java(TsidMapper.toExternal(ticket.getId()))")
    @Mapping(target = "fastTrack", source = "fastTrack")
    @Mapping(target = "tutorName", expression = "java(ticket.getTutor().getFirstName() + \" \" + ticket.getTutor().getLastName())")
    QueueItemDTO toQueueItem(Ticket ticket);
}
