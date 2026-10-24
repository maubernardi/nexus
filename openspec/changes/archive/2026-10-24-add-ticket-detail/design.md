## Decisions

### D1 — Cronologia dall'audit
L'audit registra già ogni transizione con autore, istante, stato prima/dopo, nota e modifiche: è la fonte più ricca.
Il servizio traduce in testo le modifiche utili (operatore, mansione e azienda) e gli username in nome e cognome
("Sistema" per le operazioni automatiche).

### D2 — Segnalazioni nate prima dell'audit
I dati demo e le segnalazioni inviate prima di EN-2 non hanno tutti gli eventi in audit: si aggiungono le righe dello
storico degli stati il cui passaggio (stato prima → dopo) non compare nell'audit. Il confronto per istante non funziona:
storico e audit della stessa operazione differiscono di pochi microsecondi.

### D3 — Una pagina, contenuti per ruolo
Una sola pagina di dettaglio evita due viste divergenti: il Tutor vede dati, proposta e cronologia in sola lettura; il
Call Center vede in più la lavorazione. La visibilità è verificata dall'API (`404` uguale a "inesistente").
