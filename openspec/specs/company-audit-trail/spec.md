# company-audit-trail Specification

## Purpose
Garantisce un registro cronologico e non modificabile degli eventi rilevanti per ogni azienda (proposte, esiti,
tirocini), a tutela della trasparenza verso aziende, enti finanziatori e controlli.
## Requirements
### Requirement: Registro in sola aggiunta
Il sistema SHALL registrare per ogni azienda eventi con tipo, dettagli, istante, autore ed eventuale ticket collegato.
Gli eventi registrati MUST NOT poter essere modificati né cancellati, neanche tramite accesso diretto al database con le
credenziali dell'applicazione.

#### Scenario: Aggiunta di un evento
- **WHEN** viene registrato un evento per un'azienda
- **THEN** l'evento è consultabile nello storico dell'azienda con istante e autore

#### Scenario: Tentativo di modifica
- **WHEN** si tenta di modificare un evento già registrato
- **THEN** il database rifiuta l'operazione

#### Scenario: Tentativo di cancellazione
- **WHEN** si tenta di cancellare uno o tutti gli eventi registrati
- **THEN** il database rifiuta l'operazione

### Requirement: Storico e pregressi dell'azienda
Il sistema SHALL mostrare a Call Center e ADMIN, nella scheda dell'azienda, un riepilogo dei pregressi (proposte,
accettate, appuntamenti falliti, tirocini, assunzioni, esiti negativi), con tutte le voci sempre presenti anche a zero,
e l'elenco in sola lettura degli eventi dalla più recente, con tipo, segnalazione collegata, dettaglio, autore e data.
Le esclusioni dell'azienda per una segnalazione SHALL comparire nell'elenco con il motivo. Gli altri ruoli MUST NOT
accedere allo storico.

#### Scenario: Azienda senza eventi
- **WHEN** un operatore apre la scheda di un'azienda senza eventi
- **THEN** il riepilogo mostra tutte le voci a zero e l'elenco indica che non ci sono eventi

#### Scenario: Pregressi
- **WHEN** l'azienda ha ricevuto due proposte, una accettata e conclusa con assunzione, e ha un appuntamento fallito
- **THEN** il riepilogo indica 2 proposte, 1 accettata, 1 appuntamento fallito, 1 assunzione, e ogni evento rimanda alla
  sua segnalazione

#### Scenario: Esclusione
- **WHEN** l'azienda è stata esclusa per una segnalazione con il motivo "Orari non compatibili"
- **THEN** l'elenco mostra "Azienda esclusa" con la segnalazione e il motivo

#### Scenario: Tutor
- **WHEN** un Tutor chiede lo storico di un'azienda
- **THEN** la risposta è `403`

### Requirement: Esito del tirocinio su mansione e storico
All'invio del report di restituzione il sistema SHALL aggiornare la mansione e lo storico dell'azienda:
- se la mansione è ancora disponibile, la mansione SHALL tornare libera, con l'audit del rilascio;
- se non è più disponibile, la mansione MUST restare bloccata;
- lo storico dell'azienda SHALL registrare, in sola aggiunta, la conclusione del tirocinio con la segnalazione,
  l'esito dell'assunzione (formula o motivo) e la disponibilità della mansione.

#### Scenario: Mansione ancora disponibile
- **WHEN** il Tutor invia il report con "mansione ancora disponibile: sì" e il beneficiario assunto
- **THEN** la mansione è di nuovo libera e lo storico dell'azienda mostra «Con assunzione» con la formula e
  «mansione di nuovo libera», contando un'assunzione nel riepilogo

#### Scenario: Mansione non più disponibile
- **WHEN** il Tutor invia il report con "mansione ancora disponibile: no" e il beneficiario non assunto
- **THEN** la mansione resta bloccata e lo storico dell'azienda mostra «Senza assunzione» con il motivo, contando un
  esito negativo nel riepilogo

