package it.nexus.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import it.nexus.domain.ContactRequestAttempt;

/** Tentativi di contatto (US-512): solo inserimento e lettura, non si modificano. */
@org.springframework.stereotype.Repository
public interface ContactRequestAttemptRepository extends Repository<ContactRequestAttempt, Long> {

    ContactRequestAttempt save(ContactRequestAttempt attempt);

    /** Tentativi della richiesta, dal più recente. */
    List<ContactRequestAttempt> findByContactRequestIdOrderByDateDescCreatedAtDescIdDesc(Long contactRequestId);
}
