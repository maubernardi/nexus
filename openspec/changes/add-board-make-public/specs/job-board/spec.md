## ADDED Requirements

### Requirement: Sblocco a tutti dopo 7 giorni
Il Call Center e l'ADMIN SHALL poter rendere visibile ai Tutor di tutti i progetti un annuncio pubblicato e riservato a
un progetto, solo a partire da 7 giorni solari dopo la pubblicazione. Lo sblocco SHALL togliere il progetto,
registrare la data e comparire nell'audit. Prima di quella data il comando MUST NOT essere disponibile e l'API SHALL
rispondere `409` indicando da quando si potrà fare.

#### Scenario: Troppo presto
- **WHEN** l'annuncio riservato è stato pubblicato da 2 giorni
- **THEN** la scheda dice da quando si potrà rendere visibile a tutti e il comando non c'è

#### Scenario: Sblocco
- **WHEN** sono passati 7 giorni dalla pubblicazione e l'operatore conferma "Rendi visibile a tutti"
- **THEN** l'annuncio non è più riservato, la scheda riporta "Visibile a tutti dal …" e lo vedono i Tutor di ogni
  progetto

#### Scenario: Già per tutti o non pubblicato
- **WHEN** l'annuncio è già visibile a tutti oppure è una bozza
- **THEN** lo sblocco è rifiutato con il motivo
