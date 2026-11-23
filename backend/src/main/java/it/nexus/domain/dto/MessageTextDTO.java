package it.nexus.domain.dto;

import java.util.List;

import it.nexus.domain.enumeration.MessageKey;

/**
 * Un messaggio con i testi modificabili dall'ADMIN (US-1204): nome, se è una notifica, segnaposto ammessi e parti.
 */
public record MessageTextDTO(MessageKey key, boolean notification, List<String> placeholders,
        List<MessageTextPartDTO> parts) {
}
