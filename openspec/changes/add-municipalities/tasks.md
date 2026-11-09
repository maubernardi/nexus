## 1. Backend

- [x] 1.1 Migrazione V33 (`province`, `municipality`, `municipality_import`) con i vincoli testati per nome
- [x] 1.2 Lettura del file ISTAT (CSV con `;`, Latin-1 o UTF-8, intestazione riconosciuta per nome) con test unitari
- [x] 1.3 Anteprima e caricamento con le versioni (chiusura e apertura), controlli (file, completezza, data), audit
- [x] 1.4 API: stato, anteprima, caricamento (ADMIN), ricerca (autenticati); test di integrazione
- [x] 1.5 Demo: caricamento automatico dell'elenco nel profilo `seed` se le tabelle sono vuote

## 2. Frontend

- [x] 2.1 Sezione «Comuni e province» nei Cataloghi: stato, caricamento con anteprima e conferma, ricerca; test con axe

## 3. Consegna

- [x] 3.1 Collaudo (`docs/test/collaudo.md`) e backlog
- [ ] 3.2 PR con CI verde
