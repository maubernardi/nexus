## Context

Motivazione: proposal.md (US-402). `TicketStateMachine` (EN-1) e audit (EN-2) già presenti.

## Decisions

### D1 — Effetti della transizione nel motore
`apply(ticket, transition, expectedVersion, note, effects)`: i controlli (ruolo, versione, stato) vengono prima; poi
`effects` modifica il ticket e restituisce le `AuditChanges` da unire al cambio di stato. Così il servizio non tocca il
ticket prima dei controlli e l'audit ha un unico evento completo.

### D2 — Già assegnata
Una speciale in `IN_LAVORAZIONE` può essere presa solo se non è assegnata: l'effetto controlla `assignedCcOperator` e
risponde `409` ("già presa in carico"). La gara tra due operatori sulla stessa versione è coperta dal controllo di
versione e dal blocco ottimistico (`409`).

### D3 — Interfaccia
Il pulsante ha un nome accessibile univoco per riga ("Prendi in carico la segnalazione n. N", testo nascosto dopo uno
spazio come nodo a sé). Dopo l'azione la riga sparisce: il focus va al messaggio di esito (con link alle lavorazioni o
spiegazione del conflitto). La regione scorrevole della tabella è `relative`, così i testi nascosti non allargano la
pagina.
