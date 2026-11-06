## MODIFIED Requirements

### Requirement: Contenuto e ordine della coda
Il sistema SHALL mostrare al Call Center e all'ADMIN, nella vista "Da prendere in carico" della Home (predefinita per
questi ruoli), i ticket aperti non assegnati a un operatore, esclusi quelli in attesa di approvazione della tipologia,
ordinati mettendo prima i fast-track e poi dal più vecchio al più recente. Oltre ai ticket `NUOVA` e alle speciali
`IN_LAVORAZIONE` non assegnate, la coda comprende i ticket tornati liberi perché l'operatore che li aveva in carico è
stato disattivato o è diventato Tutor (US-105), in qualunque fase: chi li prende in carico MUST seguirli dalla fase in
cui erano, senza che lo stato cambi. Il Tutor SHALL NOT accedere a questa vista.

#### Scenario: Nuova segnalazione in coda
- **WHEN** un Tutor invia una segnalazione
- **THEN** il ticket compare in "Da prendere in carico" dopo i fast-track e dopo i ticket arrivati prima

#### Scenario: Fast-track in cima
- **WHEN** la coda contiene un ticket fast-track arrivato dopo alcuni ticket normali
- **THEN** il fast-track è il primo e ha un badge con il testo "Fast-track"

#### Scenario: Ticket assegnati
- **WHEN** un ticket è assegnato a un operatore, in qualunque fase
- **THEN** non compare in "Da prendere in carico"

#### Scenario: Ticket tornato in coda
- **WHEN** l'operatore di un ticket in stato APPUNTAMENTO viene disattivato e un collega lo prende dalla coda
- **THEN** il ticket è del collega, resta in APPUNTAMENTO e la presa in carico è nella cronologia

#### Scenario: Accesso del Tutor
- **WHEN** un Tutor chiede all'API la vista "Da prendere in carico"
- **THEN** la risposta è `400` e l'interfaccia non gli mostra questa vista
