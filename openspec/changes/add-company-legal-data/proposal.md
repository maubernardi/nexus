## Why

Storia **US-506** (Sprint 7, richiesta del PO del 29/10): per i documenti dei tirocini servono il **legale
rappresentante** dell'azienda, che può non coincidere con il referente, e i **codici ATECO**, uno o più, tra cui nel
tirocinio si sceglie quello giusto (US-1001). La verifica sul modello ha confermato che non c'erano: l'azienda aveva
solo ragione sociale, P. IVA, sede legale, referente, telefono ed email.

## What Changes

- **Migrazione V22**:
  - `company_legal_representative`: uno per azienda; nome e cognome non vuoti; codice fiscale in formato;
  - `company_ateco_code`: codice unico per azienda; formato `NN`, `NN.NN` o `NN.NN.NN`.
- API, per Call Center e ADMIN:
  - `GET/PUT /companies/{id}/legal-representative`: `204` se non indicato; versione nulla alla prima registrazione;
  - `GET/POST/DELETE /companies/{id}/ateco-codes`.
- Codice fiscale: nuova validazione `@FiscalCode` (`FiscalCodes`), con struttura ufficiale, omocodia e carattere di
  controllo. Si salva in maiuscolo. Servirà anche per il beneficiario nella scheda del tirocinio.
- Audit `COMPANY`:
  - `LEGAL_REPRESENTATIVE` registra solo quali campi sono cambiati, **senza i valori** (dati personali);
  - `ATECO_ADD` e `ATECO_REMOVE` registrano il codice.
- Frontend: nella scheda dell'azienda due sezioni nuove.
  - **Legale rappresentante**: dati in testo, "Non indicato" per quelli mancanti; modulo con errori nel riepilogo.
  - **Codici ATECO**: elenco, aggiunta con il formato controllato, "Togli".

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `company-registry`: legale rappresentante e codici ATECO.
