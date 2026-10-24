## MODIFIED Requirements

### Requirement: Abbinamento e proposta
Il sistema SHALL permettere a qualunque operatore del Call Center, o all'ADMIN, di abbinare una segnalazione presa in
carico a una mansione compatibile, anche se è in carico a un collega: la mansione diventa BLOCCATA dalla segnalazione,
la segnalazione passa in `PROPOSTA_AZIENDA`, l'assegnazione non cambia e l'azione è registrata con il suo autore
nell'audit, nella cronologia e nell'audit trail dell'azienda. Se la mansione è stata appena bloccata da un'altra
segnalazione o è cambiata, o se la segnalazione non è ancora stata presa in carico, il sistema SHALL rispondere `409`
con un messaggio chiaro senza modificare nulla.

#### Scenario: Proposta riuscita
- **WHEN** l'operatore conferma la proposta di una mansione libera
- **THEN** la segnalazione è in `PROPOSTA_AZIENDA` con la mansione proposta e la mansione è BLOCCATA

#### Scenario: Mansione appena bloccata
- **WHEN** la mansione scelta è stata bloccata da un'altra segnalazione un istante prima
- **THEN** la risposta è `409`, la segnalazione resta in lavorazione e l'interfaccia spiega cosa è successo

#### Scenario: Segnalazione di un collega
- **WHEN** un operatore abbina una segnalazione in carico a un collega
- **THEN** l'abbinamento riesce, la segnalazione resta in carico al collega e l'audit indica l'operatore che ha agito

### Requirement: Presa in carico di una segnalazione di un collega
Il sistema SHALL permettere a un operatore di spostare su di sé una segnalazione in carico a un collega ("Prendila in
carico tu"), partendo dalla versione vista, con un evento di audit che riporta l'operatore prima e dopo.

#### Scenario: Collega assente
- **WHEN** un operatore usa "Prendila in carico tu" su una segnalazione in carico a un collega
- **THEN** la segnalazione è in carico a lui, compare nelle sue lavorazioni e l'audit registra `TAKE_OVER`
