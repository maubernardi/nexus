## Decisions

### D1 — Tutto nella transizione `CLOSE`
Il rilascio della mansione avviene nella stessa transizione del report (US-1101), dopo i controlli di versione e
stato: o tutto o niente. L'evento nello storico dell'azienda si registra subito dopo, come `TIROCINIO_AVVIATO` per
l'avvio (US-1003).

### D2 — Mansione non più disponibile: resta bloccata
`job_slot` ha solo `LIBERA` e `BLOCCATA` (`ck_job_slot_blocked`). Se l'azienda non offre più la mansione, questa
resta `BLOCCATA` dalla segnalazione conclusa: non torna tra le compatibili. Non la ritiriamo in automatico
(`active = false`): la scelta resta al Call Center, che la gestisce dalla scheda dell'azienda come oggi.

### D3 — La segnalazione conclusa resta collegata alla mansione
A differenza dell'appuntamento fallito (US-802), la segnalazione non perde il collegamento alla mansione: è il
tirocinio che si è svolto. Il collegamento non blocca nulla, perché il blocco è su `job_slot.blocked_by_ticket_id`.

### D4 — Dettagli dell'evento
Le chiavi seguono quelle già in uso nello storico (`assunto`, `jobSlotTitle`, `ticketNumber`), con in più `formula` o
`motivo` (il nome della voce al momento dell'invio) e `mansioneDisponibile`. Gli eventi registrati prima, con il solo
`assunto`, continuano a leggersi «Con assunzione» o «Senza assunzione».
