## ADDED Requirements

### Requirement: Tirocinio di inclusione
Il tipo di tirocinio «Di inclusione» (codice `INCLUSIONE`) SHALL potersi scegliere solo se il beneficiario della
segnalazione ha l'ente che lo ha in carico e nome, mail e telefono del case manager. La scheda del tirocinio SHALL
indicare cosa manca; il sistema MUST rifiutare la scelta altrimenti. Un tipo di inclusione già scelto resta valido.

#### Scenario: Dati mancanti
- **WHEN** il Tutor sceglie «Di inclusione» per un beneficiario senza ente
- **THEN** il campo segnala che mancano l'ente e i dati del case manager e nulla è salvato

#### Scenario: Dati completi
- **WHEN** il beneficiario ha ente e case manager con nome, mail e telefono
- **THEN** il Tutor sceglie «Di inclusione» e la scheda si salva
