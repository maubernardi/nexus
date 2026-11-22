package it.nexus.domain.dto;

import it.nexus.domain.enumeration.MessagePart;

/**
 * Una parte di un messaggio (US-1204): il testo in uso e il predefinito.
 *
 * @param custom vero se l'ADMIN l'ha cambiato: si può ripristinare il predefinito
 */
public record MessageTextPartDTO(MessagePart part, String text, String defaultText, boolean custom) {
}
