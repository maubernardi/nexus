## Why

Storia **EN-2** (Sprint 2), decisione del PO del 01/10: ogni cambio di stato e ogni scelta deve essere auditabile. Oggi
esistono la cronologia degli stati del ticket e l'audit trail dell'azienda, ma non un registro generale di chi ha fatto
cosa, su quale oggetto, con quali valori e per quale motivo.

## What Changes

- Tabella `audit_event` **in sola aggiunta** (UPDATE, DELETE e TRUNCATE rifiutati da trigger): autore, istante, tipo e id
  dell'oggetto, azione, modifiche (`changes`, JSON) e motivo.
- `AuditService.record(...)`: chiamato dentro la transazione dell'operazione (se l'operazione fallisce, l'evento non
  resta; senza transazione l'invocazione è un errore di programmazione).
- Dati personali (GDPR): per i beneficiari si registrano i **nomi** dei campi interessati, mai i valori; per ticket,
  aziende, mansioni e configurazioni i valori prima/dopo completi.
- Collegati da subito: ogni transizione del ticket (compresa la creazione, con le scelte di beneficiario, progetto e
  mansione richiesta) e la registrazione del beneficiario. Le storie successive registrano le proprie azioni (regola della
  Definition of Done).

## Capabilities

### New Capabilities
- `audit-log`: registro immodificabile di cambi di stato e scelte.

### Modified Capabilities
<!-- nessuna -->

## Non-goals

- Consultazione dell'audit da parte dell'ADMIN con filtri (rilascio R4, con la gestione utenti); la cronologia del
  ticket (US-305) ne mostrerà la parte che riguarda il ticket.
- Sostituire `ticket_status_history` o `company_audit_event`: restano, con il proprio scopo.
