## Purpose

Ricerca delle mansioni compatibili con una segnalazione e abbinamento con proposta all'azienda (fase 4).

## ADDED Requirements

### Requirement: Mansioni compatibili
Il sistema SHALL proporre per una segnalazione solo le mansioni LIBERE, a disposizione, di aziende attive e non escluse
per quella segnalazione, filtrabili per zona e tipologia. Nell'interfaccia i filtri SHALL partire dalla zona di
residenza del beneficiario e dalla mansione richiesta, e l'operatore SHALL poterli allargare a tutte le zone o
tipologie.

#### Scenario: Esclusioni
- **WHEN** l'operatore cerca le mansioni per una segnalazione
- **THEN** non compaiono mansioni bloccate, ritirate, di aziende disattivate o di aziende escluse per la segnalazione

#### Scenario: Filtri allargati
- **WHEN** l'operatore sceglie "Tutte le zone"
- **THEN** l'elenco include le mansioni compatibili di ogni zona e il numero di risultati viene annunciato

### Requirement: Abbinamento e proposta
Il sistema SHALL permettere all'operatore che ha in carico la segnalazione (o all'ADMIN) di abbinarla a una mansione
compatibile: la mansione diventa BLOCCATA dalla segnalazione, la segnalazione passa in `PROPOSTA_AZIENDA` e l'azione è
registrata nell'audit, nella cronologia e nell'audit trail dell'azienda. Se la mansione è stata appena bloccata da
un'altra segnalazione o è cambiata, il sistema SHALL rispondere `409` con un messaggio chiaro senza modificare nulla.

#### Scenario: Proposta riuscita
- **WHEN** l'operatore conferma la proposta di una mansione libera
- **THEN** la segnalazione è in `PROPOSTA_AZIENDA` con la mansione proposta e la mansione è BLOCCATA

#### Scenario: Mansione appena bloccata
- **WHEN** la mansione scelta è stata bloccata da un'altra segnalazione un istante prima
- **THEN** la risposta è `409`, la segnalazione resta in lavorazione e l'interfaccia spiega cosa è successo

#### Scenario: Segnalazione di un collega
- **WHEN** un operatore tenta di abbinare una segnalazione in carico a un collega
- **THEN** la risposta è `409` e l'interfaccia non mostra la ricerca
