package it.nexus.services;

import java.time.Instant;

/** Timer delle proposte accettate (brief, fase 5): promemoria al 4° giorno lavorativo (US-704). */
public interface TimerService {

    /**
     * Invia il promemoria al Tutor delle proposte accettate arrivate al 4° giorno lavorativo; una sola volta per timer
     * anche se il controllo gira più volte (idempotenza).
     *
     * @return numero di promemoria inviati
     */
    int sendDueReminders(Instant now);
}
