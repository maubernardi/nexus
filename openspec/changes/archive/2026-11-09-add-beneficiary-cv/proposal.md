## Why

Storia **US-205** (Sprint 11): il Tutor allega il CV in PDF al beneficiario per metterlo a disposizione del Call Center
nell'abbinamento. Criteri: solo PDF fino a 5 MB, il file non è accessibile senza autorizzazione, si scarica solo da chi
vede il beneficiario.

## What Changes

- **Migrazione V32**: tabella `stored_file_content` con il contenuto dei file (vincolo `ck_stored_file_content_size`,
  1 byte – 5 MB; si cancella con il file). Il contenuto sta nel database: il backup del database copre anche i CV e non
  serve uno spazio su disco sul server.
- **API**: `PUT /beneficiaries/{id}/cv` (multipart, Tutor proprietario), `GET /beneficiaries/{id}/cv` (chi vede il
  beneficiario, come allegato), `DELETE /beneficiaries/{id}/cv` (Tutor proprietario). Solo PDF riconosciuto dal
  contenuto (`%PDF-`), fino a 5 MB; sostituire cancella il vecchio file. Audit `CV_UPLOADED`, `CV_REPLACED`,
  `CV_REMOVED` con la sola dimensione (il nome del file può contenere il nome della persona).
- **Anonimizzazione** (US-1303): cancella anche il CV, file e contenuto.
- **Scheda del beneficiario**: sezione «Curriculum» con nome e dimensione, «Scarica il CV»; per il proprietario
  «Carica» / «Sostituisci» / «Togli il CV».
- **Revisione ASVS**: capitolo V12 (caricamento di file) rivisto.

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `beneficiary-registry`: requisito aggiunto per il curriculum.

## Impact

- Migrazione **V32**; nessun passo sul server.
