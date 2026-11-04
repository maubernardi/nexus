## ADDED Requirements

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
