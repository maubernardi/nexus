package it.nexus.services.impl;

import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.domain.BoardPost;
import it.nexus.domain.JobSlot;
import it.nexus.domain.Project;
import it.nexus.domain.Ticket;
import it.nexus.domain.audit.AuditChanges;
import it.nexus.domain.dto.BoardPostCreateDTO;
import it.nexus.domain.dto.BoardPostDTO;
import it.nexus.domain.dto.BoardPostFormDTO;
import it.nexus.domain.dto.TutorBoardPostDTO;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.domain.enumeration.BoardPostStatus;
import it.nexus.domain.enumeration.JobSlotStatus;
import it.nexus.mapper.BoardPostMapper;
import it.nexus.mapper.ReferenceMapper;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.BoardPostRepository;
import it.nexus.repository.JobSlotRepository;
import it.nexus.repository.ProjectRepository;
import it.nexus.repository.UserProjectRepository;
import it.nexus.services.AuditService;
import it.nexus.services.BoardPostService;
import it.nexus.services.CurrentAppUserService;
import it.nexus.web.errors.BadRequestException;
import it.nexus.web.errors.ConflictException;
import it.nexus.web.errors.FieldValidationException;
import it.nexus.web.errors.NotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class BoardPostServiceImpl implements BoardPostService {

    private final BoardPostRepository repository;
    private final ProjectRepository projectRepository;
    private final JobSlotRepository jobSlotRepository;
    private final UserProjectRepository userProjectRepository;
    private final CurrentAppUserService currentAppUserService;
    private final ReferenceMapper referenceMapper;
    private final AuditService auditService;
    private final BoardPostMapper mapper;
    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public List<BoardPostDTO> list(BoardPostStatus status) {
        return repository.findByStatusOrderByNumberDesc(status).stream().map(mapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BoardPostDTO get(String id) {
        return mapper.toDto(find(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TutorBoardPostDTO> listForTutor(String zoneId, String jobCategoryId) {
        Long me = currentAppUserService.getCurrentAppUser().getId();
        List<Long> projects = userProjectRepository.findByIdUserId(me).stream()
                .map(up -> up.getId().getProjectId()).toList();
        // nessun progetto: solo gli annunci per tutti (un "in" vuoto non è SQL valido ovunque)
        List<Long> visible = projects.isEmpty() ? List.of(-1L) : projects;
        return repository.findVisibleToTutor(visible, filterId(zoneId, "zona"), filterId(jobCategoryId, "tipologia"))
                .stream()
                .map(p -> new TutorBoardPostDTO(
                        TsidMapper.toExternal(p.getId()),
                        p.getNumber(),
                        referenceMapper.toDto(p.getJobSlot().getJobCategory()),
                        referenceMapper.toDto(p.getJobSlot().getZone()),
                        p.getAgeMin(),
                        p.getAgeMax(),
                        p.getWeeklyHours(),
                        p.getDurationMonths(),
                        referenceMapper.toDto(p.getProject()),
                        p.getPublishedAt()))
                .toList();
    }

    @Override
    public BoardPostDTO create(BoardPostCreateDTO dto) {
        JobSlot slot = Optional.ofNullable(TsidMapper.toInternal(dto.jobSlotId()))
                .flatMap(jobSlotRepository::findById)
                .orElseThrow(() -> new FieldValidationException("jobSlotId", "Mansione non valida"));
        requireProposable(slot, "La mansione «%s»".formatted(slot.getTitle()));
        repository.findFirstByJobSlot_IdAndStatusIn(slot.getId(), BoardPostStatus.ACTIVE).ifPresent(p -> {
            throw new ConflictException("La mansione «%s» ha già l'annuncio #%d", slot.getTitle(), p.getNumber());
        });
        BoardPost saved = repository.saveAndFlush(new BoardPost(slot, slot.getTitle()));
        auditService.record(AuditEntityType.BOARD_POST, saved.getId(), "CREATE", AuditChanges.of()
                .value("jobSlotId", null, TsidMapper.toExternal(slot.getId()))
                .value("title", null, saved.getTitle())
                .value("status", null, saved.getStatus().name()), null);
        return mapper.toDto(saved);
    }

    @Override
    public BoardPostDTO update(String id, BoardPostFormDTO dto) {
        BoardPost post = find(id);
        requireVersion(post, dto.version());
        if (post.getStatus() == BoardPostStatus.ARCHIVED) {
            throw new ConflictException("L'annuncio #%d è archiviato: non si può modificare", post.getNumber());
        }
        if (dto.ageMin() != null && dto.ageMax() != null && dto.ageMin() > dto.ageMax()) {
            throw new FieldValidationException("ageMax", "L'età massima non può essere minore della minima");
        }
        Project project = post.getProject();
        if (post.getStatus() == BoardPostStatus.DRAFT) {
            project = project(dto.projectId(), post.getProject());
        } else if (!Objects.equals(external(post.getProject()), blankToNull(dto.projectId()))) {
            // pubblicato: si rende pubblico solo con lo sblocco dopo 7 giorni (US-903)
            throw new ConflictException("Il progetto dell'annuncio #%d si sceglie solo nella bozza", post.getNumber());
        }
        AuditChanges changes = AuditChanges.of()
                .value("title", post.getTitle(), dto.title().strip())
                .value("projectId", external(post.getProject()), external(project))
                .value("ageMin", post.getAgeMin(), dto.ageMin())
                .value("ageMax", post.getAgeMax(), dto.ageMax())
                .value("weeklyHours", post.getWeeklyHours(), dto.weeklyHours())
                .value("durationMonths", post.getDurationMonths(), dto.durationMonths())
                .value("notes", post.getNotes(), blankToNull(dto.notes()));
        post.setTitle(dto.title().strip());
        post.setProject(project);
        post.setAgeMin(dto.ageMin());
        post.setAgeMax(dto.ageMax());
        post.setWeeklyHours(dto.weeklyHours());
        post.setDurationMonths(dto.durationMonths());
        post.setNotes(blankToNull(dto.notes()));
        BoardPost saved = repository.saveAndFlush(post);
        if (!changes.asMap().isEmpty()) {
            auditService.record(AuditEntityType.BOARD_POST, saved.getId(), "UPDATE", changes, null);
        }
        return mapper.toDto(saved);
    }

    @Override
    public BoardPostDTO publish(String id, long expectedVersion) {
        BoardPost post = find(id);
        requireVersion(post, expectedVersion);
        if (post.getStatus() != BoardPostStatus.DRAFT) {
            throw new ConflictException("Si pubblica solo una bozza: l'annuncio #%d non lo è", post.getNumber());
        }
        requireProposable(post.getJobSlot(), "La mansione dell'annuncio #%d".formatted(post.getNumber()));
        Instant now = clock.instant();
        post.setStatus(BoardPostStatus.PUBLISHED);
        post.setPublishedAt(now);
        BoardPost saved = repository.saveAndFlush(post);
        auditService.record(AuditEntityType.BOARD_POST, saved.getId(), "PUBLISH", AuditChanges.of()
                .value("status", BoardPostStatus.DRAFT.name(), BoardPostStatus.PUBLISHED.name())
                .value("publishedAt", null, now.toString()), null);
        return mapper.toDto(saved);
    }

    @Override
    public BoardPostDTO archive(String id, long expectedVersion) {
        BoardPost post = find(id);
        requireVersion(post, expectedVersion);
        BoardPostStatus before = post.getStatus();
        if (before == BoardPostStatus.ARCHIVED) {
            throw new ConflictException("L'annuncio #%d è già archiviato", post.getNumber());
        }
        post.setStatus(BoardPostStatus.ARCHIVED);
        BoardPost saved = repository.saveAndFlush(post);
        auditService.record(AuditEntityType.BOARD_POST, saved.getId(), "ARCHIVE", AuditChanges.of()
                .value("status", before.name(), BoardPostStatus.ARCHIVED.name()), null);
        return mapper.toDto(saved);
    }

    /** La mansione si può offrire in bacheca: libera, a disposizione, di un'azienda attiva (design D3). */
    private static void requireProposable(JobSlot slot, String subject) {
        if (!slot.isActive() || !slot.getCompany().isActive()) {
            throw new ConflictException("%s non è a disposizione", subject);
        }
        if (slot.getStatus() == JobSlotStatus.BLOCCATA) {
            throw new ConflictException("%s è bloccata dalla segnalazione n. %d", subject,
                    slot.getBlockedByTicket().getNumber());
        }
    }

    @Override
    public Long createRelaunchDraft(JobSlot slot, Ticket ticket) {
        String subject = "La mansione «%s»".formatted(slot.getTitle());
        requireProposable(slot, subject);
        repository.findFirstByJobSlot_IdAndStatusIn(slot.getId(), BoardPostStatus.ACTIVE).ifPresent(p -> {
            throw new ConflictException("%s ha già l'annuncio #%d", subject, p.getNumber());
        });
        BoardPost post = new BoardPost(slot, slot.getTitle());
        post.setProject(ticket.getProject());
        post.setCreatedFromTicket(ticket);
        BoardPost saved = repository.saveAndFlush(post);
        auditService.record(AuditEntityType.BOARD_POST, saved.getId(), "CREATE", AuditChanges.of()
                .value("jobSlotId", null, TsidMapper.toExternal(slot.getId()))
                .value("title", null, saved.getTitle())
                .value("projectId", null, TsidMapper.toExternal(ticket.getProject().getId()))
                .value("createdFromTicketId", null, TsidMapper.toExternal(ticket.getId()))
                .value("status", null, saved.getStatus().name()),
                "Rilancio dopo l'appuntamento fallito della segnalazione n. %d".formatted(ticket.getNumber()));
        return saved.getNumber();
    }

    @Override
    public List<Long> archiveForUnavailableSlots(Collection<Long> jobSlotIds, String reason) {
        if (jobSlotIds.isEmpty()) {
            return List.of();
        }
        return repository.findByJobSlot_IdInAndStatusIn(jobSlotIds, BoardPostStatus.ACTIVE).stream()
                .map(post -> {
                    BoardPostStatus before = post.getStatus();
                    post.setStatus(BoardPostStatus.ARCHIVED);
                    BoardPost saved = repository.saveAndFlush(post);
                    auditService.record(AuditEntityType.BOARD_POST, saved.getId(), "ARCHIVE", AuditChanges.of()
                            .value("status", before.name(), BoardPostStatus.ARCHIVED.name()), reason);
                    return saved.getNumber();
                })
                .toList();
    }

    private BoardPost find(String id) {
        return Optional.ofNullable(TsidMapper.toInternal(id))
                .flatMap(repository::findById)
                .orElseThrow(() -> new NotFoundException("Annuncio non trovato"));
    }

    /** Progetto attivo, oppure quello già assegnato anche se nel frattempo disattivato; vuoto = tutti i progetti. */
    private Project project(String projectId, Project current) {
        if (blankToNull(projectId) == null) {
            return null;
        }
        return Optional.ofNullable(TsidMapper.toInternal(projectId))
                .flatMap(projectRepository::findById)
                .filter(p -> p.isActive() || p.equals(current))
                .orElseThrow(() -> new FieldValidationException("projectId", "Progetto non valido"));
    }

    private static Long filterId(String externalId, String what) {
        if (externalId == null || externalId.isBlank()) {
            return null;
        }
        Long id = TsidMapper.toInternal(externalId);
        if (id == null) {
            throw new BadRequestException("Filtro %s non valido", what);
        }
        return id;
    }

    private static void requireVersion(BoardPost post, long expectedVersion) {
        if (post.getVersion() != expectedVersion) {
            throw new ConflictException("L'annuncio è stato modificato nel frattempo: ricarica la pagina e riprova");
        }
    }

    private static String external(Project project) {
        return project == null ? null : TsidMapper.toExternal(project.getId());
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
