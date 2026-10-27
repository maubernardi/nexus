## Decisions

### D1 — Testo composto dall'interfaccia
La notifica salva solo tipo e segnalazione: nessun dato personale nel database delle notifiche e testi tutti in
`it.ts`. Un tipo che l'interfaccia non conosce ancora si mostra con un testo generico.

### D2 — Aggiornamento periodico
Il numero di non lette si aggiorna ogni minuto (le notifiche nascono dalle azioni di altri utenti): semplice e
sufficiente per il carico previsto; le API restano escluse dalla cache del service worker.

### D3 — Pagina invece di un menu a tendina
La campanella porta a una pagina "Notifiche": più semplice da usare da tastiera e con lo screen reader, e leggibile su
smartphone.

### D4 — Avviso provvisorio sostituito
L'evidenza "Tipologia cambiata" di US-302 (colonna `category_change_unseen`, endpoint di "visto") è rimossa: la
migrazione trasforma in notifiche gli avvisi non ancora visti.
