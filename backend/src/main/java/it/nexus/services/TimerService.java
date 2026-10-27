package it.nexus.services;

import java.time.Instant;

/**
 * Timer delle proposte accettate (brief, fase 5): promemoria al 4° giorno lavorativo (US-704) e scadenza alla fine del
 * 7° (US-705).
 */
public interface TimerService {

    /**
     * Invia il promemoria al Tutor delle proposte accettate arrivate al 4° giorno lavorativo; una sola volta per timer
     * anche se il controllo gira più volte (idempotenza).
     *
     * @return numero di promemoria inviati
     */
    int sendDueReminders(Instant now);

    /**
     * Riapre le proposte accettate con il timer scaduto: stato RIAPERTO in carico allo stesso operatore, timer azzerato,
     * mansione di nuovo libera; notifica al Tutor e all'operatore.
     *
     * @return numero di segnalazioni riaperte
     */
    int expireDue(Instant now);
}
