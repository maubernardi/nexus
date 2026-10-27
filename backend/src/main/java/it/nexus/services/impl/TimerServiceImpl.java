package it.nexus.services.impl;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.config.TimeConfig;
import it.nexus.domain.Ticket;
import it.nexus.domain.audit.AuditChanges;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.domain.enumeration.NotificationType;
import it.nexus.domain.enumeration.TicketStatus;
import it.nexus.repository.TicketRepository;
import it.nexus.services.AuditService;
import it.nexus.services.NotificationService;
import it.nexus.services.TimerService;
import it.nexus.services.WorkingDayService;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class TimerServiceImpl implements TimerService {

    /** Giorno lavorativo del promemoria dopo l'accettazione (brief, fase 5). */
    static final int REMINDER_WORKING_DAYS = 4;

    private final TicketRepository ticketRepository;
    private final WorkingDayService workingDayService;
    private final AuditService auditService;
    private final NotificationService notificationService;

    @Override
    public int sendDueReminders(Instant now) {
        LocalDate today = LocalDate.ofInstant(now, TimeConfig.ZONE);
        List<Ticket> pending = ticketRepository
                .findByStatusAndTimerStartedAtIsNotNullAndTimerReminderSentAtIsNull(TicketStatus.PROPOSTA_ACCOLTA);
        int sent = 0;
        for (Ticket ticket : pending) {
            LocalDate reminderDay = workingDayService.plusWorkingDays(
                    LocalDate.ofInstant(ticket.getTimerStartedAt(), TimeConfig.ZONE), REMINDER_WORKING_DAYS);
            if (today.isBefore(reminderDay)) {
                continue;
            }
            // segnare l'invio nella stessa transazione della notifica rende il promemoria unico
            ticket.setTimerReminderSentAt(now);
            ticketRepository.save(ticket);
            auditService.record(AuditEntityType.TICKET, ticket.getId(), "TIMER_REMINDER",
                    AuditChanges.of().value("timerReminderSentAt", null, now.toString()), null);
            notificationService.notify(ticket.getTutor(), NotificationType.TIMER_REMINDER, ticket);
            sent++;
        }
        return sent;
    }
}
