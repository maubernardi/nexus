# ticket-detail Specification

## Purpose
Dettaglio di una segnalazione con la sua cronologia, per tutti i ruoli che possono vederla.
## Requirements
### Requirement: Dettaglio della segnalazione
Il sistema SHALL mostrare il dettaglio di una segnalazione (dati, eventuale proposta, cronologia) a Call Center e ADMIN
per ogni segnalazione, e al Tutor per le proprie e per quelle dei progetti a cui è assegnato; per le altre SHALL
rispondere `404`. Per Call Center e ADMIN il dettaglio include la lavorazione.

#### Scenario: Tutor e segnalazione del collega di progetto
- **WHEN** un Tutor apre una segnalazione di un collega dello stesso progetto
- **THEN** vede dati e cronologia in sola lettura, senza ricerca né azioni del Call Center

#### Scenario: Altro progetto
- **WHEN** un Tutor apre una segnalazione di un progetto a cui non è assegnato
- **THEN** la risposta è `404`

#### Scenario: Vecchio indirizzo
- **WHEN** si apre `/lavorazioni/:id`
- **THEN** si arriva a `/segnalazioni/:id`

### Requirement: Cronologia
La cronologia SHALL elencare, dalla più vecchia, ogni evento della segnalazione con data, autore (nome e cognome),
azione, eventuale cambio di stato in testo, dettaglio e nota.

#### Scenario: Dall'invio alla proposta
- **WHEN** una segnalazione è stata inviata, presa in carico e proposta a un'azienda
- **THEN** la cronologia mostra "Segnalazione inviata", "Presa in carico" con l'operatore e "Proposta all'azienda" con
  «mansione» presso azienda, ciascuna con autore e data

#### Scenario: Segnalazione precedente all'audit
- **WHEN** una segnalazione è nata prima dell'audit
- **THEN** la cronologia include anche i cambi di stato registrati solo nello storico

