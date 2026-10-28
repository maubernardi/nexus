## Why

Storia **US-307** (Sprint 6), dalla nota operativa del PO discussa nella review dello Sprint 5 (§8.3): la Home del
Tutor deve mostrare il timer del passo in corso, quando c'è una scadenza. Oggi la scadenza dei 7 giorni lavorativi si
vede solo nel dettaglio della segnalazione.

## What Changes

- Riga dell'elenco (`TicketListItemDTO`): `timerDeadline` (ultimo giorno utile, `aaaa-mm-gg`, null senza timer attivo)
  e `timerDueSoon` (promemoria del 4° giorno già inviato).
- Viste del Tutor "Le mie" e "Del mio progetto": prima le segnalazioni con la scadenza più vicina, poi le altre dalla
  più recente.
- Tabella delle segnalazioni: colonna "Scadenza" nelle viste del Tutor ("entro mer 04/11"); dopo il promemoria la
  scadenza è in grassetto con la scritta "in scadenza", non affidata al solo colore.

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `ticket-list`: scadenza nella Home del Tutor.
