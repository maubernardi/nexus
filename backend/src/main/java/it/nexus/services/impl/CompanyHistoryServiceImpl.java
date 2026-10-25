package it.nexus.services.impl;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.domain.AbstractCreationAuditingEntity;
import it.nexus.domain.CompanyAuditEvent;
import it.nexus.domain.Ticket;
import it.nexus.domain.TicketCompanyBlacklist;
import it.nexus.domain.dto.CompanyHistoryDTO;
import it.nexus.domain.enumeration.CompanyEventType;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.CompanyAuditEventRepository;
import it.nexus.repository.CompanyRepository;
import it.nexus.repository.TicketCompanyBlacklistRepository;
import it.nexus.services.CompanyHistoryService;
import it.nexus.services.UserDisplayNameService;
import it.nexus.web.errors.NotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CompanyHistoryServiceImpl implements CompanyHistoryService {

    /** Tipo degli eventi che vengono dalle esclusioni dell'azienda per una segnalazione. */
    static final String EXCLUSION = "ESCLUSIONE";

    private final CompanyRepository companyRepository;
    private final CompanyAuditEventRepository eventRepository;
    private final TicketCompanyBlacklistRepository blacklistRepository;
    private final UserDisplayNameService userDisplayNameService;

    @Override
    public CompanyHistoryDTO history(String companyId) {
        Long id = Optional.ofNullable(TsidMapper.toInternal(companyId))
                .flatMap(companyRepository::findById)
                .orElseThrow(() -> new NotFoundException("Azienda non trovata"))
                .getId();
        List<CompanyAuditEvent> events = eventRepository.findWithTicketByCompanyId(id);
        List<TicketCompanyBlacklist> exclusions = blacklistRepository.findByIdCompanyId(id);
        Map<String, String> names = userDisplayNameService.displayNames(
                Stream.concat(events.stream(), exclusions.stream()).map(AbstractCreationAuditingEntity::getCreatedBy).toList());

        List<CompanyHistoryDTO.Event> items = Stream.concat(
                events.stream().map(e -> event(e.getCreatedAt(), e.getEventType(), e.getTicket(), describe(e),
                        names.get(e.getCreatedBy()))),
                exclusions.stream().map(x -> event(x.getCreatedAt(), EXCLUSION, x.getTicket(), x.getReason(),
                        names.get(x.getCreatedBy()))))
                .sorted(Comparator.comparing(CompanyHistoryDTO.Event::at).reversed())
                .toList();
        return new CompanyHistoryDTO(summary(events), items);
    }

    private static CompanyHistoryDTO.Summary summary(List<CompanyAuditEvent> events) {
        return new CompanyHistoryDTO.Summary(
                count(events, CompanyEventType.PROPOSTA_INVIATA),
                count(events, CompanyEventType.PROPOSTA_ACCOLTA),
                count(events, CompanyEventType.APPUNTAMENTO_FALLITO),
                count(events, CompanyEventType.TIROCINIO_AVVIATO),
                events.stream().filter(e -> isConcluded(e) && hired(e)).count(),
                events.stream().filter(e -> isConcluded(e) && !hired(e)).count());
    }

    private static long count(List<CompanyAuditEvent> events, CompanyEventType type) {
        return events.stream().filter(e -> type.name().equals(e.getEventType())).count();
    }

    private static boolean isConcluded(CompanyAuditEvent event) {
        return CompanyEventType.TIROCINIO_CONCLUSO.name().equals(event.getEventType());
    }

    private static boolean hired(CompanyAuditEvent event) {
        return Boolean.TRUE.equals(event.getDetails().get("assunto"));
    }

    /** Dettaglio leggibile dell'evento: la mansione proposta o l'esito. */
    private static String describe(CompanyAuditEvent event) {
        Map<String, Object> d = event.getDetails();
        if (CompanyEventType.PROPOSTA_INVIATA.name().equals(event.getEventType()) && d.get("jobSlotTitle") != null) {
            return "«" + d.get("jobSlotTitle") + "»";
        }
        if (CompanyEventType.APPUNTAMENTO_FALLITO.name().equals(event.getEventType())
                && Boolean.TRUE.equals(d.get("rilancioInBacheca"))) {
            return "Con rilancio in bacheca";
        }
        if (isConcluded(event)) {
            return hired(event) ? "Con assunzione" : "Senza assunzione";
        }
        return null;
    }

    private static CompanyHistoryDTO.Event event(Instant at, String type, Ticket ticket, String detail,
            String authorName) {
        return new CompanyHistoryDTO.Event(at, type,
                ticket == null ? null : TsidMapper.toExternal(ticket.getId()),
                ticket == null ? null : ticket.getNumber(), detail, authorName);
    }
}
