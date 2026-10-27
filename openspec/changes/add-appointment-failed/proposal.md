## Why

Storia **US-802** (Sprint 5): se l'appuntamento con l'azienda non va a buon fine, il Tutor lo registra con il motivo e
la segnalazione torna al Call Center per una nuova ricerca. Decisioni della review dello Sprint 4 (§8.2): la
segnalazione riaperta resta allo stesso operatore, in cima a "In carico a me"; nessuna esclusione automatica
dell'azienda; il Tutor indica una causa da un menu e una nota.

## What Changes

- Transizione `APPOINTMENT_FAILED`: `APPUNTAMENTO` → `RIAPERTO`, solo per il **Tutor della segnalazione**
  (`POST /api/v1/tickets/{id}/appointment-failed`): causa obbligatoria (`AZIENDA_NON_CONFERMA`,
  `BENEFICIARIO_RINUNCIA`, `BENEFICIARIO_ASSENTE`, `ALTRO`; voci provvisorie) e nota, obbligatoria con "Altro".
- Effetti: la mansione torna **libera** (audit `RELEASE`) e la segnalazione non ha più una proposta in corso (resta in
  cronologia); **nessuna esclusione automatica** dell'azienda; cronologia "Appuntamento non andato a buon fine – Causa:
  …" con la nota; evento `APPUNTAMENTO_FALLITO` con la causa nello storico dell'azienda (conta tra gli appuntamenti
  falliti); notifica `APPOINTMENT_FAILED` all'operatore.
- **Segnalazioni riaperte**: restano in carico allo stesso operatore, **in cima** a "In carico a me" con lo stato
  "Riaperto" evidenziato in testo; si **abbinano di nuovo** (la proposta all'azienda parte anche da `RIAPERTO`).

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `ticket-lifecycle`: appuntamento non andato a buon fine e segnalazioni riaperte.
- `ticket-list`: le riaperte in cima a "In carico a me".
