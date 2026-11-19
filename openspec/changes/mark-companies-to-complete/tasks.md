## 1. Database e dominio

- [x] 1.1 Migrazione V45: `uq_company_vat_code` diventa un indice univoco parziale che esclude «00000000000» (D3); test
  per nome in `DomainConstraintsIT`, con due aziende con il segnaposto accettate
- [x] 1.2 `CompanyPlaceholders` con le costanti e il riconoscimento (D1, D2); `missingData()` sull'azienda; test
  unitari

## 2. Backend

- [x] 2.1 `CompanyDTO.missingData` e `GET /companies?toComplete=true` (D4); test di integrazione
- [x] 2.2 Validazione dei segnaposto in registrazione e modifica, con `400` sul campo (D4); test di integrazione,
  con il completamento e l'audit
- [x] 2.3 Documenti con la riga da completare al posto dei segnaposto (D6); test di integrazione
- [x] 2.4 Due aziende dimostrative da completare nel seed locale

## 3. Frontend

- [x] 3.1 Elenco: «Da completare» nella cella del dato che manca e filtro «Solo le aziende da completare» (D5); test
  con axe
- [x] 3.2 Scheda: riga della partita IVA, avviso dei dati che mancano, campi vuoti e validazione dei segnaposto nel
  modulo (D5); test con axe
- [x] 3.3 Dettaglio dell'abbinamento senza la partita IVA segnaposto; test

## 4. Documentazione e verifica

- [x] 4.1 Collaudo (T-25.1) e Sprint 15 (lo stato nel backlog si aggiorna alla chiusura dello sprint)
- [x] 4.2 `./mvnw verify` e controlli del frontend verdi; controllo in Chrome (tastiera, axe, tema scuro, 390 px)
- [x] 4.3 `openspec validate mark-companies-to-complete` senza errori
