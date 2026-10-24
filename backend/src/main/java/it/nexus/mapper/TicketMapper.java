package it.nexus.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import it.nexus.domain.AppUser;
import it.nexus.domain.Ticket;
import it.nexus.domain.dto.TicketListItemDTO;
import it.nexus.domain.dto.TicketDTO;

@Mapper(uses = {ReferenceMapper.class, BeneficiaryMapper.class}, imports = TsidMapper.class)
public interface TicketMapper {

    @Mapping(target = "id", expression = "java(TsidMapper.toExternal(ticket.getId()))")
    @Mapping(target = "fastTrack", source = "fastTrack")
    TicketDTO toDto(Ticket ticket);

    @Mapping(target = "id", expression = "java(TsidMapper.toExternal(ticket.getId()))")
    @Mapping(target = "fastTrack", source = "fastTrack")
    @Mapping(target = "tutorName", expression = "java(fullName(ticket.getTutor()))")
    @Mapping(target = "assignedOperatorName", expression = "java(fullName(ticket.getAssignedCcOperator()))")
    TicketListItemDTO toListItem(Ticket ticket);

    default String fullName(AppUser user) {
        return user == null ? null : user.getFirstName() + " " + user.getLastName();
    }
}
