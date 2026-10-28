## Decisions

### D1 — Giorni solari, istante esatto
"7 giorni solari" vuol dire sette giorni di calendario, con sabati e festivi compresi. Si contano dall'istante della
pubblicazione: `publishedAt + 7 giorni`, come nel brief (`currentDate >= publishedAt + 7 giorni`). Il calendario
lavorativo (US-703) qui non si applica.

### D2 — Il server decide, l'interfaccia informa
L'entità calcola `makePublicFrom` e il DTO lo espone. Il frontend lo usa solo per mostrare il comando o la data, mentre
il servizio ricontrolla con l'orologio dell'applicazione. Se il browser ha l'ora sbagliata, il server risponde `409`
con la data.

### D3 — Il vincolo nel database
`ck_board_post_made_public` (V6) impone che un annuncio con `made_public_at` non abbia progetto: lo sblocco toglie il
progetto e registra la data nella stessa scrittura.
