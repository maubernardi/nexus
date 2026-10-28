package it.nexus.services.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.domain.BoardPost;
import it.nexus.domain.Ticket;
import it.nexus.domain.audit.AuditChanges;
import it.nexus.domain.enumeration.NotificationType;
import it.nexus.domain.workflow.TicketTransitions;
import it.nexus.repository.TicketRepository;
import it.nexus.services.FastTrackService;
import it.nexus.services.NotificationService;
import it.nexus.services.TicketStateMachine;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class FastTrackServiceImpl implements FastTrackService {

    private final TicketStateMachine stateMachine;
    private final TicketRepository ticketRepository;
    private final NotificationService notificationService;

    @Override
    public Ticket reject(Ticket ticket, long expectedVersion, String reason) {
        Ticket saved = stateMachine.apply(ticket, TicketTransitions.FAST_TRACK_REJECT, expectedVersion, reason,
                FastTrackServiceImpl::endFastTrack);
        notificationService.notify(saved.getTutor(), NotificationType.FAST_TRACK_REJECTED, saved);
        return saved;
    }

    @Override
    public void dropPending(BoardPost post, String reason) {
        ticketRepository.findFirstByBoardPostIdAndFastTrackTrue(post.getId()).ifPresent(ticket -> {
            Ticket saved = stateMachine.apply(ticket, TicketTransitions.FAST_TRACK_DROP, ticket.getVersion(), reason,
                    FastTrackServiceImpl::endFastTrack);
            notificationService.notify(saved.getTutor(), NotificationType.FAST_TRACK_DROPPED, saved);
        });
    }

    /** La corsia veloce finisce; il tipo SPECIAL e l'annuncio restano come traccia dell'origine. */
    private static AuditChanges endFastTrack(Ticket ticket) {
        ticket.setFastTrack(false);
        return AuditChanges.of().value("fastTrack", true, false);
    }
}
