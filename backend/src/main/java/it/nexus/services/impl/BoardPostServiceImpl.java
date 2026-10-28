package it.nexus.services.impl;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.domain.BoardPost;
import it.nexus.domain.JobSlot;
import it.nexus.domain.Project;
import it.nexus.domain.audit.AuditChanges;
import it.nexus.domain.dto.BoardPostDTO;
import it.nexus.domain.dto.BoardPostFormDTO;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.domain.enumeration.BoardPostStatus;
import it.nexus.domain.enumeration.JobSlotStatus;
import it.nexus.mapper.BoardPostMapper;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.BoardPostRepository;
import it.nexus.repository.ProjectRepository;
import it.nexus.services.AuditService;
import it.nexus.services.BoardPostService;
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
        JobSlot slot = post.getJobSlot();
        if (!slot.isActive() || !slot.getCompany().isActive()) {
            throw new ConflictException("La mansione dell'annuncio #%d non è a disposizione", post.getNumber());
        }
        if (slot.getStatus() == JobSlotStatus.BLOCCATA) {
            throw new ConflictException("La mansione dell'annuncio #%d è bloccata dalla segnalazione n. %d",
                    post.getNumber(), slot.getBlockedByTicket().getNumber());
        }
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
