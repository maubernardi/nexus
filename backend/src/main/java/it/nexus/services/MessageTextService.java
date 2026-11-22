package it.nexus.services;

import java.util.List;

import it.nexus.domain.dto.MessageTextDTO;
import it.nexus.services.messages.MessageTexts;

/**
 * Testi dei messaggi di NEXUS (US-1204, PO 22/11): per ogni notifica il testo nell'app, la parte distintiva
 * dell'oggetto e il testo dell'email; per le email del cambio di indirizzo oggetto e testo. L'ADMIN li cambia e li
 * ripristina; finché non li cambia valgono i predefiniti.
 */
public interface MessageTextService {

    /** I testi di adesso, per comporre i messaggi. */
    MessageTexts current();

    /** Tutti i messaggi con le loro parti, per la pagina dell'ADMIN. */
    List<MessageTextDTO> list();

    /** Cambia il testo di una parte; uguale al predefinito equivale a ripristinarlo. */
    MessageTextDTO update(String key, String part, String text);

    /** Torna al testo predefinito della parte. */
    MessageTextDTO reset(String key, String part);
}
