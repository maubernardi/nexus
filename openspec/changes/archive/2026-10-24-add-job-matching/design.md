## Context

Motivazione: proposal.md (US-601, US-602). Disponibili: motore delle transizioni con effetti (US-402), audit (EN-2),
mansioni con `active` e indice per la ricerca (US-502), `ticket_company_blacklist`, `company_audit_event`.

## Decisions

### D1 — Ricerca nel database
Una query JPQL con fetch join e `not exists` sulla blacklist; filtri facoltativi (null = tutte). I default (zona del
beneficiario, mansione richiesta) li applica l'interfaccia, così l'API resta semplice e l'operatore vede sempre quali
filtri sono attivi.

### D2 — Abbinamento come transizione con effetti
`MATCH` usa `apply(…, effects)`: dopo i controlli del motore l'effetto verifica operatore, versione e disponibilità della
mansione, esclusione dell'azienda, poi blocca la mansione con `saveAndFlush`. La protezione dalle gare è doppia:
versione inviata dal client e `@Version` + vincolo `uq_job_slot_blocked_by_ticket` nel database (`409`).

### D3 — Chi può abbinare
L'operatore assegnato; un altro operatore riceve `409` ("in carico a un collega"); una segnalazione non presa in carico
va prima presa dalla coda. L'ADMIN può sempre intervenire.

### D4 — Tracciamento
Tre registri con scopi diversi: audit generale (ticket `MATCH`, mansione `BLOCK`), cronologia degli stati del ticket,
audit trail dell'azienda (`PROPOSTA_INVIATA`, consultabile con US-503).

### D5 — Interfaccia
Conferma esplicita prima della proposta (blocca una posizione reale): pannello con titolo focalizzato e testo che dice
chi, cosa e dove. Dopo l'esito il focus va al messaggio; in caso di conflitto l'elenco si riaggiorna. Pulsanti con nome
accessibile univoco ("Proponi Magazziniere presso Alfa Logistica").
