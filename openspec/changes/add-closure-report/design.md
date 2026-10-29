## Decisions

### D1 — Quando si invia
Il Tutor può inviare il report in qualunque momento del tirocinio, non solo dopo la data di fine prevista: un
tirocinio può interrompersi prima. È la scelta che non limita il Tutor (azione A14 della retrospettiva).

### D2 — Un solo invio, senza bozza
Il modulo è breve e il report non si modifica dopo l'invio (criterio della storia): un unico invio, preceduto da una
conferma che ricorda che il report non si potrà più cambiare. Niente bozza da salvare.

### D3 — Campi obbligatori
Obbligatori: le due soddisfazioni, assunto sì/no con formula o motivo, mansione ancora disponibile. Obiettivi e
difficoltà sono facoltativi (fino a 2000 caratteri), come le note della scheda. Formula e motivo si scelgono tra le voci
attive; il report conserva la voce anche se l'ADMIN la disattiva in seguito.

### D4 — Una tabella in sola aggiunta
`closure_report` ha una riga per segnalazione (`UNIQUE`) e i vincoli nel database:
- soddisfazioni da 1 a 5;
- formula solo se assunto, motivo solo se non assunto;
- testi non vuoti.

UPDATE, DELETE e TRUNCATE sono rifiutati da trigger, come per `company_note`; l'entità è `@Immutable`. I testi liberi
possono contenere dati personali: non vanno nell'audit né nei log. Quando arriverà l'anonimizzazione (US-1303),
dovrà trattare questi testi come le note dell'azienda.

### D5 — Il report nel dettaglio della segnalazione
Il report inviato arriva nel dettaglio della segnalazione (`closureReport`, assente finché non c'è), con la stessa
visibilità: Tutor della segnalazione, colleghi di progetto, Call Center e ADMIN. Nessun endpoint di lettura in più.

### D6 — Mansione e storico dell'azienda in US-1102
Questa storia registra solo la risposta "mansione ancora disponibile". La mansione che torna libera e l'evento
nello storico dell'azienda sono di US-1102, che usa la stessa transizione.
