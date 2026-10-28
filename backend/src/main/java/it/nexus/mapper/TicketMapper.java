package it.nexus.mapper;

import java.time.LocalDate;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import it.nexus.config.TimeConfig;
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
    @Mapping(target = "timerDeadline", expression = "java(timerDeadline(ticket))")
    @Mapping(target = "timerDueSoon", expression = "java(ticket.getTimerDeadlineAt() != null && ticket.getTimerReminderSentAt() != null)")
    TicketListItemDTO toListItem(Ticket ticket);

    default LocalDate timerDeadline(Ticket ticket) {
        return ticket.getTimerDeadlineAt() == null ? null : TimeConfig.lastDay(ticket.getTimerDeadlineAt());
    }

    default String fullName(AppUser user) {
        return user == null ? null : user.getFirstName() + " " + user.getLastName();
    }
}
