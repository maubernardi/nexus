## Decisions

### D1 — Si parte dalla mansione, nella scheda dell'azienda
L'operatore vede lì le mansioni libere e il loro stato: creare l'annuncio da lì evita un selettore di mansioni nella
bacheca. La bozza nasce con il titolo della mansione e senza progetto, cioè visibile a tutti. Il progetto si può
scegliere nella bozza (US-902, D1).

### D2 — Un solo annuncio attivo per mansione
Il vincolo `uq_board_post_active_job_slot` è nel database dalla V6. Il servizio controlla prima di inserire, per
rispondere con un messaggio chiaro: "ha già l'annuncio #N". Archiviato l'annuncio, se ne può creare un altro.

### D3 — Stesse condizioni della pubblicazione
Creare una bozza per una mansione bloccata o ritirata non servirebbe: non si potrebbe pubblicare. Il controllo è unico
(`requireProposable`) e vale sia per la creazione sia per la pubblicazione.
