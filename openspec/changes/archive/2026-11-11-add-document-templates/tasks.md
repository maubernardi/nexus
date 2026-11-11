## 1. Backend

- [x] 1.1 Migrazione V40 solo additiva: tabella `document_template` con vincoli con nome; test dei vincoli
- [x] 1.2 Segnaposto noti in `DocumentPlaceholders`; relazione finale tra i documenti (scaricamento ancora rifiutato)
- [x] 1.3 Servizio e API dei modelli per l'ADMIN: caricamento con i controlli (Word, segnaposto, condizioni), elenco,
  file, sostituzione, attivazione, segnaposto noti; audit; test di integrazione
- [x] 1.4 Scelta del modello al download (candidati, ente, ripiego), elenco per il Tutor, modello nella cronologia; test
  di integrazione con modelli finti

## 2. Frontend

- [x] 2.1 Pagina «Modelli dei documenti» (menu dell'ADMIN): segnaposto noti, caricamento, elenco con scarica,
  sostituisci, disattiva e riattiva; test con axe
- [x] 2.2 Scheda del tirocinio: scelta del modello quando ce n'è più di uno; test con axe

## 3. Consegna

- [x] 3.1 Verifiche: `./mvnw verify`; lint, formato, test e build del frontend
- [x] 3.2 Controllo in Chrome prima della PR (A26): tastiera, axe, tema scuro, smartphone
- [x] 3.3 Collaudo (T-23.4) e PR con la CI verde
