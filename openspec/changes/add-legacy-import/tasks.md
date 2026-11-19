## 1. Database e configurazione

- [x] 1.1 Migrazione V48: tabella `legacy_call` (D9); test per nome in `DomainConstraintsIT`
- [x] 1.2 Dipendenza Jackcess; profilo `legacy-import`; `SchedulingConfig` condizionale (D1, D2)

## 2. Importazione

- [x] 2.1 Lettura del file Access in sola lettura (D2)
- [x] 2.2 Abbinamento dei comuni con gli alias (D5); test unitari con i casi dei dati veri
- [x] 2.3 Pulizia di telefoni ed email (D6); test unitari
- [x] 2.4 Scrittura di aziende, referenti, note, audit e chiamate in una transazione, con gli id fissi (D3, D4, D7,
  D8, D9)
- [x] 2.5 Resoconto e conteggi nel log (D10); avvio e chiusura con il profilo (D1)
- [x] 2.6 Autore «Database attuale» nel diario (D4)
- [x] 2.7 Test di integrazione con un file Access costruito nel test (D11)

## 3. Prova e documentazione

- [x] 3.1 Prova in locale sul file vero, su una copia del database; alias completati con il resoconto
- [x] 3.2 Procedura nell'analisi (§9), collaudo e Sprint 15
- [x] 3.3 `./mvnw verify` e controlli del frontend verdi; `openspec validate add-legacy-import` senza errori
