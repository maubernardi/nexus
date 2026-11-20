## 1. Database e dominio

- [x] 1.1 Migrazione V49: `ticket.internship_type_id`, `ticket.job_slot_held`, vincoli e dati (D1, D2); test per nome
  in `DomainConstraintsIT` e test della migrazione con i dati
- [x] 1.2 `Ticket` con tipo e assegnazione; riassunto sulla mansione (`JobSlotAssignments`)

## 2. Backend

- [x] 2.1 Regole del tipo in un posto solo; tipo obbligatorio nella creazione e nella candidatura; tipi per progetto
  nei dati di riferimento (D1)
- [x] 2.2 Proposta per tipo con i controlli e la versione della mansione; rilasci per una sola segnalazione (D3, D4)
- [x] 2.3 Mansioni compatibili con le assegnazioni degli altri tipi (D5)
- [x] 2.4 Correzione del tipo dalla segnalazione e dalla scheda, con l'audit (D6)
- [x] 2.5 Mansioni dell'azienda e messaggi di ritiro e disattivazione con tutte le assegnazioni (D9)
- [x] 2.6 Test di integrazione: due tipi sulla stessa mansione, stesso tipo `409`, tipo non indicato, rilasci,
  correzione, scheda

## 3. Frontend

- [x] 3.1 Tipo nei moduli di segnalazione e candidatura (D7); test con axe
- [x] 3.2 Tipo nella segnalazione e correzione del Tutor (D7); test con axe
- [x] 3.3 Avvisi nell'abbinamento: elenco, dettagli, conferma; tipo non indicato (D8); test con axe
- [x] 3.4 Stato delle mansioni dell'azienda con le assegnazioni (D9); test

## 4. Documentazione e verifica

- [x] 4.1 Collaudo (T-25.5) e Sprint 15
- [x] 4.2 `./mvnw verify` e controlli del frontend verdi; controllo in Chrome (tastiera, avvisi, axe, tema scuro,
  390 px)
- [x] 4.3 `openspec validate add-slot-per-internship-type` senza errori
