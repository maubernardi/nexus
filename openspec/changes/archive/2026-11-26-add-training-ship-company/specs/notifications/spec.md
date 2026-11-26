## ADDED Requirements

### Requirement: Avviso della proposta di nave scuola
L'avviso «proposta di nave scuola» SHALL arrivare all'operatore della segnalazione nell'app e per email, con il numero
della segnalazione e il collegamento alla scheda dell'azienda, invece che alla segnalazione. I testi SHALL essere tra i
«Testi dei messaggi» modificabili dall'ADMIN, con i predefiniti. L'email SHALL avere l'oggetto «PORTALE NEXUS - …» e la
firma «Portale Nexus», come le altre.

#### Scenario: Avviso nell'app
- **WHEN** l'operatore apre l'avviso della proposta di nave scuola
- **THEN** arriva alla scheda dell'azienda

#### Scenario: Email
- **WHEN** parte l'avviso della proposta
- **THEN** l'email ha l'oggetto «PORTALE NEXUS - Segnalazione n. N - Proposta di nave scuola», il collegamento alla
  scheda dell'azienda e la firma «Portale Nexus»
