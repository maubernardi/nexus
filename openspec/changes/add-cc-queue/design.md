## Context

Motivazione: proposal.md (US-401). Dipende da `add-ticket-submission` (ticket in `NUOVA`, `TicketMapper`). L'indice
`ix_ticket_queue (status, is_fast_track DESC, created_at)` esiste già (`add-domain-model`).

## Decisions

### D1 — Cosa c'è in coda
Stato `NUOVA` o `IN_LAVORAZIONE` **e** `assigned_cc_operator_id` nullo. `IN_LAVORAZIONE` non assegnato è il caso della
segnalazione speciale appena creata (US-306) o declassata (US-404). Ordine: `is_fast_track DESC, created_at ASC, number
ASC` (il numero rende l'ordine stabile a parità di istante).

### D2 — API
`GET /api/v1/tickets/queue?projectId=&zoneId=` → `List<QueueItemDTO>` (id, numero, tipo, stato, fast-track, versione,
beneficiario in sintesi, progetto, zona, mansione richiesta, tutor, istante di arrivo). Id di filtro non validi → `400`.
JPQL con fetch join (nessun N+1) e filtri facoltativi. La zona è quella di residenza del beneficiario: la zona della
mansione esiste solo dopo l'abbinamento (fase 4). Il Call Center ha visibilità globale (brief, RBAC): vede i nomi.

### D3 — Tabella accessibile
`<table>` nativa con `<caption>`, intestazioni `<th scope="col">`, numero di riga come `<th scope="row">`. Contenitore
con scorrimento orizzontale proprio, focalizzabile e con nome (`role="region"`, `tabIndex=0`): consentito da WCAG 1.4.10
per le tabelle dati, e la pagina non scorre mai orizzontalmente. Il fast-track è un badge con testo e icona, mai solo
colore (1.4.1). Date in formato italiano con `<time dateTime>`.

### D4 — Filtri
Due `select` nativi (progetto, zona) in un `fieldset`. Il cambio aggiorna subito l'elenco (non è un cambio di contesto,
3.2.2) e una regione `role="status"` annuncia "N segnalazioni in coda". I filtri stanno nei parametri dell'indirizzo
(`?progetto=&zona=`), così Indietro e i link condivisi funzionano; pulsante "Azzera filtri" quando ce n'è almeno uno.

### D5 — Aggiornamento
TanStack Query con `refetchOnWindowFocus` e `refetchInterval` di 60 s: la coda cambia mentre l'operatore lavora.
Nessuna cache offline delle API (regola PWA del progetto).
