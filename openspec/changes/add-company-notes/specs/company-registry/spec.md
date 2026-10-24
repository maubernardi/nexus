## ADDED Requirements

### Requirement: Diario delle note dell'azienda
Il sistema SHALL consentire a Call Center e ADMIN di aggiungere note di testo (obbligatorio, al massimo 2000 caratteri)
alla scheda di un'azienda e di consultarle dalla più recente, con data, ora e autore. Le note MUST NOT essere modificate
né cancellate, e il database SHALL rifiutarne modifica, cancellazione e svuotamento. L'aggiunta SHALL essere registrata
nell'audit senza il testo della nota.

#### Scenario: Aggiunta di una nota
- **WHEN** un operatore aggiunge la nota "Chiusura per ferie ad agosto"
- **THEN** la nota compare in cima al diario con data, ora e nome dell'operatore, e l'audit registra `NOTE_ADD` senza testo

#### Scenario: Nota vuota
- **WHEN** l'operatore invia una nota vuota o di soli spazi
- **THEN** il sistema la rifiuta indicando il campo

#### Scenario: Note immodificabili
- **WHEN** si tenta di modificare o cancellare una nota direttamente nel database
- **THEN** l'operazione è rifiutata

#### Scenario: Tutor
- **WHEN** un Tutor chiede le note di un'azienda
- **THEN** la risposta è `403`
