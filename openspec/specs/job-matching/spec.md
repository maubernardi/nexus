# job-matching Specification

## Purpose
Ricerca delle mansioni compatibili con una segnalazione e abbinamento con proposta all'azienda (fase 4).
## Requirements
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

### Requirement: Dettagli di azienda e mansione durante l'abbinamento
Per ogni mansione compatibile il sistema SHALL offrire "Dettagli", che apre un dialogo modale con la scheda
dell'azienda (ragione sociale, partita IVA, sede, referente e contatti, stato), la scheda della mansione (titolo,
tipologia, zona, descrizione), le note più recenti e lo storico con il riepilogo dei pregressi. Dal dialogo SHALL essere
possibile proporre la mansione con la stessa conferma della tabella. Il dialogo SHALL tenere il focus al suo interno,
chiudersi con Esc o "Chiudi" riportando il focus su "Dettagli", e occupare l'intero schermo sui dispositivi piccoli.

#### Scenario: Consultazione
- **WHEN** l'operatore preme "Dettagli" su una mansione compatibile
- **THEN** il dialogo mostra azienda, mansione, note recenti e pregressi, con il link alla scheda completa dell'azienda

#### Scenario: Chiusura
- **WHEN** l'operatore preme Esc nel dialogo
- **THEN** il dialogo si chiude e il focus torna sul pulsante "Dettagli" di quella mansione

#### Scenario: Proposta dal dialogo
- **WHEN** l'operatore preme "Proponi" nel dialogo
- **THEN** il dialogo si chiude e compare la conferma della proposta, con il focus sul suo titolo, senza proposta
  inviata finché non conferma

