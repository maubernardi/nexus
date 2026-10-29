## ADDED Requirements

### Requirement: Report di restituzione
Il Tutor della segnalazione SHALL poter inviare il report di restituzione durante il tirocinio. La segnalazione passa
così in `FORM_RESTITUZIONE`. Il report contiene:
- soddisfazione dell'azienda e del beneficiario, da 1 a 5;
- obiettivi raggiunti e difficoltà riscontrate (facoltativi);
- assunto sì/no: sì con la formula di assunzione, no con il motivo, scelti tra le voci attive dei cataloghi;
- mansione ancora disponibile sì/no.

Il report MUST NOT essere modificabile dopo l'invio. L'invio MUST essere registrato:
- nella cronologia e nell'audit, con le scelte ma senza i testi liberi;
- con una notifica all'operatore che ha in carico la segnalazione.

Gli altri utenti MUST NOT poter inviare il report. Il report inviato SHALL essere visibile nel dettaglio della
segnalazione a chi vede la segnalazione.

#### Scenario: Invio del report
- **WHEN** il Tutor invia il report di una segnalazione in tirocinio, con il beneficiario assunto a tempo indeterminato
- **THEN** la segnalazione è in «Report di restituzione», il dettaglio mostra il report e l’operatore riceve la notifica

#### Scenario: Assunto senza formula
- **WHEN** il Tutor invia il report con "assunto sì" senza la formula di assunzione
- **THEN** l'invio è rifiutato con l'errore sul campo e la segnalazione resta in tirocinio

#### Scenario: Report non modificabile
- **WHEN** il report è stato inviato
- **THEN** non si può inviarne un altro né modificarlo, nemmeno direttamente nel database

#### Scenario: Un altro utente
- **WHEN** un collega di progetto o il Call Center provano a inviare il report
- **THEN** la richiesta è rifiutata
