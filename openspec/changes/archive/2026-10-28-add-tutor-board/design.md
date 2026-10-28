## Decisions

### D1 — Un DTO a parte, non un filtro sui campi
Il Tutor riceve `TutorBoardPostDTO`, che i campi riservati non li ha proprio: aggiungere un campo a `BoardPostDTO` non
può farlo arrivare al Tutor per errore. Il test controlla che la risposta non contenga il nome dell'azienda né il suo
id, nemmeno quando titolo e note lo citano.

### D2 — Visibilità nella query
Pubblicati, `project` nullo o tra i progetti del Tutor (`user_project`). Un Tutor senza progetti vede solo quelli per
tutti. Bozze e archiviati non escono mai.

### D3 — Archiviazione automatica in un punto solo
`BoardPostService.archiveForUnavailableSlots` gira nella stessa transazione di abbinamento, ritiro e disattivazione:
se l'operazione fallisce, l'annuncio resta com'era. Autore è l'operatore che ha reso indisponibile la mansione; il
motivo spiega perché. La stessa via servirà all'approvazione del fast-track (US-403), che abbina la mansione
dell'annuncio.

### D4 — Stessa pagina, contenuto per ruolo
`/bacheca` resta un solo indirizzo nel menu di tutti; il Tutor vede le schede, Call Center e ADMIN la gestione. La
scheda di gestione `/bacheca/:id` resta riservata a Call Center e ADMIN.
