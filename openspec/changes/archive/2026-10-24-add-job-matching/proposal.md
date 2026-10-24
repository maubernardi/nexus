## Why

Storie **US-601** (ricerca delle mansioni compatibili) e **US-602** (abbinamento e proposta), Sprint 2: completano il
flusso del rilascio R1. L'operatore che ha in carico una segnalazione trova le mansioni libere adatte al beneficiario e
lo propone all'azienda; la mansione si blocca, così nessun altro la propone in parallelo.

## What Changes

- **Ricerca (US-601)**: `GET /api/v1/tickets/{id}/compatible-job-slots?zoneId=&jobCategoryId=` — solo mansioni LIBERE,
  a disposizione, di aziende attive e **non escluse** per quella segnalazione; per azienda e titolo. Nell'interfaccia
  i filtri partono dalla zona di residenza del beneficiario e dalla mansione richiesta e si possono allargare (decisione
  del planning).
- **Abbinamento (US-602)**: transizione `MATCH` (Call Center, ADMIN) da `IN_LAVORAZIONE` a `PROPOSTA_AZIENDA` con
  `POST /api/v1/tickets/{id}/match` (mansione + versioni viste di ticket e mansione). Effetti: mansione BLOCCATA dal
  ticket, ticket collegato alla mansione. Mansione non più libera o cambiata → `409` con messaggio chiaro.
- Solo l'operatore che ha in carico la segnalazione può abbinarla (l'ADMIN sempre).
- Tracciamento: evento `MATCH` sul ticket (stato, mansione, azienda), `BLOCK` sulla mansione, `PROPOSTA_INVIATA`
  nell'audit trail dell'azienda, cronologia degli stati.
- `GET /api/v1/tickets/{id}/work`: dettaglio per la lavorazione.
- Interfaccia: pagina **Lavorazione segnalazione** (`/lavorazioni/:id`) con dati della segnalazione, ricerca con filtri,
  proposta con conferma, esito focalizzato e riepilogo della proposta. Link dalle "Mie lavorazioni" e dall'esito della
  presa in carico.

## Capabilities

### New Capabilities
- `job-matching`: ricerca delle mansioni compatibili e abbinamento.

### Modified Capabilities
<!-- nessuna -->

## Non-goals

- Esclusione manuale di un'azienda (US-603), valutazione della proposta da parte del Tutor e timer (E7), abbinamento da
  `RIAPERTO` (arriverà con le storie dell'appuntamento fallito), approvazione del fast-track (US-403).
