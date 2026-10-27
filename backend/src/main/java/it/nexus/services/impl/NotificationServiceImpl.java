package it.nexus.services.impl;

import java.time.Clock;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.config.security.SecurityUtils;
import it.nexus.domain.AppUser;
import it.nexus.domain.Notification;
import it.nexus.domain.Ticket;
import it.nexus.domain.dto.NotificationDTO;
import it.nexus.domain.dto.NotificationsDTO;
import it.nexus.domain.enumeration.NotificationType;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.NotificationRepository;
import it.nexus.services.CurrentAppUserService;
import it.nexus.services.NotificationService;
import it.nexus.web.errors.NotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    /** Notifiche mostrate nell'elenco: le più recenti. */
    static final int LIST_SIZE = 50;

    private final NotificationRepository repository;
    private final CurrentAppUserService currentAppUserService;
    private final Clock clock;

    @Override
    public void notify(AppUser recipient, NotificationType type, Ticket ticket) {
        // chi agisce non riceve la propria notifica; le attività pianificate agiscono senza utente (Sistema)
        boolean byRecipient = SecurityUtils.getCurrentUser().isPresent()
                && recipient != null && recipient.getId().equals(currentAppUserService.getCurrentAppUser().getId());
        if (recipient == null || byRecipient) {
            return;
        }
        repository.save(new Notification(recipient, type, ticket));
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationsDTO mine() {
        Long me = currentAppUserService.getCurrentAppUser().getId();
        return new NotificationsDTO(repository.countByRecipientIdAndReadAtIsNull(me),
                repository.findByRecipientIdOrderByCreatedAtDescIdDesc(me, PageRequest.of(0, LIST_SIZE)).stream()
                        .map(NotificationServiceImpl::toDto)
                        .toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long unreadCount() {
        return repository.countByRecipientIdAndReadAtIsNull(currentAppUserService.getCurrentAppUser().getId());
    }

    @Override
    public void markRead(String notificationId) {
        Long me = currentAppUserService.getCurrentAppUser().getId();
        // le notifiche altrui risultano inesistenti
        Optional.ofNullable(TsidMapper.toInternal(notificationId))
                .flatMap(id -> repository.findByIdAndRecipientId(id, me))
                .orElseThrow(() -> new NotFoundException("Notifica non trovata"))
                .markRead(clock.instant());
    }

    @Override
    public void markAllRead() {
        repository.markAllRead(currentAppUserService.getCurrentAppUser().getId(), clock.instant());
    }

    private static NotificationDTO toDto(Notification n) {
        Ticket t = n.getTicket();
        return new NotificationDTO(TsidMapper.toExternal(n.getId()), n.getType(),
                t == null ? null : TsidMapper.toExternal(t.getId()), t == null ? null : t.getNumber(), n.getCreatedAt(),
                n.getReadAt() != null);
    }
}
