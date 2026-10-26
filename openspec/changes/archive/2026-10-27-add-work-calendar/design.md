## Decisions

### D1 — Una riga per il calendario
Il calendario è unico (PO, 24/10): una tabella con una sola riga (id = 1, vincolo nel database) creata dalla migrazione,
invece di una tabella generica di impostazioni. Il patrono è salvato come giorno e mese; il 29 febbraio è ammesso
(festivo solo negli anni bisestili), il 30 febbraio no.

### D2 — Chiusure modificabili, storia nell'audit
Le chiusure si aggiungono e si rimuovono (per correggerne una si rimuove e si reinserisce): la storia completa resta
nell'audit, con i valori. Le scadenze già calcolate non cambiano: US-701 salverà la data di scadenza al momento
dell'accettazione.

### D3 — Festività nel codice
Le festività nazionali (Pasqua compresa) si calcolano (US-703): la pagina le mostra solo come promemoria, per evitare
che vengano inserite come chiusure. Il 4 ottobre non è considerato festivo (PO, 24/10).

### D4 — Solo l'ADMIN
Lettura e modifica sono dell'ADMIN; il calcolo delle scadenze (US-703) leggerà il calendario lato server, senza
esporlo agli altri ruoli.
