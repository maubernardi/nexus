## Why

Storia **US-504** (Sprint 3), decisione del PO nella review dello Sprint 2 (§7.3): un'azienda con una mansione bloccata
da una proposta in corso non deve sparire dalle ricerche perché qualcuno l'ha disattivata.

## What Changes

- La disattivazione di un'azienda è rifiutata (`409`) finché ha mansioni BLOCCATE; il messaggio elenca mansione e numero
  della segnalazione che la blocca. La riattivazione non cambia.
- La scheda azienda mostra il motivo del rifiuto inviato dal server (prima un 409 era sempre "modificata da un collega").

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `company-registry`: disattivazione con mansioni bloccate.
