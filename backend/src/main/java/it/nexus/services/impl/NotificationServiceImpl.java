package it.nexus.services.impl;

import java.time.Clock;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.config.security.SecurityUtils;
import it.nexus.domain.AppUser;
import it.nexus.domain.Company;
import it.nexus.domain.ContactRequest;
import it.nexus.domain.Notification;
import it.nexus.domain.Ticket;
import it.nexus.domain.dto.NotificationDTO;
import it.nexus.domain.dto.NotificationsDTO;
import it.nexus.domain.enumeration.MessageKey;
import it.nexus.domain.enumeration.MessagePart;
import it.nexus.domain.enumeration.NotificationType;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.NotificationRepository;
import it.nexus.services.CurrentAppUserService;
import it.nexus.services.MessageTextService;
import it.nexus.services.NotificationService;
import it.nexus.services.mail.NotificationCreatedEvent;
import it.nexus.services.messages.MessageTexts;
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
    private final ApplicationEventPublisher events;
    private final MessageTextService messageTextService;

    @Override
    public void notify(AppUser recipient, NotificationType type, Ticket ticket, Company company) {
        // chi agisce non riceve la propria notifica; le attività pianificate agiscono senza utente (Sistema)
        boolean byRecipient = SecurityUtils.getCurrentUser().isPresent()
                && recipient != null && recipient.getId().equals(currentAppUserService.getCurrentAppUser().getId());
        if (recipient == null || byRecipient) {
            return;
        }
        Notification saved = repository.save(new Notification(recipient, type, ticket, company));
        // anche via email, dopo il commit (US-1202)
        events.publishEvent(new NotificationCreatedEvent(saved.getId(), recipient.getEmail(), type,
                ticket == null ? null : TsidMapper.toExternal(ticket.getId()),
                ticket == null ? null : ticket.getNumber(),
                company == null ? null : TsidMapper.toExternal(company.getId()), null));
    }

    @Override
    public void notify(AppUser recipient, NotificationType type, ContactRequest contactRequest) {
        boolean byRecipient = SecurityUtils.getCurrentUser().isPresent()
                && recipient != null && recipient.getId().equals(currentAppUserService.getCurrentAppUser().getId());
        if (recipient == null || byRecipient) {
            return;
        }
        Notification saved = repository.save(Notification.forContactRequest(recipient, type, contactRequest));
        events.publishEvent(new NotificationCreatedEvent(saved.getId(), recipient.getEmail(), type, null, null, null,
                new NotificationCreatedEvent.ContactRequestTarget(TsidMapper.toExternal(contactRequest.getId()),
                        contactRequest.getNumber(), contactRequestValues(contactRequest))));
    }

    /** Segnaposto dei testi sulla richiesta di contatto (US-512): numero, esito e nota della chiusura. */
    private static Map<String, Object> contactRequestValues(ContactRequest r) {
        Map<String, Object> values = new HashMap<>();
        values.put("numero", r.getNumber());
        values.put("esito", r.getOutcome() == null ? null : r.getOutcome().label());
        values.put("nota", r.getClosingNote());
        return values;
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationsDTO mine() {
        Long me = currentAppUserService.getCurrentAppUser().getId();
        MessageTexts texts = messageTextService.current();
        return new NotificationsDTO(repository.countByRecipientIdAndReadAtIsNull(me),
                repository.findByRecipientIdOrderByCreatedAtDescIdDesc(me, PageRequest.of(0, LIST_SIZE)).stream()
                        .map(n -> toDto(n, texts))
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

    /** Con il testo della notifica nell'app, quello dell'ADMIN o il predefinito (US-1204). */
    private static NotificationDTO toDto(Notification n, MessageTexts texts) {
        Ticket t = n.getTicket();
        ContactRequest r = n.getContactRequest();
        Map<String, Object> values;
        if (r != null) {
            values = contactRequestValues(r);
        } else {
            values = new HashMap<>();
            values.put("numero", t == null ? null : t.getNumber());
        }
        Company c = n.getCompany();
        return new NotificationDTO(TsidMapper.toExternal(n.getId()), n.getType(),
                t == null ? null : TsidMapper.toExternal(t.getId()), t == null ? null : t.getNumber(),
                c == null ? null : TsidMapper.toExternal(c.getId()),
                r == null ? null : TsidMapper.toExternal(r.getId()), r == null ? null : r.getNumber(),
                n.getCreatedAt(), n.getReadAt() != null,
                texts.render(MessageKey.of(n.getType()), MessagePart.APP, values));
    }
}
