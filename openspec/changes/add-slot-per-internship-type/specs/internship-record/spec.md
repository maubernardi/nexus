## ADDED Requirements

### Requirement: Tipo della scheda dalla segnalazione
La scheda del tirocinio SHALL partire dal tipo di tirocinio della segnalazione. Il tipo della scheda e quello della
segnalazione SHALL restare uguali: cambiare il tipo dalla scheda è la stessa correzione della segnalazione, con gli
stessi controlli. Dopo l'avvio del tirocinio il tipo MUST NOT cambiare.

#### Scenario: Scheda nuova
- **WHEN** il Tutor apre per la prima volta la scheda di una segnalazione Di inclusione
- **THEN** il tipo della scheda è già «Di inclusione»

#### Scenario: Tipo cambiato dalla scheda
- **WHEN** il Tutor salva la scheda con un tipo diverso, che sulla mansione non ha altre segnalazioni
- **THEN** cambia anche il tipo della segnalazione
