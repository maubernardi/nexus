## Decisions

### D1 — Stessa data del dettaglio
`timerDeadline` è calcolato come nel dettaglio (`TicketDetailDTO.timerDeadline`): la scadenza è la mezzanotte di Roma
dopo il 7° giorno lavorativo, quindi l'ultimo giorno utile è il giorno prima di quell'istante. Il calcolo passa in un
unico punto (`TimeConfig.lastDay`), usato da dettaglio, cronologia ed elenco.

### D2 — "In scadenza" dal promemoria
L'evidenza parte dal promemoria (`timer_reminder_sent_at`), non da un conteggio fatto dal browser: Tutor, notifica ed
elenco dicono la stessa cosa e il browser non conosce il calendario delle festività.

### D3 — Ordinamento in memoria
Come per le riaperte in "In carico a me", l'ordinamento per scadenza è stabile e fatto dopo la query: le viste del Tutor
contengono poche decine di segnalazioni. A parità di scadenza (o senza) resta l'ordine dalla più recente.

### D4 — Colonna generica
La colonna si chiama "Scadenza" e non "Timer": altre scadenze (es. sblocco della bacheca, US-903) potranno usarla.
