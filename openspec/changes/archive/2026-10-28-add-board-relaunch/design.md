## Decisions

### D1 — La bozza nasce sempre
Finché l'appuntamento è in corso la mansione resta bloccata dalla segnalazione. Quindi non si può ritirare, l'azienda
non si può disattivare, e l'annuncio precedente era già stato archiviato all'abbinamento. Appena liberata, la mansione
è sempre proponibile. Il controllo resta comunque (`requireProposable` e un solo annuncio attivo): se un'invariante
saltasse, l'intera operazione fallirebbe con `409` invece di lasciare dati incoerenti.

### D2 — Due notifiche distinte
L'operatore riceve sia l'esito (`APPOINTMENT_FAILED`) sia la richiesta di rilancio (`BOARD_RELAUNCH_REQUESTED`): sono
due azioni diverse, la nuova ricerca per la segnalazione e la bozza da pubblicare. Il link porta alla segnalazione, che
mostra il link alla bozza.

### D3 — Il Tutor non apre la gestione
Il Tutor vede numero e stato dell'annuncio, ma non il link: la scheda di gestione con l'azienda resta di Call Center e
ADMIN. Quando l'annuncio è pubblicato, il Tutor lo trova in bacheca come gli altri.

### D4 — Bozza del rilancio e nuova proposta
Se l'operatore propone di nuovo la stessa mansione, la bozza si archivia da sola: un annuncio vive solo finché la sua
mansione è disponibile (decisione del PO, 28/10).
