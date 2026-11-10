## Why

Storia **US-206** (Sprint 12): i documenti reali (US-1011) chiedono mail e telefono del tirocinante, e i tirocini di
inclusione chiedono l'ente che ha in carico il beneficiario e il suo case manager (decisioni del PO del 09/11). Senza
ente e senza nome, mail e telefono del case manager non si può fare un tirocinio di inclusione.

## What Changes

- **Migrazione V35**: nel beneficiario `email`, `phone`, `care_agency_id` (ente del catalogo di US-115) e il case
  manager (`case_manager_name`, `case_manager_office`, `case_manager_phone`, `case_manager_email`); tutti facoltativi,
  con vincoli di formato testati per nome.
- **Scheda del beneficiario**: sezione «Contatti e presa in carico». L'ente non si cambia mentre il beneficiario ha un
  tirocinio aperto (una segnalazione non ancora in FORM_RESTITUZIONE con la scheda del tirocinio): il campo è bloccato
  con la spiegazione e il server rifiuta. Il case manager si può sostituire sempre, ma durante un tirocinio di
  inclusione aperto nome, mail e telefono non si svuotano.
- **Scheda del tirocinio**: il tipo «Di inclusione» si sceglie solo se il beneficiario ha l'ente e nome, mail e
  telefono del case manager; altrimenti l'opzione è disattivata con l'elenco di ciò che manca e il server rifiuta.
- **Privacy**: i nuovi dati si vedono solo dove si vede il beneficiario; nell'audit solo quali campi cambiano;
  l'anonimizzazione (US-1303) li cancella.
- **Riferimenti**: `GET /reference/care-agencies` con gli enti attivi.

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `beneficiary-registry`: requisito aggiunto per contatti e presa in carico.
- `internship-record`: requisito aggiunto per il tirocinio di inclusione.

## Impact

- Migrazione **V35**; nessun passo sul server.
