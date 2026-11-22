package it.nexus.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.nexus.domain.MessageText;
import it.nexus.domain.enumeration.MessageKey;
import it.nexus.domain.enumeration.MessagePart;

@Repository
public interface MessageTextRepository extends JpaRepository<MessageText, Long> {

    Optional<MessageText> findByMessageAndPart(MessageKey message, MessagePart part);
}
