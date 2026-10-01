## Context

Motivazione: proposal.md (EN-1, US-301). Tabelle `ticket` (con `version`, numero da sequenza) e
`ticket_status_history` esistono già (change `add-domain-model`). Dipende da `add-beneficiary-registration` (US-201:
beneficiari, `CurrentAppUserService`, `TsidMapper`, form accessibili).

## Decisions

### D1 — Catalogo dichiarativo delle transizioni
`TicketTransition` è un record `(name, from, to, roles)`; le costanti stanno in `TicketTransitions`, e ogni storia aggiunge
le proprie (qui solo `SUBMIT`: creazione → `NUOVA`, ruolo `TUTOR`). Una transizione con `from` vuoto è di **creazione**.
Alternativa scartata: una tabella completa stato×evento già ora — codificherebbe regole di storie non ancora discusse
(Q1, Q2, Q3 aperte).

### D2 — Un solo servizio applica le transizioni
`TicketStateMachine`:
- `create(ticket, transition, note)`: imposta lo stato iniziale, salva e scrive la cronologia (`from_status` nullo);
- `apply(ticket, transition, expectedVersion, note)`: verifica nell'ordine ruolo (`403`), versione attesa (`409`, "il
  ticket è stato modificato nel frattempo"), stato di partenza (`409`, transizione non ammessa); poi cambia stato con
  `saveAndFlush` e scrive la cronologia. Autore e istante della cronologia sono i campi di auditing (`created_by/at`).
Il setter dello stato resta sull'entità per il mapping, ma per convenzione solo il motore lo chiama (Javadoc).

### D3 — Concorrenza
Doppia protezione: il client invia la versione che ha visto (`expectedVersion`) e il `@Version` di JPA rileva le gare tra
transazioni. `ObjectOptimisticLockingFailureException` è tradotta dal gestore globale in `409` con un messaggio che invita
a ricaricare. Nessun lock pessimistico: i conflitti sono rari e l'esito corretto è far rileggere all'utente.

### D4 — API di invio
`POST /api/v1/tickets` con `TicketCreateDTO(beneficiaryId, projectId, jobCategoryId)` → `201` + `TicketDTO` (id, numero,
tipo, stato, fast-track, beneficiario in sintesi, progetto, mansione richiesta, creazione). Nessun header `Location`: il
dettaglio del ticket arriva con US-305. Controlli nel service, come `400` sul campo: beneficiario inesistente o di un altro
tutor (`beneficiaryId`), progetto non assegnato o disattivato (`projectId`), tipologia inesistente o disattivata
(`jobCategoryId`). Il messaggio non distingue "inesistente" da "non tuo".

### D5 — Progetti dell'utente
`GET /api/v1/me/projects` → `List<ReferenceItemDTO>` dei progetti attivi assegnati, ordinati per nome (query su
`user_project`). Serve al modulo e in futuro ai filtri.

### D6 — Frontend
Pagina `/segnalazioni/nuova` (solo TUTOR) con gli stessi componenti di form accessibili di US-201 (`FormField`,
`NativeSelect`, `ErrorSummary` con focus). Parametro `?beneficiario=<id>` per preselezionare il beneficiario. Senza beneficiari o
senza progetti la pagina lo spiega e offre il collegamento utile invece di un modulo inutilizzabile. Alla conferma il
focus va al titolo "Segnalazione n. N inviata", con lo stato in testo.

## Risks / Trade-offs

- Il catalogo in codice richiede un rilascio per cambiare le regole: accettabile, le regole sono del brief e vanno testate.
- `expectedVersion` è un dettaglio che il client deve portarsi dietro: lo espongono già i DTO dei ticket.
