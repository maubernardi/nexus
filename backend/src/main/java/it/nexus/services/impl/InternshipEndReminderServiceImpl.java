package it.nexus.services.impl;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.config.TimeConfig;
import it.nexus.domain.AppUser;
import it.nexus.domain.Internship;
import it.nexus.domain.Ticket;
import it.nexus.domain.audit.AuditChanges;
import it.nexus.domain.dto.InternshipToCloseDTO;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.domain.enumeration.NotificationType;
import it.nexus.domain.enumeration.TicketStatus;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.InternshipRepository;
import it.nexus.services.AuditService;
import it.nexus.services.InternshipEndReminderService;
import it.nexus.services.NotificationService;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class InternshipEndReminderServiceImpl implements InternshipEndReminderService {

    /** Gli avvisi, in ordine: giorni dalla fine prevista (negativi prima) e testo per l'audit. */
    private static final int[] DAYS = {-7, -3, 3, 7};
    private static final String[] LABELS = {"7 giorni prima della fine", "3 giorni prima della fine",
        "3 giorni dopo la fine", "7 giorni dopo la fine"};

    private final InternshipRepository internshipRepository;
    private final AuditService auditService;
    private final NotificationService notificationService;
    private final Clock clock;

    @Override
    public int sendDue(Instant now) {
        LocalDate today = LocalDate.ofInstant(now, TimeConfig.ZONE);
        int sent = 0;
        for (Internship internship : internshipRepository.findWithExtensionsByTicketStatus(TicketStatus.IN_TIROCINIO)) {
            LocalDate end = internship.currentEndDate();
            if (end == null) {
                continue;
            }
            // una fine diversa (una proroga) fa ripartire gli avvisi
            int last = end.equals(internship.getEndReminderFor()) ? internship.getEndReminderStage() : 0;
            int due = dueStage(end, today);
            if (due <= last) {
                continue;
            }
            internshipRepository.markEndReminder(internship.getId(), end, due);
            Ticket ticket = internship.getTicket();
            auditService.record(AuditEntityType.TICKET, ticket.getId(), "INTERNSHIP_END_REMINDER", AuditChanges.of()
                    .value("endReminder", null, LABELS[due - 1])
                    .value("expectedEndDate", null, end.toString()), null);
            notificationService.notify(ticket.getTutor(),
                    DAYS[due - 1] < 0 ? NotificationType.INTERNSHIP_ENDING : NotificationType.INTERNSHIP_OVERDUE,
                    ticket);
            sent++;
        }
        return sent;
    }

    @Override
    @Transactional(readOnly = true)
    public List<InternshipToCloseDTO> toClose(AppUser tutor) {
        LocalDate today = LocalDate.now(clock);
        return internshipRepository.findWithExtensionsByTicketStatusAndTutorId(TicketStatus.IN_TIROCINIO, tutor.getId())
                .stream()
                .filter(i -> i.currentEndDate() != null && i.currentEndDate().isBefore(today))
                .map(i -> new InternshipToCloseDTO(TsidMapper.toExternal(i.getTicket().getId()),
                        i.getTicket().getNumber(), i.currentEndDate()))
                .sorted(Comparator.comparing(InternshipToCloseDTO::ticketNumber))
                .toList();
    }

    /** L'avviso più recente dovuto oggi per la fine indicata: 0 nessuno, poi 1–4 come {@link #DAYS}. */
    static int dueStage(LocalDate end, LocalDate today) {
        int due = 0;
        for (int stage = 1; stage <= DAYS.length; stage++) {
            if (!today.isBefore(end.plusDays(DAYS[stage - 1]))) {
                due = stage;
            }
        }
        return due;
    }
}
