package it.nexus.domain;

import java.io.Serial;
import java.time.LocalDate;

import org.hibernate.Hibernate;
import org.hibernate.annotations.Immutable;
import org.hibernate.type.SqlTypes;
import org.hibernate.annotations.JdbcTypeCode;

import it.nexus.domain.id.TsidId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Report di restituzione (US-1101), uno per segnalazione: non si modifica dopo l'invio, il database rifiuta UPDATE,
 * DELETE e TRUNCATE e l'entità è {@link Immutable}. Obiettivi e difficoltà possono contenere dati personali: mai nei
 * log né nell'audit. Istante e autore dell'invio sono {@code createdAt}/{@code createdBy}.
 */
@Getter
@Entity
@Immutable
@Table(name = "closure_report")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClosureReport extends AbstractCreationAuditingEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @TsidId
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false, unique = true, updatable = false)
    private Ticket ticket;

    @Column(name = "company_satisfaction", nullable = false, updatable = false)
    private short companySatisfaction;

    @Column(name = "beneficiary_satisfaction", nullable = false, updatable = false)
    private short beneficiarySatisfaction;

    @Column(name = "objectives", updatable = false, length = 2000)
    private String objectives;

    @Column(name = "difficulties", updatable = false, length = 2000)
    private String difficulties;

    @Column(name = "hired", nullable = false, updatable = false)
    private boolean hired;

    /** Solo se assunto. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hiring_formula_id", updatable = false)
    private HiringFormula hiringFormula;

    /** Solo se non assunto. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "no_hire_reason_id", updatable = false)
    private NoHireReason noHireReason;

    @Column(name = "job_slot_still_available", nullable = false, updatable = false)
    private boolean jobSlotStillAvailable;

    /**
     * Fine effettiva del tirocinio (US-1009), per la relazione finale: obbligatoria per i report nuovi, assente in quelli
     * inviati prima. La scheda del tirocinio resta con le sue date.
     */
    @Column(name = "actual_end_date", updatable = false)
    private LocalDate actualEndDate;

    /**
     * Motivo di chiusura (US-1103): un'interruzione se la fine effettiva precede quella prevista, altrimenti la chiusura
     * regolare. Assente nei report inviati prima.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "closure_reason_id", updatable = false)
    private ClosureReason closureReason;

    /** Descrizione dell'interruzione anticipata (PO, 16/11): solo con un'interruzione; mai nei log né nell'audit. */
    @Column(name = "interruption_details", updatable = false, length = 2000)
    private String interruptionDetails;

    /**
     * Azienda del tirocinio al momento della chiusura (US-1104): la valutazione resta sua. Assente nei report inviati
     * prima.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", updatable = false)
    private Company company;

    /** Valutazione dell'azienda (US-1104): tre voti da 1 a 5; assenti nei report inviati prima. */
    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(name = "company_interaction", updatable = false)
    private Integer companyInteraction;

    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(name = "company_welcome", updatable = false)
    private Integer companyWelcome;

    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(name = "company_training", updatable = false)
    private Integer companyTraining;

    /** Nota della valutazione, facoltativa e senza dati personali: si vede nella scheda dell'azienda, non nell'audit. */
    @Column(name = "company_review_note", updatable = false, length = 500)
    private String companyReviewNote;

    /** Il Tutor propone l'azienda come nave scuola (US-515); decide poi l'operatore. Falso nei report inviati prima. */
    @Column(name = "training_ship_proposed", nullable = false, updatable = false)
    private boolean trainingShipProposed;

    /**
     * Valutazione dell'azienda del tirocinio (US-1104): tre voti da 1 a 5, la nota facoltativa e la proposta di nave
     * scuola (US-515).
     */
    public record CompanyReview(Company company, int interaction, int welcome, int training, String note,
            boolean trainingShipProposed) {
    }

    public ClosureReport(Ticket ticket, LocalDate actualEndDate, ClosureReason closureReason, String interruptionDetails,
            short companySatisfaction, short beneficiarySatisfaction, String objectives, String difficulties,
            HiringFormula hiringFormula, NoHireReason noHireReason, boolean jobSlotStillAvailable,
            CompanyReview review) {
        this.ticket = ticket;
        this.actualEndDate = actualEndDate;
        this.closureReason = closureReason;
        this.interruptionDetails = interruptionDetails;
        this.companySatisfaction = companySatisfaction;
        this.beneficiarySatisfaction = beneficiarySatisfaction;
        this.objectives = objectives;
        this.difficulties = difficulties;
        this.hired = hiringFormula != null;
        this.hiringFormula = hiringFormula;
        this.noHireReason = noHireReason;
        this.jobSlotStillAvailable = jobSlotStillAvailable;
        if (review != null) {
            this.company = review.company();
            this.companyInteraction = review.interaction();
            this.companyWelcome = review.welcome();
            this.companyTraining = review.training();
            this.companyReviewNote = review.note();
            this.trainingShipProposed = review.trainingShipProposed();
        }
    }

    /** Media dei tre voti della valutazione (US-1104); {@code null} nei report senza valutazione. */
    public Double companyReviewAverage() {
        return companyInteraction == null ? null
                : (companyInteraction + companyWelcome + companyTraining) / 3.0;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) {
            return false;
        }
        return id != null && id.equals(((ClosureReport) other).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "ClosureReport[id=" + id + "]";
    }
}
