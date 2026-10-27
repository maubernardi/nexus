## ADDED Requirements

### Requirement: Appuntamento non andato a buon fine
Il Tutor della segnalazione SHALL poter registrare, per una segnalazione in `APPUNTAMENTO`, che l'appuntamento non è
andato a buon fine, indicando una causa (dall'elenco) e una nota, obbligatoria con la causa "Altro". La segnalazione
passa in `RIAPERTO`, resta in carico allo stesso operatore e la mansione torna libera; l'azienda MUST NOT essere esclusa
automaticamente. L'evento SHALL essere registrato nell'audit, in cronologia (con causa e nota) e nello storico
dell'azienda, e notificato all'operatore. Una segnalazione riaperta SHALL poter essere abbinata di nuovo.

#### Scenario: Appuntamento fallito
- **WHEN** il Tutor registra la causa "L'azienda non ha confermato il tirocinio"
- **THEN** la segnalazione è "Riaperto", la mansione è libera, la cronologia mostra la causa, lo storico dell'azienda
  conta un appuntamento fallito e l'operatore riceve la notifica

#### Scenario: Causa "Altro" senza nota
- **WHEN** il Tutor sceglie "Altro" senza scrivere la nota
- **THEN** il sistema chiede la nota

#### Scenario: Nuova proposta
- **WHEN** l'operatore apre la segnalazione riaperta
- **THEN** trova le mansioni compatibili e può proporne una, anche della stessa azienda

#### Scenario: Altri utenti o stato diverso
- **WHEN** un operatore prova a registrarlo, o il Tutor lo fa prima dell'appuntamento
- **THEN** la risposta è rispettivamente `403` e `409`
