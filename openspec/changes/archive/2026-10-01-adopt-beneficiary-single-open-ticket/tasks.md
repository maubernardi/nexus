## 1. Terminologia

- [x] 1.1 Rinomina in codice, API, rotte, testi, seed, spec e documenti; `V8` per tabelle, colonne, vincoli e indici
- [x] 1.2 Glossario in `CLAUDE.md` e `openspec/config.yaml`

## 2. Una segnalazione aperta per beneficiario

- [x] 2.1 `V9` (riallineamento dati demo + indice unico parziale), seed aggiornato
- [x] 2.2 Controllo nel service (400 sul campo), `DataIntegrityViolationException` → 409, numero della segnalazione aperta nell'elenco dei beneficiari
- [x] 2.3 Modulo: beneficiari con segnalazione aperta non selezionabili, stato "tutti aperti"
- [x] 2.4 Test: vincolo per nome, 400 con numero, nuova segnalazione dopo la conclusione, elenco, modulo

## 3. Verifica e consegna

- [x] 3.1 Migrazione su un database con i dati demo esistenti; verifica in browser reale
- [x] 3.2 Backlog (Q12) aggiornato; PR con CI verde
