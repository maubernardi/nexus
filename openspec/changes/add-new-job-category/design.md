## Decisions

### D1 — Stessa segnalazione, due esiti
Approvare o respingere portano entrambi la segnalazione in `NUOVA` (in coda): cambia solo la tipologia richiesta. Il
testo proposto resta nell'audit (prima/dopo) e in cronologia.

### D2 — Codice della tipologia
Il catalogo ha un codice (`^[A-Z0-9_]+$`, fino a 30 caratteri): si ricava dal nome senza accenti
("Tecnico del suono" → `TECNICO_DEL_SUONO`), con un suffisso numerico se già usato. Il nome deve essere nuovo
(confronto senza maiuscole): se esiste, l'operatore usa la tipologia del catalogo.

### D3 — Avviso provvisorio al Tutor
Finché non ci sono le notifiche (US-1201), il cambio di tipologia accende un segno sulla segnalazione
(`category_change_unseen`), mostrato solo al Tutor della segnalazione e spento quando la apre
(`POST /tickets/{id}/category-change-seen`). Con US-1201 lo stesso evento produrrà anche notifica in app ed email.

### D4 — Rinomina dello stato
`IN_ATTESA_APPROVAZIONE_ADMIN` non è più solo dell'ADMIN: la migrazione aggiorna dati e vincoli; il codice legge il
vecchio nome dagli eventi di audit immutabili (`TicketStatus.fromStored`). Il brief originale resta invariato come
documento storico.
