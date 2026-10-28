## Context

Tabella `board_post` (V6) con i vincoli già nel database: un solo annuncio attivo per mansione
(`uq_board_post_active_job_slot`), data obbligatoria se pubblicato (`ck_board_post_published`), fasce di età, ore e
durata. Dati demo: una bozza (nata da un rilancio) e due annunci pubblicati, uno riservato al progetto GOL.

## Decisions

### D1 — Il progetto si sceglie solo nella bozza
Una volta pubblicato, un annuncio riservato diventa visibile a tutti solo con lo sblocco dopo 7 giorni (US-903). Se il
progetto restasse modificabile, la regola si aggirerebbe con una semplice modifica. Nella bozza il Call Center può
invece scegliere "Tutti i progetti" oppure un progetto attivo.

### D2 — Modifiche anche dopo la pubblicazione
Titolo, età, ore, durata e note si correggono anche su un annuncio pubblicato: la data di pubblicazione non cambia.
L'annuncio archiviato è in sola lettura (`409`).

### D3 — Pubblicare solo ciò che si può proporre
La pubblicazione richiede la mansione libera e a disposizione e l'azienda attiva. Altrimenti risponde `409` con il
motivo: il Tutor non deve vedere annunci per mansioni non proponibili.

### D4 — Audit
`UPDATE` registra solo i campi cambiati (e niente se non cambia nulla); `PUBLISH` lo stato e la data; `ARCHIVE` lo
stato precedente. Autore e istante li aggiunge il registro (EN-2).

### D5 — Fuori da questa storia
- La creazione degli annunci arriva con US-904 (da una mansione libera) e con US-803 (rilancio).
- La vista del Tutor arriva con US-901.
- Lo sblocco dopo 7 giorni arriva con US-903.
- Resta aperto un caso: un annuncio pubblicato la cui mansione viene poi bloccata da un abbinamento normale. Va deciso
  con il PO prima di US-901.
