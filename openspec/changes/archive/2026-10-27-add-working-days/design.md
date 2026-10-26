## Decisions

### D1 — Giorni contati dopo la data di partenza
Il giorno dell'accettazione non conta: la scadenza è il 7° giorno lavorativo successivo, il promemoria il 4°. Il
servizio lavora su date (`LocalDate`); sarà US-701 a stabilire "oggi" nel fuso di Roma.

### D2 — Festività nel codice, non nel database
Le festività nazionali sono stabilite per legge: calcolarle evita che manchino negli anni futuri e che vengano
modificate per errore. Pasqua con l'algoritmo di Meeus/Jones/Butcher, verificato su più anni (anche il 2038, quando
coincide con il 25 aprile).

### D3 — Chiusure lette una volta per calcolo
Il calcolo legge le chiusure in corso o future rispetto alla data di partenza (poche righe) e poi scorre i giorni in
memoria.

### D4 — Prova del calcolo per il PO
Senza interfaccia la storia non sarebbe verificabile sulla demo fino a US-701: la pagina "Calendario" offre una prova
con data di accettazione, promemoria e scadenza, aggiornata quando cambiano patrono o chiusure.
