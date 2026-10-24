package it.nexus.repository;

import java.util.Collection;

import org.springframework.data.jpa.domain.Specification;

import it.nexus.domain.Ticket;
import it.nexus.domain.enumeration.TicketStatus;
import jakarta.persistence.criteria.JoinType;

/** Filtri combinabili dell'elenco delle segnalazioni (US-303). */
public final class TicketSpecifications {

    private TicketSpecifications() {
    }

    /** Carica in un'unica query i dati mostrati nell'elenco (niente N+1). */
    public static Specification<Ticket> fetchListData() {
        return (root, query, cb) -> {
            if (query != null && Ticket.class.equals(query.getResultType())) {
                root.fetch("beneficiary").fetch("residenceZone");
                root.fetch("project");
                root.fetch("tutor");
                root.fetch("requestedJobCategory", JoinType.LEFT);
                root.fetch("assignedCcOperator", JoinType.LEFT);
            }
            return cb.conjunction();
        };
    }

    public static Specification<Ticket> tutor(Long tutorId) {
        return (root, query, cb) -> cb.equal(root.get("tutor").get("id"), tutorId);
    }

    public static Specification<Ticket> projectIn(Collection<Long> projectIds) {
        return (root, query, cb) -> projectIds.isEmpty() ? cb.disjunction() : root.get("project").get("id").in(projectIds);
    }

    public static Specification<Ticket> project(Long projectId) {
        return (root, query, cb) -> projectId == null ? cb.conjunction() : cb.equal(root.get("project").get("id"), projectId);
    }

    public static Specification<Ticket> status(TicketStatus status) {
        return (root, query, cb) -> status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }

    public static Specification<Ticket> statusIn(Collection<TicketStatus> statuses) {
        return (root, query, cb) -> root.get("status").in(statuses);
    }

    public static Specification<Ticket> open() {
        return (root, query, cb) -> cb.notEqual(root.get("status"), TicketStatus.CLOSED);
    }

    public static Specification<Ticket> unassigned() {
        return (root, query, cb) -> cb.isNull(root.get("assignedCcOperator"));
    }

    public static Specification<Ticket> assignedTo(Long operatorId) {
        return (root, query, cb) -> cb.equal(root.get("assignedCcOperator").get("id"), operatorId);
    }
}
