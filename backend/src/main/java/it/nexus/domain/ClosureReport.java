package it.nexus.domain;

import java.io.Serial;

import org.hibernate.Hibernate;
import org.hibernate.annotations.Immutable;

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

    public ClosureReport(Ticket ticket, short companySatisfaction, short beneficiarySatisfaction, String objectives,
            String difficulties, HiringFormula hiringFormula, NoHireReason noHireReason,
            boolean jobSlotStillAvailable) {
        this.ticket = ticket;
        this.companySatisfaction = companySatisfaction;
        this.beneficiarySatisfaction = beneficiarySatisfaction;
        this.objectives = objectives;
        this.difficulties = difficulties;
        this.hired = hiringFormula != null;
        this.hiringFormula = hiringFormula;
        this.noHireReason = noHireReason;
        this.jobSlotStillAvailable = jobSlotStillAvailable;
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
