## Why

Storia **US-308** (Sprint 12): la sede di competenza (catalogo di US-115) è il luogo dei documenti di fine tirocinio.
Si indica quando si crea la segnalazione, è obbligatoria se il progetto ha delle sedi e poi resta bloccata; la
correggono solo l'ADMIN e il Coordinatore del progetto (decisioni del PO del 09/11).

## What Changes

- **Migrazione V36**: `ticket.competence_site_id` (facoltativo, FK verso `competence_site`).
- **Nuova segnalazione e candidatura dalla bacheca**: si sceglie tra le sedi attive del progetto della segnalazione;
  obbligatoria se il progetto ne ha almeno una, altrimenti il campo non c'è. Il server controlla che la sede sia del
  progetto.
- **Dettaglio della segnalazione**: mostra la sede. L'ADMIN e il Coordinatore del progetto (US-112) la correggono con
  `PUT /tickets/{id}/competence-site`; la correzione è nell'audit della segnalazione (prima, dopo, autore).
- **Riferimenti**: `GET /reference/competence-sites?projectId=` con le sedi attive del progetto.
- Le segnalazioni esistenti restano senza sede.

## Capabilities

### New Capabilities
<!-- nessuna -->

### Modified Capabilities
- `ticket-submission`: requisito aggiunto per la sede di competenza alla creazione.
- `ticket-detail`: requisito aggiunto per la correzione della sede.

## Impact

- Migrazione **V36**; nessun passo sul server.
