## ADDED Requirements

### Requirement: Vincolo sulla segnalazione aperta
Il database SHALL garantire con l'indice unico parziale `uq_ticket_open_per_beneficiary` che ogni beneficiario abbia al
più un ticket in stato diverso da `FORM_RESTITUZIONE`.

#### Scenario: Secondo ticket aperto
- **WHEN** si inserisce un secondo ticket non concluso per lo stesso beneficiario
- **THEN** l'inserimento fallisce violando `uq_ticket_open_per_beneficiary`

#### Scenario: Ticket concluso
- **WHEN** il beneficiario ha solo ticket in `FORM_RESTITUZIONE`
- **THEN** l'inserimento di un nuovo ticket riesce
