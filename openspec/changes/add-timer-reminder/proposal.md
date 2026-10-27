## Why

Storia **US-704** (Sprint 5): accettata la proposta, il Tutor ha 7 giorni lavorativi per registrare l'appuntamento;
al **4° giorno lavorativo**, se non l'ha ancora fatto, riceve **un solo promemoria**. Planning dello Sprint 5: il
controllo gira **ogni ora**.

## What Changes

- Prima **attività pianificata** del backend: `TimerJob` (`@Scheduled`, ora di Roma, espressione in
  `nexus.timer.cron`, predefinita `0 5 * * * *`, cioè ogni ora; disattivata nei test con `"-"`).
- `TimerService.sendDueReminders(adesso)`: per le segnalazioni in `PROPOSTA_ACCOLTA` con timer attivo e promemoria non
  ancora inviato, arrivato il 4° giorno lavorativo dall'accettazione (calendario unico, US-703): segna
  `timer_reminder_sent_at`, registra `TIMER_REMINDER` nell'audit (autore "Sistema") e notifica il Tutor
  (`TIMER_REMINDER`). Segnare l'invio nella stessa transazione rende il promemoria **unico** anche se il controllo
  gira più volte.
- Le notifiche funzionano anche per le azioni del sistema (senza utente collegato).
- Testi: notifica del promemoria e voce "Promemoria del timer" in cronologia.

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `ticket-lifecycle`: promemoria del timer.
