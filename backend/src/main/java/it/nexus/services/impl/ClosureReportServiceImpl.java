package it.nexus.services.impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.domain.AbstractReferenceEntity;
import it.nexus.domain.ClosureReason;
import it.nexus.domain.ClosureReport;
import it.nexus.domain.HiringFormula;
import it.nexus.domain.Internship;
import it.nexus.domain.NoHireReason;
import it.nexus.domain.Ticket;
import it.nexus.domain.audit.AuditChanges;
import it.nexus.domain.dto.ClosureReportDTO;
import it.nexus.domain.dto.ClosureReportFormDTO;
import it.nexus.mapper.ReferenceMapper;
import it.nexus.mapper.TsidMapper;
import it.nexus.repository.ClosureReasonRepository;
import it.nexus.repository.ClosureReportRepository;
import it.nexus.repository.HiringFormulaRepository;
import it.nexus.repository.InternshipRepository;
import it.nexus.repository.NoHireReasonRepository;
import it.nexus.services.ClosureReportService;
import it.nexus.services.UserDisplayNameService;
import it.nexus.web.errors.FieldValidationException;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ClosureReportServiceImpl implements ClosureReportService {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ITALIAN);

    private final ClosureReportRepository repository;
    private final HiringFormulaRepository hiringFormulaRepository;
    private final NoHireReasonRepository noHireReasonRepository;
    private final InternshipRepository internshipRepository;
    private final ClosureReasonRepository closureReasonRepository;
    private final UserDisplayNameService userDisplayNameService;
    private final ReferenceMapper referenceMapper;

    @Override
    public AuditChanges create(Ticket ticket, ClosureReportFormDTO form) {
        checkActualEnd(ticket, form.actualEndDate());
        ClosureReason closureReason = closureReason(ticket, form);
        // la descrizione vale solo per un'interruzione anticipata (PO, 16/11)
        String interruptionDetails = closureReason.isRegular() ? null : text(form.interruptionDetails());
        if (!closureReason.isRegular() && interruptionDetails == null) {
            throw new FieldValidationException("interruptionDetails", "Descrivi l'interruzione");
        }
        // conta solo la voce dell'esito scelto: l'altra può essere rimasta nel modulo dopo un ripensamento
        HiringFormula formula = null;
        NoHireReason reason = null;
        if (form.hired()) {
            formula = activeEntry(form.hiringFormulaId(), hiringFormulaRepository, "hiringFormulaId",
                    "Scegli la formula di assunzione", "Formula di assunzione non valida");
        } else {
            reason = activeEntry(form.noHireReasonId(), noHireReasonRepository, "noHireReasonId",
                    "Scegli il motivo della mancata assunzione", "Motivo non valido");
        }
        ClosureReport report = repository.save(new ClosureReport(ticket, form.actualEndDate(), closureReason,
                interruptionDetails, form.companySatisfaction().shortValue(),
                form.beneficiarySatisfaction().shortValue(), text(form.objectives()), text(form.difficulties()), formula,
                reason, form.jobSlotStillAvailable()));
        return AuditChanges.of()
                .value("actualEndDate", null, form.actualEndDate().toString())
                .value("closureReason", null, closureReason.getCode())
                .value("companySatisfaction", null, form.companySatisfaction())
                .value("beneficiarySatisfaction", null, form.beneficiarySatisfaction())
                .value("hired", null, report.isHired())
                .value("hiringFormula", null, formula == null ? null : formula.getCode())
                .value("noHireReason", null, reason == null ? null : reason.getCode())
                .value("jobSlotStillAvailable", null, report.isJobSlotStillAvailable());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ClosureReportDTO> find(Ticket ticket) {
        return repository.findByTicketId(ticket.getId()).map(r -> new ClosureReportDTO(
                r.getActualEndDate(),
                referenceMapper.toDto(r.getClosureReason()),
                r.getInterruptionDetails(),
                r.getCompanySatisfaction(),
                r.getBeneficiarySatisfaction(),
                r.getObjectives(),
                r.getDifficulties(),
                r.isHired(),
                referenceMapper.toDto(r.getHiringFormula()),
                referenceMapper.toDto(r.getNoHireReason()),
                r.isJobSlotStillAvailable(),
                r.getCreatedAt(),
                userDisplayNameService.displayNames(List.of(r.getCreatedBy())).get(r.getCreatedBy())));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<String> outcome(Long ticketId) {
        // il motivo di chiusura in testa (US-1103), se c'è: i report di prima non l'hanno
        return repository.findByTicketId(ticketId).map(r -> (r.getClosureReason() == null ? ""
                : r.getClosureReason().getName() + "; ") + "%s; %s".formatted(
                r.isHired() ? "Assunto: " + r.getHiringFormula().getName()
                        : "Non assunto: " + r.getNoHireReason().getName(),
                r.isJobSlotStillAvailable() ? "mansione di nuovo libera" : "mansione non più disponibile"));
    }

    /**
     * La fine effettiva (US-1009) non precede l'inizio della scheda; se il Tutor ha svuotato l'inizio (la scheda si salva
     * anche incompleta) non c'è con cosa confrontarla. Nessun limite verso il futuro: proroghe e report inviati
     * nell'ultima settimana (D3).
     */
    private void checkActualEnd(Ticket ticket, LocalDate actualEnd) {
        LocalDate start = internshipRepository.findByTicketId(ticket.getId()).map(Internship::getStartDate).orElse(null);
        if (start != null && actualEnd.isBefore(start)) {
            throw new FieldValidationException("actualEndDate",
                    "La fine effettiva non può precedere l'inizio del tirocinio (%s)".formatted(DAY.format(start)));
        }
    }

    /**
     * Motivo di chiusura (US-1103): con la fine effettiva prima di quella prevista della scheda serve un'interruzione
     * attiva; altrimenti la chiusura regolare, qualunque cosa indichi il modulo (D3).
     */
    private ClosureReason closureReason(Ticket ticket, ClosureReportFormDTO form) {
        // con le proroghe, la fine dell'ultima convenzione (US-1012)
        LocalDate expectedEnd = internshipRepository.findByTicketId(ticket.getId())
                .map(Internship::currentEndDate)
                .orElse(null);
        if (expectedEnd == null || !form.actualEndDate().isBefore(expectedEnd)) {
            return closureReasonRepository.findByCode(ClosureReason.REGULAR)
                    .orElseThrow(() -> new IllegalStateException("Manca la chiusura regolare nel catalogo"));
        }
        ClosureReason reason = activeEntry(form.closureReasonId(), closureReasonRepository, "closureReasonId",
                "Il tirocinio finisce prima del %s: scegli il motivo dell'interruzione".formatted(DAY.format(expectedEnd)),
                "Motivo non valido");
        if (reason.isRegular()) {
            throw new FieldValidationException("closureReasonId",
                    "Il tirocinio finisce prima del %s: scegli il motivo dell'interruzione".formatted(
                            DAY.format(expectedEnd)));
        }
        return reason;
    }

    /** Voce attiva del catalogo: le voci disattivate non si scelgono più (US-109). */
    private static <T extends AbstractReferenceEntity> T activeEntry(String id, JpaRepository<T, Long> repository,
            String field, String missing, String invalid) {
        if (id == null || id.isBlank()) {
            throw new FieldValidationException(field, missing);
        }
        return Optional.ofNullable(TsidMapper.toInternal(id))
                .flatMap(repository::findById)
                .filter(T::isActive)
                .orElseThrow(() -> new FieldValidationException(field, invalid));
    }

    private static String text(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
